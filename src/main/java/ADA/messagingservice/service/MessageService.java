package ADA.messagingservice.service;

import ADA.messagingservice.dto.ConversationResponse;
import ADA.messagingservice.dto.ConversationSummary;
import ADA.messagingservice.dto.MessageResponse;
import ADA.messagingservice.dto.SendMessageRequest;
import ADA.messagingservice.entity.Message;
import ADA.messagingservice.exception.ForbiddenException;
import ADA.messagingservice.exception.ResourceNotFoundException;
import ADA.messagingservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository repository;

    @Transactional
    public MessageResponse send(Integer senderId, SendMessageRequest request) {
        Message message = Message.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .build();
        Message saved = repository.save(message);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ConversationResponse getHistory(Integer userId, Integer otherUserId, Pageable pageable) {
        Page<Message> page = repository.findConversation(userId, otherUserId, pageable);
        List<MessageResponse> messages = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return ConversationResponse.builder()
                .otherUserId(otherUserId)
                .messages(messages)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ConversationSummary> listConversations(Integer userId) {
        List<Message> all = repository.findAllForUser(userId);

        Map<Integer, List<Message>> byPartner = new LinkedHashMap<>();
        for (Message m : all) {
            Integer partnerId = m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId();
            byPartner.computeIfAbsent(partnerId, k -> new ArrayList<>()).add(m);
        }

        return byPartner.entrySet().stream()
                .map(entry -> {
                    List<Message> msgs = entry.getValue();
                    Message last = msgs.get(0);
                    long unread = msgs.stream()
                            .filter(m -> m.getReceiverId().equals(userId) && m.getReadAt() == null)
                            .count();
                    return ConversationSummary.builder()
                            .otherUserId(entry.getKey())
                            .lastMessage(last.getContent())
                            .lastMessageAt(last.getSentAt())
                            .unreadCount(unread)
                            .build();
                })
                .toList();
    }

    @Transactional
    public MessageResponse markRead(Integer userId, Integer messageId) {
        Message message = repository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));
        if (!message.getReceiverId().equals(userId)) {
            throw new ForbiddenException("You can only mark messages sent to you as read");
        }
        message.setReadAt(OffsetDateTime.now());
        return toResponse(repository.save(message));
    }

    private MessageResponse toResponse(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSenderId())
                .receiverId(m.getReceiverId())
                .content(m.getContent())
                .sentAt(m.getSentAt())
                .readAt(m.getReadAt())
                .build();
    }
}

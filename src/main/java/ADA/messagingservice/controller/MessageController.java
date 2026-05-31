package ADA.messagingservice.controller;

import ADA.messagingservice.dto.ConversationResponse;
import ADA.messagingservice.dto.ConversationSummary;
import ADA.messagingservice.dto.MessageResponse;
import ADA.messagingservice.dto.SendMessageRequest;
import ADA.messagingservice.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@Valid @RequestBody SendMessageRequest request, Authentication auth) {
        return messageService.send(extractUserId(auth), request);
    }

    @GetMapping("/conversations")
    public List<ConversationSummary> listConversations(Authentication auth) {
        return messageService.listConversations(extractUserId(auth));
    }

    @GetMapping("/{otherUserId}")
    public ConversationResponse getHistory(
            @PathVariable Integer otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        return messageService.getHistory(extractUserId(auth), otherUserId, pageable);
    }

    @PatchMapping("/{messageId}/read")
    public MessageResponse markRead(@PathVariable Integer messageId, Authentication auth) {
        return messageService.markRead(extractUserId(auth), messageId);
    }

    private Integer extractUserId(Authentication auth) {
        return Integer.parseInt((String) auth.getPrincipal());
    }
}

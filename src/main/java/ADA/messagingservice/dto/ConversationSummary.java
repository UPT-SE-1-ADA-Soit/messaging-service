package ADA.messagingservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class ConversationSummary {
    private Integer otherUserId;
    private String lastMessage;
    private OffsetDateTime lastMessageAt;
    private long unreadCount;
}

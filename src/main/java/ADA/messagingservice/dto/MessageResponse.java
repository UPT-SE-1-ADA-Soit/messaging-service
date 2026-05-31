package ADA.messagingservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class MessageResponse {
    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private OffsetDateTime sentAt;
    private OffsetDateTime readAt;
}

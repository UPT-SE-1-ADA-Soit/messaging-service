package ADA.messagingservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConversationResponse {
    private Integer otherUserId;
    private List<MessageResponse> messages;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}

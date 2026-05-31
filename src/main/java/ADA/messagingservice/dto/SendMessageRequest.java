package ADA.messagingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull
    @Positive
    private Integer receiverId;

    @NotBlank
    private String content;
}

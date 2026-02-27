package emory.emoryserver.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record ImageGenerateRequest(
        @NotBlank(message = "sessionId is required")
        String sessionId
) {}

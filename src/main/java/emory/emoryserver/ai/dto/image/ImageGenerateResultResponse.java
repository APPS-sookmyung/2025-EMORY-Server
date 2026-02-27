package emory.emoryserver.ai.dto.image;

public record ImageGenerateResultResponse(
        String imageId,
        String imageUrl
) {}
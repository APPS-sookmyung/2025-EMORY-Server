package emory.emoryserver.ai.dto;

public record ImageGenerateResponse(
        String image,      // base64 또는 data_uri 또는 url(확장 시)
        String format,     // png/webp/jpeg
        String model,
        String size
) {}

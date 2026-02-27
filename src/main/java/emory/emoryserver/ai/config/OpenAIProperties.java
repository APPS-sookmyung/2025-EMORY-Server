package emory.emoryserver.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "openai")
public record OpenAIProperties(
        String apiKey,
        String baseUrl,
        Image image,
        Webclient webclient
) {
    public record Image(
            String model,
            String size,
            String outputFormat,
            String quality,
            String background,
            Integer n,
            String returnMode
    ) {}

    public record Webclient(
            Integer maxInMemorySize
    ) {}
}

package emory.emoryserver.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(OpenAIProperties.class)
public class OpenAIWebClientConfig {

    @Bean
    public WebClient openAiWebClient(OpenAIProperties props) {
        int maxSize = (props.webclient() != null && props.webclient().maxInMemorySize() != null)
                ? props.webclient().maxInMemorySize()
                : 10 * 1024 * 1024;

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(maxSize))
                .build();

        return WebClient.builder()
                .baseUrl(props.baseUrl())
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

package emory.emoryserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EMORY API")
                        .version("v1"))
                .servers(List.of(
                        new Server()
                                .url("https://emory-server-406346608321.asia-northeast3.run.app")
                                .description("Cloud Run HTTPS endpoint")
                ));
    }
}
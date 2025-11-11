package emory.emoryserver.config;

import io.swagger.v3.oas.models.OpenAPI;
<<<<<<< HEAD
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
=======
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
<<<<<<< HEAD
                .info(new Info().title("EMORY API").version("v1"))
                .servers(List.of(new Server()
                        .url("https://emory-server-406346608321.asia-northeast3.run.app")
                        .description("Cloud Run HTTPS endpoint")));
=======
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .info(new Info()
                        .title("EMORY Server API")
                        .description("swagger description")
                        .version("1.0.0"));
>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
    }
}

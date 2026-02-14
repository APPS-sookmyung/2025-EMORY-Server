package emory.emoryserver;

import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MongoUriDebug {
    private final Environment env;

    public MongoUriDebug(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void log() {
        String uri = env.getProperty("spring.data.mongodb.uri");
        String envUri = System.getenv("SPRING_DATA_MONGODB_URI");

        System.out.println(">>> EFFECTIVE spring.data.mongodb.uri = " + sanitize(uri));
        System.out.println(">>> ENV SPRING_DATA_MONGODB_URI = " + sanitize(envUri));
    }

    private String sanitize(String s) {
        if (s == null) return "null";
        // mongodb+srv://user:password@... 형태에서 password만 마스킹
        return s.replaceAll("(?<=mongodb\\+srv://[^:]+:)[^@]+(?=@)", "****");
    }
}

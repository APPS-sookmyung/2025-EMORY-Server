package emory.emoryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "emory.emoryserver")
public class EmoryserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmoryserverApplication.class, args);
	}

}

package emory.emoryserver.web;

import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PingController {

    @PermitAll
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "time", System.currentTimeMillis());
    }
}
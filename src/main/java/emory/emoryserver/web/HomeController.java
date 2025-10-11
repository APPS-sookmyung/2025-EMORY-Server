package emory.emoryserver.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/swagger-ui.html";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
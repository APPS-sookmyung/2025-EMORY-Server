package emory.emoryserver.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        // 루트로 오면 스웨거로 보냄
        return "redirect:/swagger-ui/index.html";
    }
}

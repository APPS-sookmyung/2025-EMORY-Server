package emory.emoryserver.domain.admin.controller;

import emory.emoryserver.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users/count")
    public ResponseEntity<Long> countUsers() {
        long count = userService.countAllUsers();
        return ResponseEntity.ok(count);
    }
}

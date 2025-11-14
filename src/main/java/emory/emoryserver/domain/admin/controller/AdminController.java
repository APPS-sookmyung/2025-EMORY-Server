package emory.emoryserver.domain.admin.controller;

<<<<<<< HEAD
import emory.emoryserver.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
=======
>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
<<<<<<< HEAD
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users/count")
    public ResponseEntity<Long> countUsers() {
        long count = userService.countAllUsers();
        return ResponseEntity.ok(count);
=======
public class AdminController {

    // 1.5 전체 사용자 수 조회
    @GetMapping("/users/count")
    public ResponseEntity<String> getUserCount() {
        return ResponseEntity.ok("전체 사용자 수 조회");
>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
    }
}

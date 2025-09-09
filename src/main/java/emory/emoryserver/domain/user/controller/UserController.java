package emory.emoryserver.domain.user.controller;

import emory.emoryserver.domain.user.dto.request.UserProfileUpdateRequest;
import emory.emoryserver.domain.user.dto.response.UserProfileResponse;
import emory.emoryserver.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal String email) {
        UserProfileResponse profile = userService.getMyProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal String email,
            @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse updated = userService.updateProfile(email, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal String email) {
        userService.deleteAccount(email);
        return ResponseEntity.noContent().build();
    }
}

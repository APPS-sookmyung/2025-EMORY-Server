package emory.emoryserver.domain.user.service;

import emory.emoryserver.domain.user.dto.request.UserProfileUpdateRequest;
import emory.emoryserver.domain.user.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getMyProfile(String userId);
    UserProfileResponse updateProfile(String userId, UserProfileUpdateRequest request);
    void deleteAccount(String userId); // 회원 탈퇴
    long countAllUsers();

}

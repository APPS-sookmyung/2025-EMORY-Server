package emory.emoryserver.domain.user.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String email;
    private String password;  // SNS 로그인만 한다면 사용하지 않아도 됨
    private String nickname;

    private String provider;
    private String providerId;

    @Builder.Default
    private boolean diaryReminderEnabled = false;
    @Builder.Default
    private String reminderTime = null;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateDiarySetting(boolean enabled, String time) {
        this.diaryReminderEnabled = enabled;
        this.reminderTime = time;
    }
}

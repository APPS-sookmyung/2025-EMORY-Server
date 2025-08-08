package emory.emoryserver.common.enums;

import lombok.Getter;

@Getter
public enum EmotionCategory {
    JOY("기쁨"),
    HAPPY("행복"),
    SAD("슬픔"),
    ANGRY("화남"),
    ANXIOUS("불안"),
    SOSO("보통");

    private final String description;

    EmotionCategory(String description) {
        this.description = description;
    }
}
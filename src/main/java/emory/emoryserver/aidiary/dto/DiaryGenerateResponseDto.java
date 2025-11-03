package emory.emoryserver.aidiary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryGenerateResponseDto {
    @Schema(description = "일기 ID")
    private String diaryId;

    @Schema(description = "일기 제목")
    private String title;

    @Schema(description = "AI가 생성한 일기 내용")
    private String content;

    @Schema(description = "감정 태그", example = "슬픔")
    private String emotion;

    @Schema(description = "이미지 ID")
    private String imageId;

    @Schema(description = "버전")
    private Integer version;

    @Schema(description = "상태", example = "DRAFT")
    private String status;

    @Schema(description = "수정 가능 여부")
    private Boolean editable;

    @Schema(description = "스크랩 여부", example = "false")
    private Boolean scraped;

    @Schema(description = "일기 생성 날짜", example = "2025-07-16")
    private LocalDate date;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;
}

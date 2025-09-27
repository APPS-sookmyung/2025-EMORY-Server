package emory.emoryserver.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryResponse {
    @Schema(description = "일기 ID")
    private String diaryId;

    @Schema(description = "일기 제목")
    private String title;

    @Schema(description = "일기 내용")
    private String content;

    @Schema(description = "감정 태그", example = "기쁨")
    private String emotion;

    @Schema(description = "이미지 ID")
    private String imageId;

    @Schema(description = "스크랩 여부")
    private Boolean scraped;

    @Schema(description = "상태 (항상 FINAL)", example = "FINAL")
    private String status;

    @Schema(description = "일기 날짜", example = "2025-09-20")
    private LocalDate date;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;
}
package emory.emoryserver.aidiary.controller;

import emory.emoryserver.aidiary.dto.FeedbackSaveRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Diary Feedback", description = "사용자 선택형 피드백 저장 API")
@RestController
@RequestMapping("/diary/feedback")
public class FeedbackController {

    @Operation(summary = "피드백 항목 목록 조회", description = "사용자가 선택할 수 있는 피드백 항목 목록 반환합니다.")
    @GetMapping("/options")
    public ResponseEntity<List<String>> getFeedbackOptions() {
        List<String> options = List.of(
                "내 감정과 하루를 잘 반영했어요",
                "내 감정과 하루를 잘 반영하지 못했어요",
                "문맥이 이상해요",
                "말투가 어색해요"
        );
        return ResponseEntity.ok(options);
    }

    @Operation(summary = "일기 선택형 피드백 저장", description = "사용자가 AI 일기에 대한 피드백을 선택하고 저장합니다.")
    @PostMapping("/{diaryId}")
    public void saveFeedback(@PathVariable Long DiaryId,
                             @RequestBody FeedbackSaveRequestDto request) {
        //선택형 피드백 저장 로직
    }
}

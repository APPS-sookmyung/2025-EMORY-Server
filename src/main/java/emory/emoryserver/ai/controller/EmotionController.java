package emory.emoryserver.ai.controller;

import emory.emoryserver.ai.dto.emotion.EmotionAnalyzeRequestDto;
import emory.emoryserver.ai.dto.emotion.EmotionFeedbackRequestDto;
import emory.emoryserver.ai.dto.emotion.EmotionFeedbackResponseDto;
import emory.emoryserver.ai.dto.emotion.EmotionVerifyRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI Emotion", description = "감정 분석 및 감정 진위 판단 API")
@RestController
@RequestMapping("/ai/emotion")
public class EmotionController {

    @Operation(summary = "감정 분석", description = "사용자와 AI의 대화 기반으로 감정을 분석합니다.")
    @PostMapping("/analyze")
    public void analyzeEmotion(@RequestBody EmotionAnalyzeRequestDto request) {
        // 감정 분석 로직
    }

    @Operation(summary = "감정 진위 여부 판단", description = "사용자가 카테고리에서 선택한 감정과 실제 분석된 감정이 일치하는지 판단합니다.")
    @PostMapping("/verify")
    public void verifyEmotion(@RequestBody EmotionVerifyRequestDto request) {

    }

    @Operation(summary = "감정 피드백 메시지 생성", description = "감정 분석 결과와 원인을 바탕으로 위로 또는 피드백 메시지를 생성합니다.")
    @PostMapping("/feedback-message")
    public void generateEmotionFeedbackMessage(@RequestBody EmotionFeedbackRequestDto request) {
        // 감정 위로 메시지 생성 로직
    }
}


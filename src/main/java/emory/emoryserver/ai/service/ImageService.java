package emory.emoryserver.ai.service;

import emory.emoryserver.ai.dto.image.ImageRequestDto;
import emory.emoryserver.ai.dto.image.ImageResponseDto;
import emory.emoryserver.ai.model.ChatLog;
import emory.emoryserver.ai.model.ImageAsset;
import emory.emoryserver.ai.repository.ChatLogRepository;
import emory.emoryserver.ai.repository.ImageAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ChatLogRepository chatLogRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final OpenAIImageService openAIImageService;

    public ImageResponseDto generate(ImageRequestDto req) {
        String sessionId = req.getSessionId();
        String emotion = req.getEmotion();
        String diaryContent = req.getDiaryContent();

        String prompt = buildPrompt(sessionId, emotion, diaryContent);

        String b64 = openAIImageService.generateBase64Png(prompt);

        ImageAsset saved = imageAssetRepository.save(
                ImageAsset.builder()
                        .sessionId(sessionId)
                        .mimeType("image/png")
                        .base64(b64)
                        .prompt(prompt)
                        .createdAt(Instant.now())
                        .build()
        );

        ImageResponseDto dto = new ImageResponseDto();
        dto.setImageUrl("/images/" + saved.getId()); // 프론트는 API_BASE_URL/images/{id}로 가져가면 됨
        dto.setDominantColor(mapEmotionToColor(emotion));
        return dto;
    }

    private String buildPrompt(String sessionId, String emotion, String diaryContent) {
        String summaryText = "";

        if (sessionId != null && !sessionId.isBlank()) {
            // ✅ 너 에러났던 부분: findBySessionId가 아니라 이거임
            List<ChatLog> logs = chatLogRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

            // ✅ 너 에러났던 부분: getContent()가 아니라 getText()
            StringBuilder sb = new StringBuilder();
            for (ChatLog l : logs) {
                String role = (l.getRole() == null) ? "unknown" : l.getRole();
                String text = (l.getText() == null) ? "" : l.getText();
                if (text.length() > 300) text = text.substring(0, 300) + "...";
                sb.append("- ").append(role).append(": ").append(text).append("\n");
            }
            summaryText = sb.toString().trim();
        }

        String base = """
너는 감정 일기 서비스의 이미지 생성 모델이다.
아래 정보를 바탕으로 '하루를 상징하는 한 장의 이미지'를 생성하기 위한 프롬프트를 만든다.

요구:
- 인물 얼굴/실명/특정 장소 등 개인 식별 요소는 피한다.
- 추상적/상징적/감성적 일러스트 느낌.
- 한국어로 묘사하되, 이미지 스타일 키워드는 영어 섞어도 됨 (예: soft pastel, cinematic light).
- 1~2문단, 간결하게.

[감정]
%s

[대화/일기 텍스트]
%s

[추가 일기 내용(옵션)]
%s
""".formatted(
                safe(emotion),
                safe(summaryText),
                safe(diaryContent)
        );

        // 여기서는 "프롬프트 생성"을 다시 LLM에 맡기지 않고,
        // 그대로 image generation prompt로 사용(간단/빠르게).
        return base;
    }

    private String mapEmotionToColor(String emotion) {
        if (emotion == null) return null;
        return switch (emotion.toUpperCase()) {
            case "HAPPY" -> "#FFD166";
            case "SOSO" -> "#BDBDBD";
            case "SAD" -> "#6C8EBF";
            case "ANGRY" -> "#EF476F";
            case "ANXIOUS" -> "#7B6D8D";
            case "THOUGHTFUL" -> "#06D6A0";
            default -> "#BDBDBD";
        };
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}


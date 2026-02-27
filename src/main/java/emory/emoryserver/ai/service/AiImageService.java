package emory.emoryserver.ai.service;

import emory.emoryserver.ai.model.AiGeneratedImage;
import emory.emoryserver.ai.model.ChatLog;
import emory.emoryserver.ai.repository.AiGeneratedImageRepository;
import emory.emoryserver.ai.repository.ChatLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AiImageService {

    private final ChatLogRepository chatLogRepository;
    private final AiGeneratedImageRepository aiGeneratedImageRepository;

    private final OpenAIImagePromptService openAIImagePromptService;
    private final OpenAIImageService openAIImageService;

    /** 컨트롤러에서 사용할 결과 */
    public record ImageResult(String imageId) {}

    /**
     * sessionId로 해당 세션의 대화 로그를 모아서 → 이미지용 프롬프트 생성 → 이미지 생성 → DB 저장 → imageId 반환
     */
    public ImageResult generateImageFromSession(String userId, String sessionId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is blank");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is blank");
        }

        List<ChatLog> logs = chatLogRepository.findBySessionIdAndUserIdOrderByCreatedAtAsc(sessionId, userId);
        if (logs == null || logs.isEmpty()) {
            throw new IllegalArgumentException("chat logs are empty for this sessionId");
        }

        logs = logs.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ChatLog::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        String transcript = buildTranscript(logs);
        if (transcript.isBlank()) {
            throw new IllegalArgumentException("transcript is empty");
        }

        // 1) 대화 → 이미지 프롬프트 생성
        String prompt = openAIImagePromptService.buildImagePrompt(transcript);
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt is blank");
        }

        // 2) 이미지 생성 (base64 + format)
        OpenAIImageService.GeneratedImage generated = openAIImageService.generateImage(prompt);

        // 3) DB 저장
        AiGeneratedImage saved = aiGeneratedImageRepository.save(
                AiGeneratedImage.builder()
                        .userId(userId)
                        .sessionId(sessionId)
                        .prompt(prompt)
                        .format(generated.format())
                        .b64(generated.b64())
                        .createdAt(Instant.now())
                        .build()
        );

        return new ImageResult(saved.getId());
    }

    /** imageId로 저장된 이미지 가져오기(권한 포함) */
    public AiGeneratedImage getImageOrThrow(String userId, String imageId) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is blank");
        if (imageId == null || imageId.isBlank()) throw new IllegalArgumentException("imageId is blank");

        return aiGeneratedImageRepository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new IllegalArgumentException("image not found"));
    }

    private String buildTranscript(List<ChatLog> logs) {
        StringBuilder sb = new StringBuilder();
        for (ChatLog log : logs) {
            if (log == null) continue;
            if (log.getText() == null || log.getText().isBlank()) continue;

            String role = (log.getRole() == null ? "unknown" : log.getRole().trim().toLowerCase());
            switch (role) {
                case "user" -> sb.append("사용자: ");
                case "assistant" -> sb.append("AI: ");
                default -> sb.append(role).append(": ");
            }
            sb.append(log.getText().trim()).append("\n");
        }
        return sb.toString().trim();
    }

    public AiGeneratedImage getImagePublicOrThrow(String imageId) {
        if (imageId == null || imageId.isBlank()) {
            throw new IllegalArgumentException("imageId is blank");
        }
        return aiGeneratedImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("image not found"));
    }
}
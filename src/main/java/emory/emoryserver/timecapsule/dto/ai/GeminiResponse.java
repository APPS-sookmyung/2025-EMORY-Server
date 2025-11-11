package emory.emoryserver.timecapsule.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// Gemini API 응답 본문 (필요한 부분만 파싱)
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiResponse(List<Candidate> candidates) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(Content content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(List<Part> parts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Part(String text) {}

    // 요약 텍스트 추출
    public String getGeneratedText() {
        try {
            return this.candidates.get(0).content().parts().get(0).text().trim();
        } catch (Exception e) {
            return null; // 오류 발생 시 null 반환
        }
    }
}
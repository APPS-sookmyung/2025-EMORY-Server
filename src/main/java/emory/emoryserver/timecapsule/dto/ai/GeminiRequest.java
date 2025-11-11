package emory.emoryserver.timecapsule.dto.ai;

import java.util.List;

// Gemini API 요청 본문
public record GeminiRequest(List<Content> contents) {
    public record Content(List<Part> parts) {}
    public record Part(String text) {}

    // 팩토리 메서드
    public static GeminiRequest fromText(String text) {
        Part part = new Part(text);
        Content content = new Content(List.of(part));
        return new GeminiRequest(List.of(content));
    }
}
package emory.emoryserver.ai.controller;

import emory.emoryserver.ai.dto.chat.ChatSaveRequestDto;
import emory.emoryserver.ai.dto.chat.ChatStartRequestDto;
import emory.emoryserver.ai.dto.chat.ChatStartResponseDto;
import emory.emoryserver.ai.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Chat", description = "AI 실시간 대화(Realtime) 세션 관리/저장 API")
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatSessionService chatSessionService;

    @Operation(summary = "대화 세션 시작", description = "백엔드 세션을 생성하고 sessionId를 반환합니다.")
    @PostMapping("/start")
    public ChatStartResponseDto startSession(@RequestBody ChatStartRequestDto request) {
        String sessionId = chatSessionService.startSession(
                request.getSelectedEmotion(),
                request.getCalendarSummary()
        );
        return ChatStartResponseDto.builder().sessionId(sessionId).build();
    }

    @Operation(summary = "대화 세션 종료(서버 상태만)", description = "WebRTC 연결 종료는 클라이언트가 수행하고, 서버는 세션 상태만 STOPPED로 기록합니다.")
    @PostMapping("/stop")
    public void stopSession(@RequestParam String sessionId) {
        chatSessionService.stopSession(sessionId);
    }

    @Operation(summary = "대화 결과 저장", description = "클라이언트가 모은 transcript(messages)를 MongoDB에 저장하고 세션을 SAVED 처리합니다.")
    @PostMapping("/save")
    public void saveResult(@RequestBody ChatSaveRequestDto request) {
        chatSessionService.saveConversation(request);
    }
}

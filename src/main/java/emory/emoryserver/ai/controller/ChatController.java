package emory.emoryserver.ai.controller;


import emory.emoryserver.ai.dto.chat.ChatMessageRequestDto;
import emory.emoryserver.ai.dto.chat.ChatSaveRequestDto;
import emory.emoryserver.ai.dto.chat.ChatStartRequestDto;
import emory.emoryserver.ai.dto.chat.DiaryGenerateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Chat", description = "AI 실시간 대화 API")
@RestController
@RequestMapping("/ai/chat")
public class ChatController {

    @Operation(summary = "AI 실시간 대화 시작", description = "선택한 감정 및 캘린더 일정을 기반으로 AI와 음성 대화를 시작합니다.")
    @PostMapping("/start")
    public void startSession(@RequestBody ChatStartRequestDto request) {
        //세션 시작 로직
    }

    @Operation(summary = "사용자 메시지 전송", description = "사용자의 음성메시지를 전송해 대화 상태 갱신")
    @PostMapping("/message")
    public void sendMessage(@RequestBody ChatMessageRequestDto request) {
        //메시지 처리 로직
    }

    @Operation(summary = "대화 상태 모니터링", description = "대화 중 상태 전달을 위한 보조 메시지. 예) 타이핑 중 여부 확인")
    @GetMapping("/status")
    public void getStatus(@RequestParam String sessionId) {
        //상태 전달 로직
    }

    @Operation(summary = "AI 실시간 대화 종료")
    @PostMapping("/stop")
    public void stopSession(@RequestParam String sessionId) {
        // 세션 종료 로직
    }
    
    @Operation(summary = "대화 결과 저장")
    @PostMapping("/save")
    public void saveResult(@RequestBody ChatSaveRequestDto request) {
        //결과 저장 로직
        // 저장할 대화 내용 처리
    }

    @Operation(summary = "AI 일기 생성", description = "저장된 대화 내용 기반으로 AI가 일기를 생성합니다.")
    @PostMapping("/generate-diary")
    public void generateDiary(@RequestBody DiaryGenerateRequestDto request) {
        // AI 일기 생성 로직
        // 세션 id로 대화내용 찾아서 일기 생성
    }
}

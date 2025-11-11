package emory.emoryserver.timecapsule.service;

import emory.emoryserver.aidiary.model.AiDiary;
import java.time.LocalDate;
import java.util.List;

public interface TimecapsuleAiGenerator {

    String generateWeeklySummary(List<AiDiary> diaries, LocalDate weekStart, LocalDate weekEnd);
}
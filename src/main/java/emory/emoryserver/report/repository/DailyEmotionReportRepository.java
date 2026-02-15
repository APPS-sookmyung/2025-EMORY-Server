package emory.emoryserver.report.repository;

import emory.emoryserver.report.model.DailyEmotionReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyEmotionReportRepository extends MongoRepository<DailyEmotionReport, String> {
    Optional<DailyEmotionReport> findByUserIdAndDate(String userId, LocalDate date);
}

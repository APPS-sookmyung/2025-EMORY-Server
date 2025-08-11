package emory.emoryserver.aidiary.exception;

public class DiaryNotFoundException extends RuntimeException {
    public DiaryNotFoundException(String diaryId) {
        super("Diary not found with id " + diaryId);
    }
}

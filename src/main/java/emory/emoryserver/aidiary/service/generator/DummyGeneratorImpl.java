package emory.emoryserver.aidiary.service.generator;

import org.springframework.stereotype.Service;

/**
 * AiDiaryService가 필요로 하는 DiaryContentGenerator의 임시(Dummy) 구현체.
 * 이 빈(Bean)이 없으면 AiDiaryService가 생성되지 않아 서버 시작이 실패함.
 */
@Service
public class DummyGeneratorImpl implements DiaryContentGenerator {

}

package com.back;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.domain.wiseSaying.repository.WiseSayingDbRepository;
import com.back.global.AppConfig;
import com.back.standard.dto.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

// MySQL 저장·재시작·빌드 테스트. AppTestRunner.run() 을 두 번 부르면 "프로그램 재시작" 과 같다.
// 테스트 DB(wise_saying_test)를 쓰고, 매 테스트 전에 TRUNCATE 한다.
class WiseSayingDbPersistenceTest {
    @BeforeEach
    void beforeEach() {
        AppTestRunner.clearTestDb();
    }

    @Test
    @DisplayName("테스트는 개발 DB 가 아닌 테스트 DB 를 쓴다")
    void t1() {
        assertThat(AppConfig.isTestMode()).isTrue();
        assertThat(AppConfig.getDbName()).isEqualTo("wise_saying_test");
        assertThat(AppConfig.getJdbcUrl()).contains("/wise_saying_test?");
    }

    @Test
    @DisplayName("등록하면 DB 에 저장된다")
    void t2() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """);

        WiseSaying saved = AppTestRunner.newTestDbRepository().findById(1).orElseThrow();
        assertThat(saved.getContent()).isEqualTo("현재를 사랑하라.");
        assertThat(saved.getAuthor()).isEqualTo("작자미상");
    }

    @Test
    @DisplayName("재시작해도 DB 에서 명언을 읽어 최신순 목록에 보여 준다")
    void t3() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                종료
                """);

        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out)
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .contains("1 / 작자미상 / 현재를 사랑하라.");
        assertThat(out.indexOf("2 / 작자미상"))
                .isLessThan(out.indexOf("1 / 작자미상"));
    }

    @Test
    @DisplayName("수정하면 DB 가 갱신되고, 재시작 후에도 수정된 내용이 보인다")
    void t4() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                수정?id=1
                현재와 자신을 사랑하라.
                홍길동
                종료
                """);

        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out).contains("1 / 홍길동 / 현재와 자신을 사랑하라.");
    }

    @Test
    @DisplayName("삭제하면 DB 에서 지워지고, 재시작 후 목록에서도 빠진다")
    void t5() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                삭제?id=1
                종료
                """);

        assertThat(AppTestRunner.newTestDbRepository().findById(1)).isEmpty();

        String out = AppTestRunner.run("""
                목록
                삭제?id=1
                종료
                """);

        assertThat(out)
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.")
                .contains("1번 명언은 존재하지 않습니다.");
    }

    @Test
    @DisplayName("재시작 후 다음 번호가 이어지고, 삭제한 번호는 재사용하지 않는다")
    void t6() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                삭제?id=2
                종료
                """);

        String out = AppTestRunner.run("""
                등록
                미래를 꿈꿔라.
                작자미상
                종료
                """);

        assertThat(out).contains("3번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("빌드: 모든 명언을 배열 형태의 data.json 으로 만들고 안내를 출력한다")
    void t7() throws IOException {
        String out = AppTestRunner.run("""
                등록
                명언 1
                작가 1
                등록
                명언 2
                작가 2
                빌드
                종료
                """);

        assertThat(out).contains("data.json 파일의 내용이 갱신되었습니다.");
        assertThat(Files.readString(AppTestRunner.testDbPath("data.json"), StandardCharsets.UTF_8)).isEqualTo("""
                [
                  {
                    "id": 1,
                    "content": "명언 1",
                    "author": "작가 1"
                  },
                  {
                    "id": 2,
                    "content": "명언 2",
                    "author": "작가 2"
                  }
                ]""");
    }

    @Test
    @DisplayName("따옴표·역슬래시가 들어간 명언도 그대로 저장하고 읽는다")
    void t8() {
        AppTestRunner.run("""
                등록
                그는 "괜찮다" 라고 말했다 \\ 끝
                작자미상
                종료
                """);

        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out).contains("1 / 작자미상 / 그는 \"괜찮다\" 라고 말했다 \\ 끝");
    }

    @Test
    @DisplayName("검색·페이징: SQL(LIKE, ORDER BY id DESC, LIMIT/OFFSET)로 처리한 결과")
    void t9() {
        WiseSayingDbRepository repository = AppTestRunner.newTestDbRepository();
        for (int i = 1; i <= 12; i++) {
            repository.save(new WiseSaying("명언 " + i, i % 2 == 0 ? "짝수작가" : "홀수작가"));
        }

        Page<WiseSaying> page = repository.findForListDesc("author", "짝수", 2, 5);

        assertThat(page.getTotalCount()).isEqualTo(6);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).extracting(WiseSaying::getId).containsExactly(2);
    }

    @Test
    @DisplayName("검색어의 % 와 _ 는 글자 그대로 찾는다")
    void t10() {
        WiseSayingDbRepository repository = AppTestRunner.newTestDbRepository();
        repository.save(new WiseSaying("100% 노력", "작자미상"));
        repository.save(new WiseSaying("노력", "작자미상"));
        repository.save(new WiseSaying("a_b", "작자미상"));
        repository.save(new WiseSaying("axb", "작자미상"));

        assertThat(repository.findForListDesc("content", "%", 1, 5).getContent())
                .extracting(WiseSaying::getContent).containsExactly("100% 노력");
        assertThat(repository.findForListDesc("content", "_", 1, 5).getContent())
                .extracting(WiseSaying::getContent).containsExactly("a_b");
    }
}

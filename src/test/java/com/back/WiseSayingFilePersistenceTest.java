package com.back;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

// 파일 저장·재시작·빌드 테스트. AppTestRunner.run() 을 두 번 부르면 "프로그램 재시작" 과 같다.
class WiseSayingFilePersistenceTest {
    @BeforeEach
    void beforeEach() {
        AppTestRunner.clearTestDb();
    }

    private static String read(String fileName) throws IOException {
        return Files.readString(AppTestRunner.testDbPath(fileName), StandardCharsets.UTF_8);
    }

    private static boolean exists(String fileName) {
        return Files.exists(AppTestRunner.testDbPath(fileName));
    }

    @Test
    @DisplayName("테스트는 실제 db/ 가 아닌 테스트 전용 폴더를 쓴다")
    void t1() {
        assertThat(AppTestRunner.testDbPath("1.json").normalize().toString().replace('\\', '/'))
                .isEqualTo("db_test/wiseSaying/1.json");
    }

    @Test
    @DisplayName("등록하면 db/wiseSaying/{번호}.json 과 lastId.txt 가 생긴다")
    void t2() throws IOException {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """);

        assertThat(read("1.json")).isEqualTo("""
                {
                  "id": 1,
                  "content": "현재를 사랑하라.",
                  "author": "작자미상"
                }""");
        assertThat(read("lastId.txt").trim()).isEqualTo("1");
    }

    @Test
    @DisplayName("재시작해도 파일에서 명언을 읽어 목록에 보여 준다")
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
    @DisplayName("수정하면 파일이 갱신되고, 재시작 후에도 수정된 내용이 보인다")
    void t4() throws IOException {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                수정?id=1
                현재와 자신을 사랑하라.
                홍길동
                종료
                """);

        assertThat(read("1.json"))
                .contains("\"content\": \"현재와 자신을 사랑하라.\"")
                .contains("\"author\": \"홍길동\"");

        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out).contains("1 / 홍길동 / 현재와 자신을 사랑하라.");
    }

    @Test
    @DisplayName("삭제하면 파일도 지워지고, 재시작 후 목록에서도 빠진다")
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

        assertThat(exists("1.json")).isFalse();
        assertThat(exists("2.json")).isTrue();

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
    void t6() throws IOException {
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

        assertThat(read("lastId.txt").trim()).isEqualTo("2");

        String out = AppTestRunner.run("""
                등록
                미래를 꿈꿔라.
                작자미상
                종료
                """);

        assertThat(out).contains("3번 명언이 등록되었습니다.");
        assertThat(exists("2.json")).isFalse();
        assertThat(exists("3.json")).isTrue();
        assertThat(read("lastId.txt").trim()).isEqualTo("3");
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
        assertThat(read("data.json")).isEqualTo("""
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
    @DisplayName("실제 db/ 폴더에는 아무것도 쓰지 않는다")
    void t9() {
        Path realFile = Path.of("db/wiseSaying/lastId.txt");
        boolean existedBefore = Files.exists(realFile);

        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """);

        assertThat(Files.exists(realFile)).isEqualTo(existedBefore);
    }
}

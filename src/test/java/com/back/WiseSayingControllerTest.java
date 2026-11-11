package com.back;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// 테스트는 db_test/ 폴더를 쓰고, 매 테스트 전에 비운다.
// 그래서 테스트 실행 순서와 상관없이 항상 1번부터 시작한다.
class WiseSayingControllerTest {
    @BeforeEach
    void beforeEach() {
        AppTestRunner.clearTestDb();
    }

    @Test
    @DisplayName("종료")
    void t1() {
        String out = AppTestRunner.run("""
                종료
                """);

        assertThat(out)
                .contains("== 명언 앱 ==")
                .contains("명령) ")
                .contains("프로그램 종료");
    }

    @Test
    @DisplayName("명령을 여러 번 입력할 수 있다")
    void t2() {
        String out = AppTestRunner.run("""
                목록
                목록
                종료
                """);

        long count = out.split("명령\\) ", -1).length - 1;
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("등록")
    void t3() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """);

        assertThat(out).contains("명언 :", "작가 :", "1번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("등록할 때마다 번호가 1씩 증가한다")
    void t4() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                종료
                """);

        assertThat(out)
                .contains("1번 명언이 등록되었습니다.")
                .contains("2번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("목록: 최신순으로 보여준다")
    void t5() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                목록
                종료
                """);

        assertThat(out)
                .contains("번호 / 작가 / 명언")
                .contains("----------------------")
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .contains("1 / 작자미상 / 현재를 사랑하라.");

        assertThat(out.indexOf("2 / 작자미상 / 과거에 집착하지 마라."))
                .isLessThan(out.indexOf("1 / 작자미상 / 현재를 사랑하라."));
    }

    @Test
    @DisplayName("삭제?id=1")
    void t6() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                삭제?id=1
                목록
                종료
                """);

        assertThat(out)
                .contains("1번 명언이 삭제되었습니다.")
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("없는 번호 삭제: 존재하지 않는다고 안내한다")
    void t7() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                삭제?id=1
                삭제?id=1
                삭제?id=3
                종료
                """);

        assertThat(out)
                .contains("1번 명언이 삭제되었습니다.")
                .contains("1번 명언은 존재하지 않습니다.")
                .contains("3번 명언은 존재하지 않습니다.");
    }

    @Test
    @DisplayName("수정?id=2")
    void t8() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                수정?id=2
                현재와 자신을 사랑하라.
                홍길동
                목록
                종료
                """);

        assertThat(out)
                .contains("명언(기존) : 과거에 집착하지 마라.")
                .contains("작가(기존) : 작자미상")
                .contains("2 / 홍길동 / 현재와 자신을 사랑하라.")
                .doesNotContain("2 / 작자미상 / 과거에 집착하지 마라.");
    }

    @Test
    @DisplayName("없는 번호 수정: 존재하지 않는다고 안내한다")
    void t9() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                수정?id=3
                목록
                종료
                """);

        assertThat(out)
                .contains("3번 명언은 존재하지 않습니다.")
                .doesNotContain("명언(기존)")
                .contains("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("저장 폴더를 비우면 번호가 1부터 다시 시작한다")
    void t10() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """);

        AppTestRunner.clearTestDb();

        String out = AppTestRunner.run("""
                등록
                과거에 집착하지 마라.
                작자미상
                목록
                종료
                """);

        assertThat(out)
                .contains("1번 명언이 등록되었습니다.")
                .doesNotContain("현재를 사랑하라.");
    }
}

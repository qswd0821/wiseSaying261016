package com.back;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 샘플 명언 10개("명언 1 / 작자미상 1" ~ "명언 10 / 작자미상 10")로 시작하는 검색·페이징 테스트
class WiseSayingSearchPagingTest {
    @BeforeEach
    void beforeEach() {
        AppTestRunner.clearTestDb();
    }

    private static List<String> lines(String out) {
        return Arrays.stream(out.split("\\R"))
                .map(line -> line.replace("명령) ", "").trim())
                .toList();
    }

    @Test
    @DisplayName("앱 시작 시 명언이 0개면 샘플 명언 10개를 만든다")
    void t1() {
        String out = AppTestRunner.runWithSampleData("""
                목록?page=1
                목록?page=2
                종료
                """);

        List<String> lines = lines(out);
        for (int i = 1; i <= 10; i++) {
            assertThat(lines).contains("%d / 작자미상 %d / 명언 %d".formatted(i, i, i));
        }
    }

    @Test
    @DisplayName("목록: page 를 생략하면 1페이지, 최신순 5개(10~6번)")
    void t2() {
        String out = AppTestRunner.runWithSampleData("""
                목록
                종료
                """);

        List<String> lines = lines(out);
        assertThat(lines).containsSubsequence(
                "번호 / 작가 / 명언",
                "----------------------",
                "10 / 작자미상 10 / 명언 10",
                "9 / 작자미상 9 / 명언 9",
                "8 / 작자미상 8 / 명언 8",
                "7 / 작자미상 7 / 명언 7",
                "6 / 작자미상 6 / 명언 6",
                "----------------------",
                "페이지 : [1] / 2"
        );
        assertThat(lines).doesNotContain("5 / 작자미상 5 / 명언 5");
    }

    @Test
    @DisplayName("목록?page=2: 5~1번")
    void t3() {
        String out = AppTestRunner.runWithSampleData("""
                목록?page=2
                종료
                """);

        List<String> lines = lines(out);
        assertThat(lines).containsSubsequence(
                "5 / 작자미상 5 / 명언 5",
                "4 / 작자미상 4 / 명언 4",
                "3 / 작자미상 3 / 명언 3",
                "2 / 작자미상 2 / 명언 2",
                "1 / 작자미상 1 / 명언 1",
                "----------------------",
                "페이지 : 1 / [2]"
        );
        assertThat(lines).doesNotContain("6 / 작자미상 6 / 명언 6");
    }

    @Test
    @DisplayName("목록?keywordType=content&keyword=1: 내용에 1이 들어간 10번·1번")
    void t4() {
        String out = AppTestRunner.runWithSampleData("""
                목록?keywordType=content&keyword=1
                종료
                """);

        List<String> lines = lines(out);
        assertThat(lines).containsSubsequence(
                "----------------------",
                "검색타입 : content",
                "검색어 : 1",
                "----------------------",
                "번호 / 작가 / 명언",
                "----------------------",
                "10 / 작자미상 10 / 명언 10",
                "1 / 작자미상 1 / 명언 1",
                "----------------------",
                "페이지 : [1]"
        );
        assertThat(lines).doesNotContain("2 / 작자미상 2 / 명언 2", "9 / 작자미상 9 / 명언 9");
    }

    @Test
    @DisplayName("목록?keywordType=author&keyword=작자: 작가 검색")
    void t5() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                홍길동
                목록?keywordType=author&keyword=작자
                종료
                """);

        List<String> lines = lines(out);
        assertThat(lines).containsSubsequence(
                "검색타입 : author",
                "검색어 : 작자",
                "1 / 작자미상 / 현재를 사랑하라."
        );
        assertThat(lines).doesNotContain("2 / 홍길동 / 과거에 집착하지 마라.");
    }

    @Test
    @DisplayName("목록?keywordType=content&keyword=과거: 명세 예시")
    void t6() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                목록?keywordType=content&keyword=과거
                종료
                """);

        List<String> lines = lines(out);
        assertThat(lines).containsSubsequence(
                "----------------------",
                "검색타입 : content",
                "검색어 : 과거",
                "----------------------",
                "번호 / 작가 / 명언",
                "----------------------",
                "2 / 작자미상 / 과거에 집착하지 마라."
        );
        assertThat(lines).doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("검색 결과를 기준으로 페이징한다")
    void t7() {
        // 샘플 10개 + "검색대상" 이 들어간 명언 7개(11~17번) → 검색 결과 7개 = 2페이지
        StringBuilder input = new StringBuilder();
        for (int i = 1; i <= 7; i++) {
            input.append("등록\n검색대상 ").append(i).append("\n홍길동\n");
        }
        input.append("목록?keywordType=content&keyword=검색대상&page=2\n");
        input.append("종료\n");

        String out = AppTestRunner.runWithSampleData(input.toString());

        List<String> lines = lines(out);
        assertThat(lines).containsSubsequence(
                "검색타입 : content",
                "검색어 : 검색대상",
                "12 / 홍길동 / 검색대상 2",
                "11 / 홍길동 / 검색대상 1",
                "----------------------",
                "페이지 : 1 / [2]"
        );
        assertThat(lines).doesNotContain("13 / 홍길동 / 검색대상 3", "10 / 작자미상 10 / 명언 10");
    }

    @Test
    @DisplayName("샘플 없이 시작하면 저장소가 비어 있다")
    void t8() {
        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out).doesNotContain("명언 1");
        assertThat(lines(out)).contains("페이지 : [1]");
    }
}

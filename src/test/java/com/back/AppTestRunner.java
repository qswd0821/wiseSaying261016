package com.back;

import com.back.standard.util.TestUtil;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Consumer;

public final class AppTestRunner {
    public static String run(String input, Consumer<Scanner> runApp) {
        var bytes = TestUtil.setOutToByteArray();
        try (Scanner scanner = TestUtil.genScanner(input)) {
            runApp.accept(scanner);
            System.out.flush();
            return bytes.toString(StandardCharsets.UTF_8);
        } finally {
            TestUtil.clearSetOutToByteArray();
        }
    }

    // CRUD 테스트용: 샘플 없이 빈 저장소로 시작한다.
    public static String run(String input) {
        return run(input, scanner -> new App(scanner, false).run());
    }

    // 검색·페이징 테스트용: 샘플 명언 10개를 만들고 시작한다.
    public static String runWithSampleData(String input) {
        return run(input, scanner -> new App(scanner, true).run());
    }
}

package com.back;

import com.back.domain.wiseSaying.repository.WiseSayingDbRepository;
import com.back.global.AppConfig;
import com.back.standard.util.TestUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class AppTestRunner {
    static {
        // 테스트는 실제 db/ 가 아닌 db_test/ 를 쓴다.
        AppConfig.setTestMode();
    }

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

    // CRUD 테스트용: 샘플 없이 시작한다.
    public static String run(String input) {
        return run(input, scanner -> new App(scanner, false).run());
    }

    // 검색·페이징 테스트용: 저장소가 비어 있으면 샘플 명언 10개를 만들고 시작한다.
    public static String runWithSampleData(String input) {
        return run(input, scanner -> new App(scanner, true).run());
    }

    // 테스트 DB 의 wise_saying 테이블을 TRUNCATE 하고(번호도 1로 리셋), 테스트 출력 폴더도 지운다.
    // 매 테스트 전에 호출한다.
    public static void clearTestDb() {
        newTestDbRepository().truncate();
        deleteTestDir();
    }

    public static WiseSayingDbRepository newTestDbRepository() {
        if (!AppConfig.isTestMode()) {
            throw new IllegalStateException("테스트 모드가 아닙니다.");
        }

        return new WiseSayingDbRepository(
                AppConfig.getJdbcUrl(),
                AppConfig.getDbUsername(),
                AppConfig.getDbPassword(),
                AppConfig.getDbDirPath()
        );
    }

    private static void deleteTestDir() {
        Path dir = Path.of(AppConfig.getDbDirPath());

        if (!Files.exists(dir)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Path testDbPath(String fileName) {
        return Path.of(AppConfig.getDbDirPath(), fileName);
    }
}

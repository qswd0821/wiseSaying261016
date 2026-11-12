package com.back.global;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

// 실행 환경별 설정.
// - 테스트는 setTestMode() 로 테스트 DB(wise_saying_test)와 테스트 폴더(db_test/)를 쓴다.
// - DB 접속 정보는 소스에 두지 않고 환경변수 → config/db.properties 순서로 읽는다.
public final class AppConfig {
    private static final Path DB_PROPERTIES_PATH = Path.of("config/db.properties");

    private static String mode = "prod";
    private static Properties dbProperties;

    private AppConfig() {
    }

    public static void setProdMode() {
        mode = "prod";
    }

    public static void setTestMode() {
        mode = "test";
    }

    public static boolean isTestMode() {
        return mode.equals("test");
    }

    // 파일 출력용 폴더 (data.json 빌드 결과 등). prod: db/wiseSaying, test: db_test/wiseSaying
    public static String getDbDirPath() {
        return isTestMode() ? "db_test/wiseSaying" : "db/wiseSaying";
    }

    // 개발 DB 와 테스트 DB 를 분리한다.
    public static String getDbName() {
        return isTestMode() ? "wise_saying_test" : "wise_saying_dev";
    }

    public static String getJdbcUrl() {
        return "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
                .formatted(get("DB_HOST", "db.host", "127.0.0.1"), get("DB_PORT", "db.port", "3306"), getDbName());
    }

    public static String getDbUsername() {
        return get("DB_USERNAME", "db.username", "root");
    }

    public static String getDbPassword() {
        return get("DB_PASSWORD", "db.password", "");
    }

    private static String get(String envName, String propertyName, String defaultValue) {
        String env = System.getenv(envName);

        if (env != null && !env.isBlank()) {
            return env;
        }

        return loadDbProperties().getProperty(propertyName, defaultValue);
    }

    private static synchronized Properties loadDbProperties() {
        if (dbProperties != null) {
            return dbProperties;
        }

        dbProperties = new Properties();

        if (Files.exists(DB_PROPERTIES_PATH)) {
            try (Reader reader = Files.newBufferedReader(DB_PROPERTIES_PATH, StandardCharsets.UTF_8)) {
                dbProperties.load(reader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return dbProperties;
    }
}

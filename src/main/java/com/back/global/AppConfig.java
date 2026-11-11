package com.back.global;

// 실행 환경별 설정. 테스트는 setTestMode() 로 실제 db/ 대신 테스트 전용 폴더를 쓴다.
public final class AppConfig {
    private static String mode = "prod";

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

    // prod: db/wiseSaying, test: db_test/wiseSaying
    public static String getDbDirPath() {
        return isTestMode() ? "db_test/wiseSaying" : "db/wiseSaying";
    }
}

package com.back.global;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.domain.wiseSaying.repository.WiseSayingDbRepository;
import com.back.domain.wiseSaying.repository.WiseSayingRepository;
import com.back.domain.wiseSaying.service.WiseSayingService;

import java.util.Scanner;

// 공유 객체를 한 곳에서 하나씩만 만들어 둔다.
public class AppContext {
    public static Scanner scanner;
    public static WiseSayingRepository wiseSayingRepository;
    public static WiseSayingService wiseSayingService;
    public static WiseSayingController wiseSayingController;
    public static SystemController systemController;

    public static void init(Scanner scanner) {
        AppContext.scanner = scanner;
        // 리포지터리 구현체를 여기서만 고른다.
        //   메모리: new WiseSayingMemoryRepository()
        //   파일:   new WiseSayingFileRepository(AppConfig.getDbDirPath())
        //   MySQL:  아래
        wiseSayingRepository = new WiseSayingDbRepository(
                AppConfig.getJdbcUrl(),
                AppConfig.getDbUsername(),
                AppConfig.getDbPassword(),
                AppConfig.getDbDirPath()
        );
        wiseSayingService = new WiseSayingService(wiseSayingRepository);
        wiseSayingController = new WiseSayingController(scanner, wiseSayingService);
        systemController = new SystemController();
    }
}

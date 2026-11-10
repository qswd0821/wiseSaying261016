package com.back.global;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.WiseSayingController;
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
        wiseSayingRepository = new WiseSayingRepository();
        wiseSayingService = new WiseSayingService(wiseSayingRepository);
        wiseSayingController = new WiseSayingController(scanner, wiseSayingService);
        systemController = new SystemController();
    }

    public static void init() {
        init(new Scanner(System.in));
    }
}

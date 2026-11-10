package com.back;

import com.back.global.AppContext;

public class Main {
    public static void main(String[] args) {
        AppContext.init();
        new App().run();
    }
}

package com.back;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class App {
    private Scanner scanner = new Scanner(System.in);
    private int lastId = 0;
    private List<WiseSaying> wiseSayings = new ArrayList<>();

    public void run() {
        System.out.println("== 명언 앱 ==");

        while (true) {
            System.out.print("명령) ");
            String cmd = scanner.nextLine().trim();

            if (cmd.equals("종료")) {
                break;
            } else if (cmd.equals("목록")) {
                actionList();
            } else if (cmd.equals("등록")) {
                actionWrite();
            } else if (cmd.startsWith("삭제")) {
                actionDelete(cmd);
            } else if (cmd.startsWith("수정")) {
                actionModify(cmd);
            }
        }

        scanner.close();
    }

    private void actionList() {
        System.out.println("번호 / 작가 / 명언");
        System.out.println("----------------------");

        List<WiseSaying> forListWiseSayings = findForList();

        for (WiseSaying wiseSaying : forListWiseSayings) {
            System.out.printf("%d / %s / %s\n", wiseSaying.getId(), wiseSaying.getAuthor(), wiseSaying.getContent());
        }
    }

    private void actionWrite() {
        System.out.print("명언 : ");
        String content = scanner.nextLine().trim();
        System.out.print("작가 : ");
        String author = scanner.nextLine().trim();

        WiseSaying wiseSaying = write(content, author);

        System.out.println("%d번 명언이 등록되었습니다.".formatted(wiseSaying.getId()));
    }

    private void actionDelete(String cmd) {
        String[] cmdBits = cmd.split("=", 2);

        if (cmdBits.length < 2 || cmdBits[1].isEmpty()) {
            System.out.println("id를 입력해주세요.");
            return;
        }

        int id = Integer.parseInt(cmdBits[1]);

        if (delete(id)){
            System.out.println("%d번 명언이 삭제되었습니다.".formatted(id));
        } else {
            System.out.println("%d번 명언은 존재하지 않습니다".formatted(id));
        }
    }

    private void actionModify(String cmd) {
        String[] cmdBits = cmd.split("=", 2);

        if (cmdBits.length < 2 || cmdBits[1].isEmpty()) {
            System.out.println("id를 입력해주세요.");
            return;
        }

        int id = Integer.parseInt(cmdBits[1]);

        if (modify(id)){
            System.out.println("%d번 명언이 수정되었습니다.".formatted(id));
        } else {
            System.out.println("%d번 명언은 존재하지 않습니다".formatted(id));
        }
    }

    private int getSize() { return wiseSayings.size(); }

    private List<WiseSaying> findForList() {
        List<WiseSaying> forListWiseSayings = new ArrayList<>();

        for (int i = wiseSayings.size() - 1; i >= 0; i--) {
            forListWiseSayings.add(wiseSayings.get(i));
        }

        return forListWiseSayings;
    }

    private WiseSaying write(String content, String author) {
        WiseSaying wiseSaying = new WiseSaying(++lastId, content, author);
        wiseSayings.add(wiseSaying);
        return wiseSaying;
    }

    private boolean delete(int id) {
        int deleteIndex = findIndexById(id);

        if (deleteIndex == -1) {
            return false;
        }
        /*for (int i = deleteIndex + 1; i <= wiseSayingsLastIndex; i++) {
            wiseSayings[i - 1] = wiseSayings[i];
        }

        wiseSayings[wiseSayingsLastIndex] = null;
        wiseSayingsLastIndex--;*/
        wiseSayings.remove(deleteIndex);
        return true;
    }

    private boolean modify(int id) {
        int modifyIndex = findIndexById(id);

        if (modifyIndex == -1) {
            return false;
        }
        WiseSaying wiseSaying = wiseSayings.get(modifyIndex);
        System.out.println("명언(기존): %s".formatted(wiseSaying.getContent()));
        System.out.print("명언 : ");
        String content = scanner.nextLine().trim();
        System.out.println("작가(기존): %s".formatted(wiseSaying.getAuthor()));
        System.out.print("작가 : ");
        String author = scanner.nextLine().trim();

        wiseSaying.setContent(content);
        wiseSaying.setAuthor(author);

        return true;
    }

    private int findIndexById(int id){
        for (int i = 0; i < wiseSayings.size(); i++) {
            if (wiseSayings.get(i).getId() == id) {
                return i;
            }
        }        return -1;
    }
}
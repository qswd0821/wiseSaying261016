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
                Rq rq = new Rq(cmd);

                switch ( rq.getActionName() ){
                    case "종료":
                        System.out.println("프로그램 종료");
                        return;
                    case "목록":
                        actionList();
                        break;
                    case "등록":
                        actionWrite();
                        break;
                    case "삭제":
                        actionDelete(rq);
                        break;
                    case "수정":
                        actionModify(rq);
                        break;
                }
            }
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

        private void actionDelete(Rq rq) {
            int id = rq.getParamAsInt("id", -1);

            if (id == -1) {
                System.out.println("id를 숫자로 입력해주세요.");
                return;
            }

            boolean deleted = delete(id);

            if (!deleted) {
                System.out.println("%d번 명언은 존재하지 않습니다.".formatted(id));
                return;
            }

            System.out.println("%d번 명언이 삭제되었습니다.".formatted(id));
        }

        private void actionModify(Rq rq) {
            int id = rq.getParamAsInt("id", -1);

            if (id == -1) {
                System.out.println("id를 숫자로 입력해주세요.");
                return;
            }

            WiseSaying wiseSaying = findById(id);

            if (wiseSaying == null) {
                System.out.println("%d번 명언은 존재하지 않습니다.".formatted(id));
                return;
            }

            System.out.printf("명언(기존) : %s\n", wiseSaying.getContent());
            System.out.print("명언 : ");
            String content = scanner.nextLine().trim();

            System.out.printf("작가(기존) : %s\n", wiseSaying.getAuthor());
            System.out.print("작가 : ");
            String author = scanner.nextLine().trim();

            modify(wiseSaying, content, author);
        }

        private void modify(WiseSaying wiseSaying, String content, String author) {
            wiseSaying.setContent(content);
            wiseSaying.setAuthor(author);
        }

        private List<WiseSaying> findForList() {
            return wiseSayings.reversed();
        }

        private WiseSaying write(String content, String author) {
            WiseSaying wiseSaying = new WiseSaying(++lastId, content, author);
            wiseSayings.add(wiseSaying);
            return wiseSaying;
        }

        private boolean delete(int id) {
            return wiseSayings.removeIf(w -> w.getId() == id);
        }

        private boolean modify(int id) {
            WiseSaying wiseSaying = findById(id);

            if (wiseSaying == null) {
                return false;
            }

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

        private WiseSaying findById(int id) {
            return wiseSayings.stream()
                    .filter(w -> w.getId() == id)
                    .findFirst()
                    .orElse(null);
        }
    }
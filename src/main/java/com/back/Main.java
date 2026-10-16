package com.back;
import java.util.Scanner;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==명언 앱==");

        int lastid = 0;

        WiseSaying[] wiseSayings= new WiseSaying[100];
        int wiseSayingsLastIndex = -1;

        while(true) {
            System.out.print("명령: ");
            String cmd = scanner.nextLine().trim();

            if(cmd.equals("종료")){
                break;
            }
            else if(cmd.equals("목록")){
                System.out.println("번호 / 작가 / 명언");
                System.out.println("------------------");

                for (int i = wiseSayingsLastIndex; i>=0; i--){
                    WiseSaying wiseSaying = wiseSayings[i];

                    System.out.printf("%d / %s / %s\n", wiseSaying.id, wiseSaying.author, wiseSaying.content);
                }
            }
            else if(cmd.equals("등록")) {
                System.out.print("명언: ");
                String wiseSayingcontent = scanner.nextLine().trim();
                System.out.print("작자: ");
                String wiseSayingauthor = scanner.nextLine().trim();

                int id = ++lastid;

                WiseSaying wiseSaying = new WiseSaying();
                wiseSaying.id = id;
                wiseSaying.content = wiseSayingcontent;
                wiseSaying.author = wiseSayingauthor;
                wiseSayings[++wiseSayingsLastIndex] = wiseSaying;


                System.out.println("%d 번째 명언이 등록되었습니다.".formatted(id));
            }
        }
        scanner.close();
    }
}

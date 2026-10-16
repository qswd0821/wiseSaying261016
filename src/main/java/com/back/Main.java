package com.back;
import java.util.Scanner;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        Scanner scanner = new Scanner(System.in); // 2. 객체 생성
        System.out.println("==명언 앱==");
        int lastid = 0;
        while(true) {
            System.out.print("명령: ");
            String cmd = scanner.nextLine().trim();

            int id = lastid++;

            if(cmd.equals("종료")){
                break;
            }

            else if(cmd.equals("등록")) {
                System.out.print("명언: ");
                String wiseSaying = scanner.nextLine().trim();
                System.out.print("작자: ");
                String author = scanner.nextLine().trim();
                System.out.println("명언 등록.");

                System.out.println(lastid + " 번째 명언이 등록되었습니다");

            }

            else{
                System.out.println("등록/종료를 입력하십시오");
            }
        }
    }
}

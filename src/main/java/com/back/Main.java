package com.back;
import java.util.Scanner;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        Scanner scanner = new Scanner(System.in); // 2. 객체 생성
        System.out.println("==wiseSaying app ==");
        while(true) {
            System.out.print("command: ");
            String cmd = scanner.nextLine().trim();

            if(cmd.equals("exit")){
                break;
            }

            else if(cmd.equals("register")) {
                System.out.print("wiseSaying: ");
                String wiseSaying = scanner.nextLine().trim();
                System.out.print("authoer: ");
                String author = scanner.nextLine().trim();
                System.out.print("command registered.");

                System.out.println("1 wiseSaying registered.");
            }

            else{
                System.out.println("register or exit");
            }
        }
    }
}

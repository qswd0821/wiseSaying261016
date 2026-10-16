package com.back;
import java.util.Scanner;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        Scanner scanner = new Scanner(System.in); // 2. 객체 생성
        System.out.println("==command app ==");
        System.out.println("register");
        System.out.print("command: ");
        String myeong = scanner.next();
        System.out.print("authoer: ");
        String author = scanner.next();
        System.out.print("command registered.");

        System.out.println("exit");

    }
}

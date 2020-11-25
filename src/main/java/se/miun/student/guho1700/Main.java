package se.miun.student.guho1700;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        RestServer server = new RestServer(9999);
        server.start();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Press any key to stop");

        scanner.nextLine();

        server.shutDown();

        System.out.println(" - Exiting -");
    }

}

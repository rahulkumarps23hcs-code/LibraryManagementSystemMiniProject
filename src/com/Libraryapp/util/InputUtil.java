package com.Libraryapp.util;

import java.util.Scanner;

public class InputUtil {

    private static final Scanner SC = new Scanner(System.in);

    private InputUtil() {
    }

    public static String readString(String prompt) {
        System.out.print(prompt);
        return SC.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readString(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}

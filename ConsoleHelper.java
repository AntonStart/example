package com.javarush.task.task30.task3008;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
// этот класс облегчает работу с потоками ввода вывода
public class ConsoleHelper {
    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    // просто вывод текста в консоль
    public static void writeMessage(String message) {
        System.out.println(message);
    }

    // просто чтение строки
    public static String readString() throws IOException {
        String s = null;
        while (true) {
            try {
                return bufferedReader.readLine();
            } catch (IOException e) {
                System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            }
        }
    }
    // просто чтение числа
    public static int readInt() throws IOException{
        try {
                return Integer.parseInt(readString());
        } catch (NumberFormatException e) {
            System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            return Integer.parseInt(readString());
        }
    }
}

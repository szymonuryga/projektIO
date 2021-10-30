package io;

import java.util.Scanner;

public class DataReader {
    private ConsolePrinter printer;
    private Scanner input = new Scanner(System.in);

    public DataReader(ConsolePrinter printer) {
        this.printer = printer;
    }

    public void close() {
        input.close();
    }

    public int getInt() {
        try {
            return input.nextInt();
        } finally {
            input.nextLine();
        }
    }

    public double getDouble() {
        try {
            return input.nextDouble();
        } finally {
            input.nextLine();
        }
    }

    public String getString() {
        return input.nextLine();
    }
}

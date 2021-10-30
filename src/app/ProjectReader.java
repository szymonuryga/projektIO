package app;

import exception.NoSuchOptionException;
import io.ConsolePrinter;
import io.DataReader;

import java.util.InputMismatchException;

public class ProjectReader {
    private ConsolePrinter printer = new ConsolePrinter();
    private DataReader dataReader = new DataReader(printer);

    String FileChooserLoop() {
        Option option;
        String fileName = "C:\\Users\\szymo\\Desktop\\Workspace\\projektIO\\";
        printOption();
        option = getOption();
        switch (option) {
            case FILE_DANE1:
                fileName += "Dane1.xlsx";
                break;
            case FILE_DANE2:
                fileName += "Dane2.xlsx";
                break;
            case FILE_DANE3:
                fileName += "Dane3.xlsx";
                break;
            case EXIT:
                exit();
                break;
            default:
                printer.printLine("Wprowadzona wartość nie jest poprawna, spróbuj ponownie: ");
        }

        return fileName;
    }

    private void exit() {
        printer.printLine("Zamykam program ");
        dataReader.close();
    }

    private Option getOption() {
        boolean optionOk = false;
        Option option = null;
        while (!optionOk) {
            try {
                option = Option.createFromInt(dataReader.getInt());
                optionOk = true;
            } catch (NoSuchOptionException e) {
                printer.printLine(e.getMessage() + ", podaj ponownie:");
            } catch (InputMismatchException ignored) {
                printer.printLine("Wprowadzono wartość, która nie jest liczbą, podaj ponownie:");
            }
        }
        return option;
    }

    private void printOption() {
        for (Option options : Option.values()) {
            printer.printLine(options.toString());
        }
    }

    private enum Option {
        EXIT(0, "Wyjście z programu"),
        FILE_DANE1(1, "Dane1 -  50 zadań na 10 maszynach"),
        FILE_DANE2(2, "Dane2 - 100 zadań na 20 maszynach"),
        FILE_DANE3(3, "Dane3 - 200 zadań na 20 maszynach");


        private int value;
        private String description;

        Option(int value, String description) {
            this.value = value;
            this.description = description;
        }

        @Override
        public String toString() {
            return value + " - " + description;
        }

        static Option createFromInt(int option) throws NoSuchOptionException {
            try {
                return Option.values()[option];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new NoSuchOptionException("Brak opcji o id " + option);
            }
        }
    }
}

package model;

import java.util.*;

//Klasa pomocnicza Data, która umożliwia zpisywanie kolejności zadań i termin zakończenia ostatniego zadania
public class Data {
    public int[] Tab = new int[1000];
    private int suma;

    public Data() {
        setSuma(0);
    }

    public Data(int[] tab, int suma) {
        this.Tab = tab;
        setSuma(suma);
    }

    public int getSuma() {
        return suma;
    }

    public void setSuma(int suma) {
        this.suma = suma;
    }

    public static DataComparer sort ;

}

package model;

import java.util.Comparator;

public class DataComparer implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Data d1 = (Data)o1;
        Data d2 = (Data)o2;

        if (d1 != null && d2 != null) {
            if (d2.getSuma() == d1.getSuma())
                return 0;
            else if (d1.getSuma() > d2.getSuma())
                return 1;
            else
                return -1;
        }
        else
            throw new IllegalArgumentException("Parametr nie jest osobnikiem!");
    }
}

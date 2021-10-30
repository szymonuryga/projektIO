package app;

import exception.NoSuchOptionException;
import io.ConsolePrinter;
import io.DataReader;
import io.file.CsvFileManager;
import model.Data;
import model.Tabu;

import java.util.*;

public class ProjectControl {
    public ConsolePrinter printer = new ConsolePrinter();
    public DataReader dataReader = new DataReader(printer);
    ProjectReader projectReader = new ProjectReader();

    void ControlLoop() {
        String fileName = projectReader.FileChooserLoop();
        CsvFileManager excell = new CsvFileManager(fileName);

        int [][] data = excell.importData();
        int rowsize = data.length;
        int colsize = data[0].length;
        Option option;
        int powtorzenia = 10;
        int nrozwiazan = 10;
        double mutacje = 0.1;
        int wariant = 0;
        int[] przedzial = new int[2];
        przedzial[0] = przedzial[1] = 0;
        Data result = new Data();
        do {
            printOption();
            option = getOption();
            if (option.value != 0 && option.value != 2) {
                printer.printLine("Podaj liczbę pokoleń/powtórzeń:");
                powtorzenia = dataReader.getInt();
            }
            if (option.value > 2) {
                printer.printLine("Podaj liczbę rozwiązań do analizy (musi być parzysta):");
                nrozwiazan = dataReader.getInt();
                printer.printLine("Podaj prawdopodobieństwo mutacji (liczba zmiennoprzecinkowa od 0 do 1):");
                mutacje = dataReader.getDouble();
                printer.printLine("Podaj przedział (dwie liczby całkowite od 0 do ):" + nrozwiazan);
                przedzial[0] = dataReader.getInt();
                printer.printLine("Do :");
                przedzial[1] = dataReader.getInt();
                printer.printLine("Wybierz sposób krzyżowania wybierając 1 lub 2");
                wariant = dataReader.getInt();
            }
            Random rnd = new Random();
            Random rdouble = new Random();
            switch (option) {
                case ALGORITHM_NEH:
                    result = Neh(data, rowsize, colsize);
                    break;
                case ALGORITHM_TABU:
                    result = TabuSearch(data, rowsize, colsize, rnd, powtorzenia);
                    break;
                case ALGORITHM_WYZARZANIA:
                    result = Wyzarzanie(data, rowsize, colsize, rnd, rdouble, powtorzenia);
                    break;
                case ALGORITHM_WSPINACZKI:
                    result = Wspinaczka(data, rowsize, colsize, rnd, powtorzenia);
                    break;
                case ALGORITHM_LINIOWY:
                    result = RankingLiniowy(data, rowsize, colsize, rnd, rdouble, powtorzenia, nrozwiazan, mutacje, przedzial, wariant);
                    break;
                case ALGORITHM_RULETKA:
                    result = Ruletka(data, rowsize, colsize, rnd, rdouble, powtorzenia, nrozwiazan, mutacje, przedzial, wariant);
                    break;
                case ALGORITHM_TURNIEJ:
                    result = Turniej(data, rowsize, colsize, rnd, powtorzenia, nrozwiazan, mutacje, przedzial, wariant);
                    break;
                case EXIT:
                    exit();
                    break;
                default:
                    printer.printLine("Wprowadzona wartość nie jest poprawna, spróbuj ponownie: ");
            }
        } while (option != Option.EXIT);
        System.out.println(result);
    }

    static Data FindMin(List<Data> grupa, int glosowa)//Znajdowanie najmniejszej wartości z badanego ugrupowania
    {
        Data min = grupa.get(0);
        for (int i = 1; i < glosowa; i++) {
            if (min.getSuma() > grupa.get(i).getSuma())
                min = grupa.get(i);
        }
        return min;
    }

    static int Findmax(List<Tabu> tab)//Funkcja pomocnicza do TabuSearch znajdująca najgorszy element listy top najlepszych rozwiązań
    {
        int iter = 0;
        int max = tab.get(0).getCounter();
        for (int i = 1; i < 5; i++) {
            if (max < tab.get(i).getCounter()) {
                max = tab.get(i).getCounter();
                iter = i;
            }
        }
        return iter;
    }

    static Tabu FindBest(List<Tabu> tab) {
        Tabu min = tab.get(0);
        for (int i = 1; i < tab.size(); i++) {
            if (min.getCounter() > tab.get(i).getCounter())
                min = tab.get(i);
        }
        return min;
    }

    static int[] Swap(int[] Tab, int indeks) {
        int a = Tab[indeks];
        Tab[indeks] = Tab[indeks + 1];
        Tab[indeks + 1] = a;

        return Tab;
    }

    //Funkcja zliczająca czas ostatniego zadania na ostatniej maszynie
    static Data Zlicz(int[] next, int[][] data, int rowsize, int colsize) {
        Data result = new Data();
        int[][] dane = new int[rowsize][colsize];
        int[][] pomoc = new int[rowsize][colsize];
        for (int i = 0; i < rowsize; i++)//Przepisywanie wartości do pomocniczej tablicy oraz tablicy wynikowej
        {
            result.Tab[i] = dane[i][0] = next[i];
            for (int j = 0; j < colsize; j++) {
                pomoc[i][j] = data[next[i] - 1][j];
            }
        }
        for (int i = 0; i < rowsize; i++)//Zliczanie czasu zakończeń kolejnych zadań
        {
            for (int e = 1; e < colsize; e++) {
                if (i == 0) {
                    if (e == 1)
                        dane[i][e] = pomoc[i][e];
                    else
                        dane[i][e] = dane[i][e - 1] + pomoc[i][e];
                } else {
                    if (e == 1)
                        dane[i][e] = dane[i - 1][e] + pomoc[i][e];
                    else {
                        if (dane[i][e - 1] > dane[i - 1][e]) {
                            dane[i][e] = dane[i][e - 1] + pomoc[i][e];
                        } else {
                            dane[i][e] = dane[i - 1][e] + pomoc[i][e];
                        }
                    }
                }
            }
            //Console.WriteLine(dane[i, 0] + " " + dane[i, 1] + " " + dane[i, 2] + " " + dane[i, 3] + " " + dane[i, 4] + " " + dane[i, 5] + " " + dane[i, 6] + " " + dane[i, 7]);
        }

        result.setSuma(dane[rowsize - 1][colsize - 1]);//Zapisanie ostatniego zakończenia do wyniku

        return result;
    }

    static Data Losowanie(int[][] data, int rowsize, int colsize, Random rnd)//Losowanie rozwiązania
    {
        Data result;
        int[][] dane = new int[rowsize][colsize];
        int[] next = new int[rowsize];
        int zamiana;
        List<Integer> mieszalnik = new ArrayList<>();
        List<Integer> lista = new ArrayList<>();
        //Tworzenie poszczególnych ustawień dla n-rozwiązań
        for (int i = 0; i < rowsize; i++)
            lista.add(i);
        for (int i = 0; i < rowsize; i++) {
            zamiana = rnd.nextInt(lista.size());
            mieszalnik.add(lista.get(zamiana));
            lista.remove(zamiana);
        }
        for (int i = 0; i < rowsize; i++) {
            for (int j = 0; j < colsize; j++) {
                next[mieszalnik.get(i)] = data[i][0];
            }
        }
        result = Zlicz(next, data, rowsize, colsize);//Zliczanie sumy dla losowanego ustawienia
        System.out.println(result.getSuma());
        return result;
    }

    //Tworzenie dzieci z dwóch najlepszych osobników lub przejście rodziców do pokolenia dzieci w razie mutacji - Krzyżowanie dwupunktowe
    static Data[] Krzyzowanie1(Data a, Data b, int[] przedzial, int rozmiar, int colsize, double mutacje, int[][] data, Random rdouble, Random rand) {
        Data[] dzieci = new Data[2];
        List<Integer> lista1 = new ArrayList<>();
        List<Integer> lista2 = new ArrayList<>();
        int k;
        int i;
        int numer;

        dzieci[0] = new Data();
        dzieci[1] = new Data();
        //Tworzenie 1. dziecka
        for (i = przedzial[1]; i < rozmiar; i++)//Trzeci przedział osobnika B
            lista1.add(b.Tab[i]);
        for (i = 0; i < przedzial[1]; i++)//Pierwszy i drugi przedział osobnika B
            lista2.add(b.Tab[i]);
        for (i = przedzial[0]; i < przedzial[1]; i++)//Przepisanie wartości drugiego przedziału osobnika A do 1. dziecka
        {
            dzieci[0].Tab[i] = a.Tab[i];
            if (lista1.size() != 0 && lista1.contains(a.Tab[i]))//Usunięcie powtarzających się wartości dziecka i trzeciego przedziału osobnika B
                lista1.remove(a.Tab[i]);
            else if (lista2.size() != 0 && lista2.contains(a.Tab[i]))//Usunięcie powtarzających się wartości dziecka i pierwszego przedziału osobnika B
                lista2.remove(a.Tab[i]);
        }
        k = lista1.size();
        for (i = przedzial[1]; i < przedzial[1] + k; i++)//Przepisania wartości trzeciego przedziału osobnika B do 1. dziecka
        {
            numer = rand.nextInt(lista1.size());
            dzieci[0].Tab[i] = lista1.get(numer);
            lista1.remove(numer);
        }
        for (i = przedzial[1] + k; i < rozmiar; i++)//Przepisania wartości pierwszego i drugiego przedziału osobnika B do 1. dziecka
        {
            numer = rand.nextInt(lista2.size());
            dzieci[0].Tab[i] = lista2.get(numer);
            lista2.remove(numer);
        }
        for (i = 0; i < przedzial[0]; i++)//Przepisania wartości pierwszego i drugiego przedziału osobnika B do 1. dziecka
        {
            numer = rand.nextInt(lista2.size());
            dzieci[0].Tab[i] = lista2.get(numer);
            lista2.remove(numer);
        }
        //Tworzenie 2. dziecka
        for (i = przedzial[1]; i < rozmiar; i++)//Trzeci przedział osobnika A
            lista1.add(a.Tab[i]);
        for (i = 0; i < przedzial[1]; i++)//Pierwszy i drugi przedział osobnika A
            lista2.add(a.Tab[i]);
        for (i = przedzial[0]; i < przedzial[1]; i++)//Przepisanie wartości drugiego przedziału osobnika B do 2. dziecka
        {
            dzieci[1].Tab[i] = b.Tab[i];
            if (lista1.size() != 0 && lista1.contains(b.Tab[i]))//Usunięcie powtarzających się wartości dziecka i trzeciego przedziału osobnika A
                lista1.remove(b.Tab[i]);
            else if (lista2.size() != 0 && lista2.contains(b.Tab[i]))//Usunięcie powtarzających się wartości dziecka i pierwszego przedziału osobnika A
                lista2.remove(b.Tab[i]);
        }
        k = lista1.size();
        for (i = przedzial[1]; i < przedzial[1] + k; i++)//Przepisania wartości trzeciego przedziału osobnika A do 2. dziecka
        {
            numer = rand.nextInt(lista1.size());
            dzieci[1].Tab[i] = lista1.get(numer);
            lista1.remove(numer);
        }
        for (i = przedzial[1] + k; i < rozmiar; i++)//Przepisania wartości pierwszego i drugiego przedziału osobnika A do 2. dziecka
        {
            numer = rand.nextInt(lista2.size());
            dzieci[1].Tab[i] = lista2.get(numer);
            lista2.remove(numer);
        }
        for (i = 0; i < przedzial[0]; i++)//Przepisania wartości pierwszego i drugiego przedziału osobnika A do 2. dziecka
        {
            numer = rand.nextInt(lista2.size());
            dzieci[1].Tab[i] = lista2.get(numer);
            lista2.remove(numer);
        }

        numer = 0;
        if (rdouble.nextDouble() > mutacje)//Jeżeli liczba losowa jest większa niż badana wartość współczynnika mutacji, to ta nie nastąpi
        {
            for (i = 0; i < rozmiar; i++) {
                if (dzieci[0].Tab[i] == dzieci[1].Tab[i])
                    numer++;
            }
            if (numer == rozmiar - 2)//Jeżeli mutacja następuje, a losowa jest większa od współczynnika mutacji to rodzice przechodzą od razu pokolenia dzieci
            {
                dzieci[0] = a;
                dzieci[1] = b;
            } else {
                dzieci[0] = Zlicz(dzieci[0].Tab, data, rozmiar, colsize);
                dzieci[1] = Zlicz(dzieci[1].Tab, data, rozmiar, colsize);
            }
        } else {
            dzieci[0] = Zlicz(dzieci[0].Tab, data, rozmiar, colsize);
            dzieci[1] = Zlicz(dzieci[1].Tab, data, rozmiar, colsize);
        }

        return dzieci;
    }

    //Krzyżowanie jednorodne
    static Data[] Krzyzowanie2(Data a, Data b, int rozmiar, int colsize, double mutacje, int[][] data, Random rdouble, Random rand) {
        Data[] dzieci = new Data[2];
        List<Integer> lista1 = new ArrayList<>();
        List<Integer> lista2 = new ArrayList<>();
        int i;
        int numer;

        dzieci[0] = new Data();
        dzieci[1] = new Data();
        //Tworzenie 1. dziecka
        for (i = 0; i < rozmiar; i++)//Data A i B
        {
            lista1.add(a.Tab[i]);
            lista2.add(b.Tab[i]);
        }
        for (i = 0; i < rozmiar; i++) {
            numer = rand.nextInt(100);
            if (numer > 50) {
                if (lista1.contains(a.Tab[i])) {
                    dzieci[0].Tab[i] = a.Tab[i];
                    lista1.remove(a.Tab[i]);
                    lista2.remove(a.Tab[i]);
                } else if (lista2.contains(b.Tab[i])) {
                    dzieci[0].Tab[i] = b.Tab[i];
                    lista1.remove(b.Tab[i]);
                    lista2.remove(b.Tab[i]);
                } else {
                    dzieci[0].Tab[i] = a.Tab[0];
                    lista1.remove(a.Tab[0]);
                    lista2.remove(a.Tab[0]);
                }
            } else {
                if (lista2.contains(b.Tab[i])) {
                    dzieci[0].Tab[i] = b.Tab[i];
                    lista1.remove(b.Tab[i]);
                    lista2.remove(b.Tab[i]);
                } else if (lista2.contains(a.Tab[i])) {
                    dzieci[0].Tab[i] = a.Tab[i];
                    lista1.remove(a.Tab[i]);
                    lista2.remove(a.Tab[i]);
                } else {
                    dzieci[0].Tab[i] = b.Tab[0];
                    lista1.remove(b.Tab[0]);
                    lista2.remove(b.Tab[0]);
                }
            }
        }
        //Tworzenie 2. dziecka
        for (i = 0; i < rozmiar; i++)//Data A i B
        {
            lista1.add(a.Tab[i]);
            lista2.add(b.Tab[i]);
        }
        for (i = 0; i < rozmiar; i++) {
            numer = rand.nextInt(100);
            if (numer > 50) {
                if (lista1.contains(a.Tab[i])) {
                    dzieci[1].Tab[i] = a.Tab[i];
                    lista1.remove(a.Tab[i]);
                    lista2.remove(a.Tab[i]);
                } else if (lista2.contains(b.Tab[i])) {
                    dzieci[1].Tab[i] = b.Tab[i];
                    lista1.remove(b.Tab[i]);
                    lista2.remove(b.Tab[i]);
                } else {
                    dzieci[1].Tab[i] = a.Tab[0];
                    lista1.remove(a.Tab[0]);
                    lista2.remove(a.Tab[0]);
                }
            } else {
                if (lista2.contains(b.Tab[i])) {
                    dzieci[1].Tab[i] = b.Tab[i];
                    lista1.remove(b.Tab[i]);
                    lista2.remove(b.Tab[i]);
                } else if (lista2.contains(a.Tab[i])) {
                    dzieci[1].Tab[i] = a.Tab[i];
                    lista1.remove(a.Tab[i]);
                    lista2.remove(a.Tab[i]);
                } else {
                    dzieci[1].Tab[i] = b.Tab[0];
                    lista1.remove(b.Tab[0]);
                    lista2.remove(b.Tab[0]);
                }
            }
        }

        numer = 0;
        if (rdouble.nextDouble() > mutacje)//Jeżeli liczba losowa jest większa niż badana wartość współczynnika mutacji, to ta nie nastąpi
        {
            for (i = 0; i < rozmiar; i++) {
                if (dzieci[0].Tab[i] == dzieci[1].Tab[i])
                    numer++;
            }
            if (numer == rozmiar - 2)//Jeżeli mutacja następuje, a losowa jest większa od współczynnika mutacji to rodzice przechodzą od razu pokolenia dzieci
            {
                dzieci[0] = a;
                dzieci[1] = b;
            } else {
                dzieci[0] = Zlicz(dzieci[0].Tab, data, rozmiar, colsize);
                dzieci[1] = Zlicz(dzieci[1].Tab, data, rozmiar, colsize);
            }
        } else {
            dzieci[0] = Zlicz(dzieci[0].Tab, data, rozmiar, colsize);
            dzieci[1] = Zlicz(dzieci[1].Tab, data, rozmiar, colsize);
        }

        return dzieci;
    }


    static Data RankingLiniowy(int[][] dane, int rowsize, int colsize, Random rnd, Random rdouble, int powtorzenia, int nrozwiazan, double mutacje, int[] przedzial, int wariant) {
        Random rand = new Random();
        List<Data> wyniki = new ArrayList<>();
        List<Data> LosowiOsobnicy;
        Data result;
        Data[] pomocnicy = new Data[2];
        List<Integer> mieszalnik = new ArrayList<>();
        pomocnicy[0] = pomocnicy[1] = new Data();
        int glosowa = (int) (nrozwiazan * 0.3);//Ugrupowanie, z którego wybierany jest najlepszy wynik ma wielkość 30% liczby rozwiązań

        Data[] osobnicy = new Data[nrozwiazan];
        Data[] osobnicyPomoc = new Data[nrozwiazan];
        //Losowanie początkowych ustawień zadań dla n-rozwiązań
        for (int j = 0; j < nrozwiazan; j++) {
            osobnicy[j] = Losowanie(dane, rowsize, colsize, rnd);
            //System.out.println(" " + osobnicy[j].suma);
        }

        int rozmiarRuletki;//wielkość pojedynczeko wycinka ruletki
        double losowa;
        int znacznik;
        int mnoznik;
        int g;
        List<Integer> lista = new ArrayList<Integer>();
        List<Double> ruletkaList = new ArrayList<Double>();
        lista = new ArrayList<Integer>();

        for (int i = 0; i < powtorzenia; i++) {
            for (int j = 0; j < nrozwiazan; j += 2) {
                Arrays.sort(osobnicy, Data.sort);//Sortowanie tablicy osobników
                rozmiarRuletki = 0;
                for (int k = 0; k < nrozwiazan; k++) {
                    lista.add(k);
                    rozmiarRuletki += (k + 1);
                }
                ruletkaList.add(0.0);
                for (int k = 0; k < nrozwiazan; k++)//Nadawanie wielkości poszczególnym kawałkom ruletki
                {
                    losowa = k + 1.0;
                    losowa /= rozmiarRuletki;
                    losowa += ruletkaList.get(k);
                    ruletkaList.add(losowa);
                }

                LosowiOsobnicy = new ArrayList<Data>();
                for (int k = 0; k < glosowa; )//Losowanie chromosomów
                {
                    losowa = rdouble.nextDouble();//Liczba losowa
                    g = 0;
                    while (losowa > ruletkaList.get(g))
                        g++;
                    g--;
                    if (lista.contains(g))//Dodawanie elementów ugrupowania do zbioru liczb poddawanych analizie
                    {
                        lista.remove(g);
                        LosowiOsobnicy.add(osobnicy[g]);
                        k++;
                    }
                }

                pomocnicy[0] = FindMin(LosowiOsobnicy, glosowa);//Wybór pierwszego najlepszego wyniku
                LosowiOsobnicy.remove(pomocnicy[0]);
                pomocnicy[1] = FindMin(LosowiOsobnicy, glosowa - 1);//Wybór drugiego najlepszego wyniku
                //System.out.println("Rodzic 1: " + pomocnicy[0].suma);
                //System.out.println("Rodzic 2: " + pomocnicy[1].suma);

                if (wariant == 0)
                    pomocnicy = Krzyzowanie1(pomocnicy[0], pomocnicy[1], przedzial, rowsize, colsize, mutacje, dane, rdouble, rand);//Tworzenie dzieci z wybranych rodziców
                else
                    pomocnicy = Krzyzowanie2(pomocnicy[0], pomocnicy[1], rowsize, colsize, mutacje, dane, rdouble, rand);
                osobnicyPomoc[j] = pomocnicy[0];
                osobnicyPomoc[j + 1] = pomocnicy[1];

                //System.out.println("Dziecko " + j + ": "+ osobnicyPomoc[j].suma+" " + osobnicy[j].suma);
                //System.out.println("Dziecko " + j + 1 + ": " + osobnicyPomoc[j+1].suma + " " + osobnicy[j + 1].suma);

            }
            osobnicy = osobnicyPomoc;
            System.out.println("\nKolejne pokolenie");
            for (int j = 0; j < nrozwiazan; j++) {
                System.out.println(osobnicy[j].getSuma());
            }
        }
        for (int j = 0; j < nrozwiazan; j++) {
            wyniki.add(osobnicy[j]);
        }
        result = FindMin(wyniki, nrozwiazan);
        System.out.println("Wynik końcowy: " + result.getSuma());

        return result;
    }

    //(data, rowsize, colsize,rnd);
    //(int[][ data, int rowsize, int colsize, Random rnd)
    static Data Wspinaczka(int[][] data, int rowsize, int colsize, Random rnd, int powtorzenia) {
        int j = 0;
        Data dane = new Data();
        Data next = new Data();
        int zamiana = 0;
        int pomocnik = 0;
        dane = Losowanie(data, rowsize, colsize, rnd);//Losowanie rozwiązania początkowego i obliczenie czasu zakończenia ostatniego zadania

        for (int h = 0; h < powtorzenia; h++)//Zwiększenie wartości h polepszy końcowy wynik
        {
            //Losowanie dwóch indeksów do zamiany
            int i1 = rnd.nextInt(rowsize);
            int i2 = rnd.nextInt(rowsize);
            //Zamiana indeksów w nowej tablicy
            next = dane;
            pomocnik = next.Tab[i1];
            next.Tab[i1] = next.Tab[i2];
            next.Tab[i2] = pomocnik;
            next = Zlicz(next.Tab, data, rowsize, colsize);//Liczenie nowej wartości czasu zakończenia ostatniego zadania

            System.out.println("Iteracja: " + h + " Poprzednia suma: " + dane.getSuma() + " Obecna suma: " + next.getSuma() + " Liczba zamian: " + zamiana);
            if (dane.getSuma() > next.getSuma())//Jeżeli stara wartość sumy odchyleń jest większa od obecnej to następuje zamiana
            {
                dane = next;
                zamiana++;
            }
        }
        System.out.println("Wynik końcowy: " + dane.getSuma());
        return dane;
    }

    static Data Wyzarzanie(int[][] data, int rowsize, int colsize, Random rnd, Random rdouble, int powtorzenia) {
        Scanner scanner = new Scanner(System.in);
        String odp;
        int j = 0;
        Data next = new Data();
        Data dane = new Data();
        j = 0;
        double proba = 0.0;
        double alpha = 0.999;
        double temp = 10000000000.0; //Zmieniając te wartości
        double ep = 0.000001; //można dojść do polepszenia wyniku
        int delta;
        int pomocnik = 0;
        dane = Losowanie(data, rowsize, colsize, rnd);//Losowanie rozwiązania początkowego i czasu zakończenia ostatniego zadania

        System.out.println("Podaj wartość alpha:");
        alpha = scanner.nextDouble();
        System.out.println("Podaj wartość temperatury:");
        temp = scanner.nextDouble();
        System.out.println("Podaj wartość epsilon:");
        ep = scanner.nextDouble();

        while (temp > ep)//Dopóki temperatura jest większa od episilon, będą wykonywane kolejne iteracje polepszania wyniku
        {
            next = dane;//przypisywanie wartości z pierwotnej tablicy do nowej tablicy
            //Losowanie dwóch indeksów do zamiany
            int i1 = rnd.nextInt(rowsize);
            int i2 = rnd.nextInt(rowsize);
            //Zamiana indeksów w nowej tablicy
            pomocnik = next.Tab[i1];
            next.Tab[i1] = next.Tab[i2];
            next.Tab[i2] = pomocnik;
            next = Zlicz(next.Tab, data, rowsize, colsize);//Liczenie nowej wartości sumy odchyleń

            delta = next.getSuma() - dane.getSuma();//Liczenie delty dla obecnego rozwiązania
            System.out.println("Poprzednia suma: " + dane.getSuma() + " Obecna suma: " + next.getSuma() + " delta: " + delta);
            if (delta < 0)//Jeżeli stara wartość sumy odchyleń jest większa od obecnej to następuje zamiana
            {
                dane = next;
            } else {
                proba = rdouble.nextDouble();//W przeciwnym przypadku zamiana następuje z prawdopodobienstwem exp(-delta/temp))
                if (proba < Math.exp(-delta / temp)) {
                    dane = next;
                }
            }
            temp *= alpha;//Następuje zmiana temperatury
        }
        System.out.println("Wynik końcowy: " + dane.getSuma());
        return dane;
    }

    static Data TabuSearch(int[][] data, int rowsize, int colsize, Random rnd, int powtorzenia) {
        String odp;
        int wielkosc = 0;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Podaj wielkość listy Tabu (całkowita, mniejsza od )" + (rowsize - 1));
        wielkosc = scanner.nextInt();

        int pomocnik = 0;
        int j = 0;
        Tabu t = new Tabu();
        int iterator = 0;
        Data next = new Data();
        Data dane = new Data();
        ArrayDeque<Tabu> lista = new ArrayDeque<Tabu>();
        List<Tabu> top = new ArrayList<Tabu>();
        List<Data> kolejnosc = new ArrayList<Data>();
        Data Opomocnik = new Data();
        //Losowanie rozwiązania początkowego i obliczanie czasu zakończenia ostatniego zadania

        dane = Losowanie(data, rowsize, colsize, rnd);//Żeby wyłączyć losowanie to to komentujesz, a te dwie linijki niżej odkomentowujesz i Ci pójdzie z ustawieniem początkowym 1,2,3...n
        //for (int i = 0; i < rowsize; i++)
        // dane.Tab[i] = data[i, 0];
        dane = Zlicz(dane.Tab, data, rowsize, colsize);
        //Część główna
        for (int h = 0; h < powtorzenia; h++)//Zwiększenie wartości h polepszy końcowy wynik
        { //Usuwanie przedawnionych zamian z listy Tabu (od drugiej iteracji)
            kolejnosc = new ArrayList<Data>();
            boolean outcome = false;
            iterator = 0;
            Tabu result = new Tabu();
            if (h > 0) {
                Tabu pomoc = lista.peek();
                if (pomoc.getCounter() == 0) {
                    lista.removeFirst();
                    pomoc = lista.peekFirst();
                }
            }
            for (int x = 0; x < rowsize; x++)//Analiza poszczególnych przypadków
            {
                for (int y = x + 1; y < rowsize; y++) {
                    //przypisywanie wartości z pierwotnej tablicy do nowej tablicy
                    next = dane;
                    //Zamiana wartości w nowej tablicy dla indeksów x i y
                    pomocnik = dane.Tab[x];
                    next.Tab[x] = dane.Tab[y];
                    next.Tab[y] = pomocnik;
                    //for (int i = 0; i < 67; i++)
                    //System.out.println(next.Tab[i]);

                    next = Zlicz(next.Tab, data, rowsize, colsize);//Liczenie nowej wartości czasu zakończenia ostatniego zadania
                    System.out.println("Iteracja: " + h + " Pierwszy indeks: " + x + " Wynik pośredni: " + next.getSuma() + " " + dane.getSuma());
                    //Tworzenie listy top 5 najlepszych rozwiązań
                    if (iterator < 5)//Najpierw lista uzupełniana jest 5 pierwszymi rozwiązaniami
                    {
                        t = new Tabu(x, y, next.getSuma());
                        Opomocnik = new Data(next.Tab, next.getSuma());
                        kolejnosc.add(Opomocnik);
                        top.add(t);
                        iterator++;
                        System.out.println(t.toString());
                    } else {
                        if (top.get(Findmax(top)).getCounter() > next.getSuma())//Jeżeli obecna suma jest mniejsza od najgorszego rozwiązania z listy top to w miejsce najgorzego wyniku wpisywana jest obecna suma
                        {
                            int p = Findmax(top);
                            t = top.get(p);
                            top.set(p, new Tabu(x, y, next.getSuma()));
                            kolejnosc.set(p, new Data(next.Tab, next.getSuma()));
                            System.out.println(t.toString());
                        }
                        iterator++;
                    }
                }
            }
            int wynik = -1;
            //Wybór rozwiązania
            if (lista.size() > 0) {//Sprawdzenie, czy najlepsze rozwiązanie z top nie pojawiło się na liście tabu
                for (int i = 0; outcome == false || i < 5; i++) {
                    t.setA(top.get(i).getA());
                    t.setB(top.get(i).getB());
                    iterator = 0;
                    for (j = 1; j <= 3; j++) {
                        t.setCounter(j);
                        if (lista.contains(t) == false) {
                            iterator++;
                        }
                    }
                    if (iterator == 3) {//Zapisanie najlepszego dozwolonego wyniku na listę Tabu
                        result = t;
                        result.setCounter(wielkosc + 1);
                        lista.remove(result);
                        outcome = true;
                    }
                    wynik++;
                }
            } else {//Zapisanie najlepszego wyniku na listę Tabu
                result.setA(t.getA());
                result.setB(t.getB());
                result.setCounter(wielkosc + 1);
                System.out.println(t);
                lista.remove(result);
                outcome = true;
                wynik++;
            }
            //Zamiana indeksów w nowej tablicy na wartości z najlepszego wyniku
            next.Tab = kolejnosc.get(wynik).Tab;
            wynik = 0;
            dane = Zlicz(next.Tab, data, rowsize, colsize);//Liczenie nowej wartości sumy odchyleń

            System.out.println("Iteracja: " + h + "Wynik: " + dane.getSuma());
            for (Tabu tb : lista)//Pomniejszanie wartości liczącej ile razy zamiana nie może nastąpić przy poszczególnych indeksach
            {
                tb.setCounter(tb.getCounter() - 1);
            }
        }
        System.out.println("Wynik końcowy: " + dane.getSuma());
        return dane;
    }

    static Data Neh(int[][] data, int rowsize, int colsize) {
        Data pomocnik = new Data();
        Data result = new Data();
        int[][] Tab = new int[rowsize][colsize];
        int[] kolejnosc = new int[rowsize];
        for (int h = 0; h < colsize; h++)//Tablica z pierwszym zadaniem
            Tab[0][h] = data[0][h];
        kolejnosc[0] = data[0][0];

        for (int h = 1; h < rowsize; h++) {
            kolejnosc[h] = data[h][0];//Dodanie kolejnoeg zadania
            for (int j = 0; j < colsize; j++)
                Tab[h][j] = data[h][j];
            result = pomocnik = Zlicz(kolejnosc, data, (h + 1), colsize);//Pierwsze ustawienie i czas zakończenia ostatniego zadania
            System.out.println(h + " " + " Pomocnik: " + pomocnik.getSuma() + " Result: " + result.getSuma());
            for (int i = h - 1; i >= 0; i--) {
                kolejnosc = Swap(kolejnosc, i);//Zamiana kolejności zadań
                pomocnik = Zlicz(kolejnosc, data, (h + 1), colsize);//Czas zakończenia ostatniego zadania dla nowego ustawienia
                if (result.getSuma() > pomocnik.getSuma())//Jeżeli najlepszy dotychczasowy wynik jest większy od obecnie badanego ustawienia to następuje aktualizacja nalepszego ustawienia
                    result = pomocnik;

                System.out.println(h + " " + i + " Pomocnik: " + pomocnik.getSuma() + " Result: " + result.getSuma());
                System.out.println(h + " " + i + " Pomocnik: " + pomocnik.getSuma() + " Result: " + result.getSuma());

            }
            kolejnosc = result.Tab;
        }

        return result;
    }

    static Data Ruletka(int[][] dane, int rowsize, int colsize, Random rnd, Random rdouble, int powtorzenia, int nrozwiazan, double mutacje, int[] przedzial, int wariant) {
        Random rand = new Random();
        List<Data> wyniki = new ArrayList<Data>();
        List<Data> losowiOsobnicy = new ArrayList<Data>();
        Data result = new Data();
        Data[] pomocnicy = new Data[2];
        List<Integer> mieszalnik = new ArrayList<Integer>();
        pomocnicy[0] = pomocnicy[1] = new Data();
        int glosowa = (int) (nrozwiazan * 0.3);//Ugrupowanie, z którego wybierany jest najlepszy wynik ma wielkość 30% liczby rozwiązań

        Data[] osobnicy = new Data[nrozwiazan];
        Data[] osobnicyPomoc = new Data[nrozwiazan];
        //Losowanie początkowych ustawień zadań dla n-rozwiązań
        for (int j = 0; j < nrozwiazan; j++) {
            osobnicy[j] = Losowanie(dane, rowsize, colsize, rnd);
            //System.out.println(osobnicy[j].Tab[0] + " " + osobnicy[j].suma);
        }

        double rozmiarWycinka = 100 / nrozwiazan;//wielkość pojedynczego wycinka ruletki413
        double losowa = 0.0;
        int znacznik;
        int mnoznik;
        List<Integer> lista = new ArrayList<Integer>();
        lista = new ArrayList<Integer>();

        for (int i = 0; i < powtorzenia; i++) {
            for (int j = 0; j < nrozwiazan; j += 2) {
                for (int k = 0; k < nrozwiazan; k++)
                    lista.add(k);
                losowiOsobnicy = new ArrayList<Data>();
                for (int k = 0; k < glosowa; )//Losowanie chromosomów
                {
                    losowa = rdouble.nextDouble() * 100;//Liczba losowa
                    mnoznik = (int) (Math.floor(losowa / rozmiarWycinka));//numer indeksu wycinka ruletki
                    if (lista.contains(mnoznik))//Dodawanie elementów ugrupowania do zbioru liczb poddawanych analizie
                    {
                        lista.remove(mnoznik);
                        losowiOsobnicy.add(osobnicy[mnoznik]);
                        k++;
                    }
                }
                pomocnicy[0] = FindMin(losowiOsobnicy, glosowa);//Wybór pierwszego najlepszego wyniku
                losowiOsobnicy.remove(pomocnicy[0]);
                pomocnicy[1] = FindMin(losowiOsobnicy, glosowa - 1);//Wybór drugiego najlepszego wyniku
                //System.out.println("Rodzic 1: " + pomocnicy[0].suma);
                //System.out.println("Rodzic 2: " + pomocnicy[1].suma);

                if (wariant == 0)
                    pomocnicy = Krzyzowanie1(pomocnicy[0], pomocnicy[1], przedzial, rowsize, colsize, mutacje, dane, rdouble, rand);//Tworzenie dzieci z wybranych rodziców
                else
                    pomocnicy = Krzyzowanie2(pomocnicy[0], pomocnicy[1], rowsize, colsize, mutacje, dane, rdouble, rand);
                osobnicyPomoc[j] = pomocnicy[0];
                osobnicyPomoc[j + 1] = pomocnicy[1];

                //System.out.println("Dziecko " + j + ": "+ osobnicyPomoc[j].suma+" " + osobnicy[j].suma);
                //System.out.println("Dziecko " + j + 1 + ": " + osobnicyPomoc[j+1].suma + " " + osobnicy[j + 1].suma);

            }
            osobnicy = osobnicyPomoc;

            System.out.println("\nKolejne pokolenie");
            for (int j = 0; j < nrozwiazan; j++) {
                System.out.println(osobnicy[j].getSuma());
            }
        }
        for (int j = 0; j < nrozwiazan; j++) {
            wyniki.add(osobnicy[j]);
        }
        result = FindMin(wyniki, nrozwiazan);
        System.out.println("Wynik końcowy: " + result.getSuma());

        return result;
    }

    static Data Turniej(int[][] dane, int rowsize, int colsize, Random rnd, int powtorzenia, int nrozwiazan, double mutacje, int[] przedzial, int wariant) {
        List<Data> wyniki = new ArrayList<>();
        Data result = new Data();
        Random rdouble = new Random();
        Random rand = new Random();
        List<Integer> lista = new ArrayList<Integer>();
        List<Data> LosowiOsobnicy = new ArrayList<Data>();
        int zamiana;
        int glosowa = (int) (nrozwiazan * 0.3);//Ugrupowanie, z którego wybierany jest najlepsy wynik ma wielkość 30% liczby rozwiązań

        Data[] osobnicyPomoc = new Data[nrozwiazan];
        Data[] osobnicy = new Data[nrozwiazan];
        Data[] dzieci = new Data[nrozwiazan];
        Data[] pomocnicy = new Data[2];
        List<Integer> mieszalnik = new ArrayList<Integer>();
        pomocnicy[0] = pomocnicy[1] = new Data();
        //Losowanie początkowych ustawień zadań dla n-rozwiązań
        for (int j = 0; j < nrozwiazan; j++) {
            osobnicy[j] = Losowanie(dane, rowsize, colsize, rnd);
            //System.out.println(osobnicy[j].Tab[0] + " " + osobnicy[j].suma);
        }
        for (int i = 0; i < powtorzenia; i++) {
            for (int j = 0; j < nrozwiazan; j += 2) {
                mieszalnik = new ArrayList<Integer>();
                LosowiOsobnicy = new ArrayList<Data>();
                lista = new ArrayList<Integer>();
                for (int k = 0; k < nrozwiazan; k++)
                    lista.add(k);
                for (int k = 0; k < glosowa; k++)//Losowanie ugrupowania z którego powstaną rodzice
                {
                    zamiana = rnd.nextInt(lista.size());
                    mieszalnik.add(lista.get(zamiana));
                    lista.remove(zamiana);
                    LosowiOsobnicy.add(osobnicy[mieszalnik.get(k)]);
                }
                pomocnicy[0] = FindMin(LosowiOsobnicy, glosowa);//Wybór pierwszego najlepszego wyniku
                LosowiOsobnicy.remove(pomocnicy[0]);
                pomocnicy[1] = FindMin(LosowiOsobnicy, glosowa - 1);//Wybór drugiego najlepszego wyniku
                //System.out.println("Rodzic 1: " + pomocnicy[0].suma);
                //System.out.println("Rodzic 2: " + pomocnicy[1].suma);
                if (wariant == 0)
                    pomocnicy = Krzyzowanie1(pomocnicy[0], pomocnicy[1], przedzial, rowsize, colsize, mutacje, dane, rdouble, rand);//Tworzenie dzieci z wybranych rodziców
                else
                    pomocnicy = Krzyzowanie2(pomocnicy[0], pomocnicy[1], rowsize, colsize, mutacje, dane, rdouble, rand);
                osobnicyPomoc[j] = pomocnicy[0];
                osobnicyPomoc[j + 1] = pomocnicy[1];

                //System.out.println("Dziecko " + j + ": "+ osobnicyPomoc[j].suma+" " + osobnicy[j].suma);
                //System.out.println("Dziecko " + j + 1 + ": " + osobnicyPomoc[j+1].suma + " " + osobnicy[j + 1].suma);
            }
            osobnicy = osobnicyPomoc;
            System.out.println("\nKolejne pokolenie");
            for (int j = 0; j < nrozwiazan; j++) {
                System.out.println(osobnicy[j].getSuma());
            }

        }
        for (int j = 0; j < nrozwiazan; j++) {
            wyniki.add(osobnicy[j]);
        }
        result = FindMin(wyniki, nrozwiazan);
        System.out.println("Wynik końcowy: " + result.getSuma());
        return result;
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
        ALGORITHM_NEH(1, "Algorytm Neh"),
        ALGORITHM_TABU(2, "Algorytm TabuSearch"),
        ALGORITHM_WYZARZANIA(3, "Algorytm Wyżarzania"),
        ALGORITHM_WSPINACZKI(4, "Algorytm Wspinaczki"),
        ALGORITHM_LINIOWY(5, "Algorytm genetyczny: Ranking Liniowy"),
        ALGORITHM_RULETKA(6, "Algorytm genetyczny: Ruletka"),
        ALGORITHM_TURNIEJ(7, "Algorytm genetyczny: Turniej");


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

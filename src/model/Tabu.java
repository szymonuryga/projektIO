package model;

public class Tabu {
    private int a;
    private int b;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    private int counter;

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public Tabu()
    {
        setA(0);
        setB(0);
        setCounter(0);
    }
    public Tabu(int a, int b, int counter)
    {
        setA(a);
        setB(b);
        setCounter(counter);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getA()+" ");
        sb.append(getB() + " ");
        sb.append(getCounter());
        return sb.toString();
    }
}

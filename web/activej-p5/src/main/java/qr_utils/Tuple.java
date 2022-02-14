package qr_utils;

public class Tuple<T, U> {
    public final T x;
    public final U y;

    public Tuple(T x, U y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Tuple tuple = (Tuple) obj;
        if (x.equals(tuple.x) && y.equals(tuple.y)) {
            return true;
        }
        return false;

    }

    public int hashCode() {
        return (Integer) x + (Integer) y;
    }

    public static void main(String[] args) {
        Tuple t1 = new Tuple("123", "234");
        Tuple t2 = new Tuple("123", "235");
        //System.out.println(t1.equals(t2));
    }
}

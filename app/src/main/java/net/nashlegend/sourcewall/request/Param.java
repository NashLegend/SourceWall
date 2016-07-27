package net.nashlegend.sourcewall.request;

/**
 * Created by NashLegend on 16/7/4.
 */

public class Param {

    public final String key;
    public final String value;

    public Param(String key, Object value) {
        this(key, String.valueOf(value));
    }

    public Param(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Param) {
            Param p = (Param) o;
            return equals(p.key, key) && equals(p.value, value);
        }
        return false;
    }

    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    @Override
    public int hashCode() {
        return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
    }

    public static Param create(String a, String b) {
        return new Param(a, b);
    }

    public Param copy() {
        return new Param(key, value);
    }
}

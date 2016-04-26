package net.nashlegend.router;

import android.net.Uri;

import java.util.Map;
import java.util.Set;

public class ExtraTypes {
    public static final int STRING = 0;
    public static final int INT = 1;
    public static final int LONG = 2;
    public static final int BOOL = 3;
    public static final int SHORT = 4;
    public static final int FLOAT = 5;
    public static final int DOUBLE = 6;

    private String[] intExtra;
    private String[] longExtra;
    private String[] boolExtra;
    private String[] shortExtra;
    private String[] floatExtra;
    private String[] doubleExtra;
    private String[] stringExtra;
    private String[] required;
    private Map<String, String> transfer;

    public String[] getStringExtra() {
        return stringExtra;
    }

    public void setStringExtra(String[] stringExtra) {
        this.stringExtra = stringExtra;
    }

    public String[] getIntExtra() {
        return intExtra;
    }

    public void setIntExtra(String[] intExtra) {
        this.intExtra = intExtra;
    }

    public String[] getLongExtra() {
        return longExtra;
    }

    public void setLongExtra(String[] longExtra) {
        this.longExtra = longExtra;
    }

    public String[] getBoolExtra() {
        return boolExtra;
    }

    public void setBoolExtra(String[] boolExtra) {
        this.boolExtra = boolExtra;
    }

    public String[] getShortExtra() {
        return shortExtra;
    }

    public void setShortExtra(String[] shortExtra) {
        this.shortExtra = shortExtra;
    }

    public String[] getFloatExtra() {
        return floatExtra;
    }

    public void setFloatExtra(String[] floatExtra) {
        this.floatExtra = floatExtra;
    }

    public String[] getDoubleExtra() {
        return doubleExtra;
    }

    public void setDoubleExtra(String[] doubleExtra) {
        this.doubleExtra = doubleExtra;
    }

    public Map<String, String> getTransfer() {
        return transfer;
    }

    public void setTransfer(Map<String, String> transfer) {
        this.transfer = transfer;
    }

    public String[] getRequired() {
        return required;
    }

    public void setRequired(String[] required) {
        this.required = required;
    }

    public int getType(String name) {
        if (arrayContain(intExtra, name)) {
            return INT;
        }
        if (arrayContain(longExtra, name)) {
            return LONG;
        }
        if (arrayContain(boolExtra, name)) {
            return BOOL;
        }
        if (arrayContain(shortExtra, name)) {
            return SHORT;
        }
        if (arrayContain(floatExtra, name)) {
            return FLOAT;
        }
        if (arrayContain(doubleExtra, name)) {
            return DOUBLE;
        }
        return STRING;
    }

    private boolean arrayContain(String[] array, String value) {
        if (array == null) {
            return false;
        }
        for (String s : array) {
            if (s.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public String transfer(String name) {
        if (transfer == null) {
            return name;
        }
        String result = transfer.get(name);
        return result != null ? result : name;
    }

    public boolean matchRequired(Uri uri) {
        if (required == null || required.length == 0) {
            return true;
        }
        Set<String> names = uri.getQueryParameterNames();
        for (String s : required) {
            boolean found = false;
            for (String name : names) {
                if (name.equals(s)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }
}

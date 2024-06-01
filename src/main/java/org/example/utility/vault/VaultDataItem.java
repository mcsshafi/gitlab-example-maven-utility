package org.example.utility.vault;

import java.text.DateFormat;
import java.util.Date;

public class VaultDataItem {
    private String _value;

    public VaultDataItem(String value) {
        this._value = value;
    }

    public String getValue() {
        return this._value;
    }
    public boolean getBoolean() {
        boolean v = false;
        if(this._value != null && this._value.isEmpty() == false) {
            String temp = this._value.trim().toLowerCase();
            if(temp.compareToIgnoreCase("y") == 0 || temp.compareToIgnoreCase("a") == 0 ||
                    temp.compareToIgnoreCase("true") == 0 || temp.compareToIgnoreCase("active") == 0) {
                v = true;
            }
        }
        return v;
    }
    public int getInt() {
        int v = 0;
        try { v = Integer.parseInt(this._value); }
        catch(Exception ex) { }
        return v;
    }
    public long getLong() {
        long v = 0;
        try { v = Long.parseLong(this._value); }
        catch(Exception ex) { }
        return v;
    }
    public double getDouble() {
        double v = 0;
        try { v = Double.parseDouble(this._value); }
        catch(Exception ex) { }
        return v;
    }
    public float getFloat() {
        float v = 0;
        try { v = Float.parseFloat(this._value); }
        catch(Exception ex) { }
        return v;
    }
    public Date getDate(DateFormat formatter) {
        Date v = new Date();
        try { v = formatter.parse(this._value); }
        catch(Exception ex) { }
        return v;
    }

    @Override
    public String toString() {
        return this._value;
    }
}

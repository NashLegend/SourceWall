package com.example.outerspace.model;

/**
 * Created by NashLegend on 2014/10/31 0031
 */
public class SubItem extends AceModel {
    private int type;
    private String name;
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

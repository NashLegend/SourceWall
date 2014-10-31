package com.example.outerspace.model;

/**
 * Created by NashLegend on 2014/10/31 0031
 */
public class SubItem extends AceModel {

    public static final int Type_Collections = 0;
    public static final int Type_Single_Channel = 1;
    public static final int Type_Single_Subject = 2;

    public static final int Section_Article = 0;
    public static final int Section_Post = 1;
    public static final int Section_Question = 2;

    private int section;
    private int type;
    private String id = "";
    private String name = "";
    private String value = "";

    public SubItem(int section, int type, String name, String value) {
        setSection(section);
        setType(type);
        setName(name);
        setValue(value);
    }

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

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }
}

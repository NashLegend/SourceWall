package com.example.sourcewall.model;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class Basket extends AceModel {
    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public int getLinks_count() {
        return links_count;
    }

    public void setLinks_count(int links_count) {
        this.links_count = links_count;
    }

    private String category_id = "";
    private String category_name = "";
    private String id = "";
    private String name = "";
    private String introduction = "";
    private int links_count = 0;
}

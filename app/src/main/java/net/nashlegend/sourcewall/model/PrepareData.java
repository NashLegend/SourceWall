package net.nashlegend.sourcewall.model;

import net.nashlegend.sourcewall.request.Param;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2015/1/9 0009
 * 发贴所需数据
 */
public class PrepareData {

    private String csrf = "";
    private ArrayList<Param> pairs = new ArrayList<>();

    public String getCsrf() {
        return csrf;
    }

    public void setCsrf(String csrf) {
        this.csrf = csrf;
    }

    public ArrayList<Param> getPairs() {
        return pairs;
    }

    public void setPairs(ArrayList<Param> pairs) {
        this.pairs = pairs;
    }

}

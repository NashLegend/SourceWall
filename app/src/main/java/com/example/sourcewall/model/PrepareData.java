package com.example.sourcewall.model;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2015/1/9 0009
 * 发帖所需数据
 */
public class PrepareData {

    String csrf = "";
    ArrayList<BasicNameValuePair> pairs = new ArrayList<>();

    public String getCsrf() {
        return csrf;
    }

    public void setCsrf(String csrf) {
        this.csrf = csrf;
    }

    public ArrayList<BasicNameValuePair> getPairs() {
        return pairs;
    }

    public void setPairs(ArrayList<BasicNameValuePair> pairs) {
        this.pairs = pairs;
    }

}

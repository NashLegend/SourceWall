package com.example.sourcewall.util;

import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.APIBase;

/**
 * Created by NashLegend on 2014/12/5 0005
 */
public class MDUtil {

    /**
     * 通过github接口转换markdown，一小时只能60次
     *
     * @param text
     * @return
     */
    public static ResultObject parseMarkdownByGitHub(String text) {
        return APIBase.parseMarkdownByGitHub(text);
    }

    /**
     * @param text
     * @return
     */
    public static ResultObject parseMarkdownInADumbWay(String text) {
        ResultObject resultObject = new ResultObject();
        String[] paragraphs = text.split("\n");
        for (int i = 0; i < paragraphs.length; i++) {
            String content = "<p>"+replaceImage(paragraphs[i])+"</p>";
        }
        return resultObject;
    }

    public static String replaceImage(String image){
        return image.replaceAll("image","url");
    }
}

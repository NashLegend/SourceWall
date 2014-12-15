package com.example.sourcewall.util;

import android.text.Html;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class RegUtil {
    public static String clearHtmlByTag(String content, String tag) {
        return content.replaceAll("<" + tag + ">" + ".*?" + "</" + tag + ">", "");
    }

    public static String clearHtmlBlockQuote(String content) {
        return clearHtmlByTag(content, "blockquote");
    }

    public static String html2PlainText(String content) {
        return Html.fromHtml(content).toString().replaceAll("\n", "");
    }

    public static String html2PlainTextWithoutBlockQuote(String content) {
        return html2PlainText(clearHtmlBlockQuote(content));
    }
}

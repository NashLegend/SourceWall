package net.nashlegend.sourcewall.util;

import android.text.Html;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class RegUtil {

    public static final String OBJ = String.valueOf((char) 65532);

    public static String tryGetStringByLength(String str, int len) {
        if (str.length() > len) {
            return str.substring(0, len) + "...";
        } else {
            return str;
        }
    }

    public static String clearHtmlByTag(String content, String tag) {
        return content.replaceAll("<" + tag + ">" + ".*?" + "</" + tag + ">", "");
    }

    public static String clearHtmlBlockQuote(String content) {
        return clearHtmlByTag(content, "blockquote");
    }

    /**
     * Html转纯文本无格式不带换行，但是保留图片标签
     */
    public static String html2PlainTextWithImageTag(String content) {
        return Html.fromHtml(
                content.replaceAll("<img .*?/>|<img.*?>.*?</img>", "[图片]")).toString().replaceAll(
                OBJ, "[图片]").replaceAll("\n", "");
    }

    /**
     * Html转纯文本无格式不带换行
     */
    public static String html2PlainText(String content) {
        return Html.fromHtml(content).toString().replaceAll(OBJ, "[图片]").replaceAll("\n", "");
    }

    /**
     * Html转纯文本无格式不带换行除去引用块
     */
    public static String html2PlainTextWithoutBlockQuote(String content) {
        return html2PlainText(clearHtmlBlockQuote(content));
    }
}

package com.example.sourcewall.util;

/**
 * Created by NashLegend on 2014/12/15 0015
 */
public class Config {
    public static String getComplexReplyTail() {
        return "<p></p><p>来自<a href=\"http://www.guokr.com/blog/798434/\" target=\"_blank\">SourceWall</a></p>";
    }

    public static String getSimpleReplyTail() {
        return "\n\n[blockquote]来自 [url=http://www.guokr.com/blog/798434/]SourceWall[/url][/blockquote]";
    }
}

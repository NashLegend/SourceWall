package net.nashlegend.sourcewall.data;

import net.nashlegend.sourcewall.util.MDUtil;
import net.nashlegend.sourcewall.util.PrefsUtil;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by NashLegend on 16/9/19.
 */
public class TailTest {
    @Test
    public void getComplexReplyTail() throws Exception {

    }

    @Test
    public void getDefaultComplexTail() throws Exception {

    }

    @Test
    public void getParametricCustomComplexTail() throws Exception {
        String str = "来自[url=http://m.guokr.com]果壳网移动版[/url]";
        System.out.println(MDUtil.UBB2HtmlLink(str));
    }

    @Test
    public void getSimpleReplyTail() throws Exception {

    }

    @Test
    public void getParametricCustomSimpleTail() throws Exception {

    }

    @Test
    public void getDefaultPlainTail() throws Exception {

    }

    @Test
    public void getPhonePlainTail() throws Exception {

    }

    @Test
    public void getUrl() throws Exception {

    }

}
package net.nashlegend.sourcewall.data;

import android.text.TextUtils;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.util.MDUtil;
import net.nashlegend.sourcewall.util.PrefsUtil;

/**
 * Created by NashLegend on 16/9/7.
 */

public class Tail {

    public final static String defaultDisplayName = "果壳的壳";
    public final static String defaultUrl = "https://github.com/NashLegend/SourceWall/blob/master/README.md";
    public final static String altUrl = "http://www.guokr.com/blog/798434/";

    /**
     * 返回尾巴，html格式
     *
     * @return html格式的尾巴，发贴时带，但是评论答案和评论问题不带
     */
    public static String getComplexReplyTail() {
        String tail = "";
        switch (PrefsUtil.readInt(Consts.Keys.Key_Use_Tail_Type, Consts.TailType.Type_Use_Default_Tail)) {
            case Consts.TailType.Type_Use_Default_Tail:
                tail = getDefaultComplexTail();
                break;
            case Consts.TailType.Type_Use_Phone_Tail:
                tail = getPhoneComplexTail();
                break;
            case Consts.TailType.Type_Use_Custom_Tail:
                tail = getParametricCustomComplexTail();
                break;
        }
        return tail;
    }

    /**
     * 返回默认尾巴
     *
     * @return 默认尾巴
     */
    public static String getDefaultComplexTail() {
        return "<p>   </p><blockquote><p>来自 <a href=\"" + getUrl() + "\" target=\"_blank\">" + defaultDisplayName + "</a></p></blockquote>";
    }

    /**
     * 返回手机尾巴
     *
     * @return 手机尾巴
     */
    private static String getPhoneComplexTail() {
        String mTypeString = android.os.Build.MODEL == null ? App.getApp().getString(R.string.unknown_phone) : android.os.Build.MODEL;
        return "<p/>  </p><blockquote><p>来自 <a href=\"" + getUrl() + "\" target=\"_blank\">" + mTypeString + "</a></p></blockquote>";
    }

    /**
     * 返回自定义参数{}尾巴
     *
     * @return 参数化的尾巴
     */
    public static String getParametricCustomComplexTail() {
        String tail = MDUtil.UBB2HtmlDumb(PrefsUtil.readString(Consts.Keys.Key_Custom_Tail, ""));
        if (TextUtils.isEmpty(tail)) {
            return "";
        } else {
            return "<p>   </p><blockquote>" + tail + "</blockquote>";
        }
    }

    /**
     * 返回尾巴，UBB格式
     *
     * @return html格式的尾巴，评论、发贴、回答时都带，但是评论答案和评论问题不带
     */
    public static String getSimpleReplyTail() {
        String tail = "";
        switch (PrefsUtil.readInt(Consts.Keys.Key_Use_Tail_Type, Consts.TailType.Type_Use_Default_Tail)) {
            case Consts.TailType.Type_Use_Default_Tail:
                tail = getDefaultSimpleTail();
                break;
            case Consts.TailType.Type_Use_Phone_Tail:
                tail = getPhoneSimpleTail();
                break;
            case Consts.TailType.Type_Use_Custom_Tail:
                tail = getParametricCustomSimpleTail();
                break;
        }
        return tail;
    }

    /**
     * 返回手机尾巴
     *
     * @return 手机尾巴
     */
    private static String getPhoneSimpleTail() {
        String mTypeString = android.os.Build.MODEL == null ? App.getApp().getString(R.string.unknown_phone) : android.os.Build.MODEL;
        return "\n\n[blockquote]来自 [url=" + getUrl() + "]" + mTypeString + "[/url][/blockquote]";
    }


    /**
     * 返回默认尾巴
     *
     * @return 默认尾巴
     */
    private static String getDefaultSimpleTail() {
        return "\n\n[blockquote]来自 [url=" + getUrl() + "]" + defaultDisplayName + "[/url][/blockquote]";
    }

    /**
     * @return 自定义尾巴
     */
    public static String getParametricCustomSimpleTail() {
        String tail = PrefsUtil.readString(Consts.Keys.Key_Custom_Tail, "");
        if (TextUtils.isEmpty(tail)) {
            return "";
        } else {
            return "\n\n[blockquote]" + tail + "[/blockquote]";
        }
    }

    public static String getDefaultPlainTail() {
        return "来自 " + defaultDisplayName;
    }

    public static String getPhonePlainTail() {
        String mTypeString = android.os.Build.MODEL == null ? App.getApp().getString(R.string.unknown_phone) : android.os.Build.MODEL;
        return "来自 " + mTypeString;
    }

    //strictfp StringTokenizer
    public static String getUrl() {
        if (Math.random() > 0.5) {
            return defaultUrl;
        } else {
            return altUrl;
        }
    }
}

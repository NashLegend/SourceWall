package net.nashlegend.sourcewall.util;

import android.text.TextUtils;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.BaseActivity;
import net.nashlegend.sourcewall.activities.Reply2Activity;
import net.nashlegend.sourcewall.activities.ReplyActivity;
import net.nashlegend.sourcewall.util.Consts.ImageLoadMode;
import net.nashlegend.sourcewall.util.Consts.Keys;
import net.nashlegend.sourcewall.util.Consts.TailType;

/**
 * Created by NashLegend on 2014/12/15 0015
 */
public class Config {

    public final static long throttleSpan = 500;
    public final static float longImageRatio = 2f;//长宽比超过此值，则认为是超长图
    public final static int ExitTapsGap = 1200;
    public final static String defaultDisplayName = "果壳的壳";
    public final static String defaultUrl = "https://github.com/NashLegend/SourceWall/blob/master/README.md";
    public final static String altUrl = "http://www.guokr.com/blog/798434/";

    /**
     * 是否使用Html方式回复
     *
     * @return
     */
    public static boolean shouldReplyComplex() {
        return PrefsUtil.readBoolean(Keys.Key_Reply_With_Html, false);
    }

    /**
     * 是否使用Html方式回复
     *
     * @return
     */
    public static Class<? extends BaseActivity> getReplyActivity() {
        return PrefsUtil.readBoolean(Keys.Key_Reply_With_Html, false)? Reply2Activity.class : ReplyActivity.class;
    }

    public static boolean shouldLoadImage() {
        //略微有点耗时，最多可耗时3ms，最低0.3ms
        //可以监听网络状态变化，记录状态，而不是直接读ConnectivityManager和SharedPreference
        int mode = getImageLoadMode();
        boolean flag = true;
        switch (mode) {
            case ImageLoadMode.MODE_ALWAYS_LOAD:
                flag = true;
                break;
            case ImageLoadMode.MODE_NEVER_LOAD:
                flag = false;
                break;
            case ImageLoadMode.MODE_LOAD_WHEN_WIFI:
                flag = DeviceUtil.isWifiConnected();
                break;
        }
        return flag;
    }

    public static boolean shouldLoadHomepageImage() {
        return !PrefsUtil.readBoolean(Keys.Key_Image_No_Load_Homepage, false);
    }

    public static int getImageLoadMode() {
        return PrefsUtil.readInt(Keys.Key_Image_Load_Mode, ImageLoadMode.MODE_ALWAYS_LOAD);
    }

    /**
     * 返回尾巴，html格式
     *
     * @return html格式的尾巴，发贴时带，但是评论答案和评论问题不带
     */
    public static String getComplexReplyTail() {
        String tail = "";
        switch (PrefsUtil.readInt(Keys.Key_Use_Tail_Type, TailType.Type_Use_Default_Tail)) {
            case TailType.Type_Use_Default_Tail:
                tail = getDefaultComplexTail();
                break;
            case TailType.Type_Use_Phone_Tail:
                tail = getPhoneComplexTail();
                break;
            case TailType.Type_Use_Custom_Tail:
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
    private static String getDefaultComplexTail() {
        return "<p></p><p>来自 <a href=\"" + getUrl() + "\" target=\"_blank\">" + defaultDisplayName + "</a></p>";
    }

    /**
     * 返回手机尾巴
     *
     * @return 手机尾巴
     */
    private static String getPhoneComplexTail() {
        String mTypeString = android.os.Build.MODEL == null ? App.getApp().getString(R.string.unknown_phone) : android.os.Build.MODEL;
        return "<p></p><p>来自 <a href=\"" + getUrl() + "\" target=\"_blank\">" + mTypeString + "</a></p>";
    }

    /**
     * 返回自定义参数{}尾巴
     *
     * @return 参数化的尾巴
     */
    public static String getParametricCustomComplexTail() {
        String tail = MDUtil.UBB2HtmlDumb(PrefsUtil.readString(Keys.Key_Custom_Tail, ""));
        if (TextUtils.isEmpty(tail)) {
            return "";
        } else {
            return "<p></p>" + tail;
        }
    }

    /**
     * 返回尾巴，UBB格式
     *
     * @return html格式的尾巴，评论、发贴、回答时都带，但是评论答案和评论问题不带
     */
    public static String getSimpleReplyTail() {
        String tail = "";
        switch (PrefsUtil.readInt(Keys.Key_Use_Tail_Type, TailType.Type_Use_Default_Tail)) {
            case TailType.Type_Use_Default_Tail:
                tail = getDefaultSimpleTail();
                break;
            case TailType.Type_Use_Phone_Tail:
                tail = getPhoneSimpleTail();
                break;
            case TailType.Type_Use_Custom_Tail:
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
        String tail = PrefsUtil.readString(Keys.Key_Custom_Tail, "");
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

package com.example.sourcewall.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.sourcewall.AppApplication;

/**
 * Created by NashLegend on 2014/12/15 0015
 */
public class Config {

    public static boolean shouldLoadImage() {
        //略微有点耗时，最多可耗时3ms，最低0.3ms
        //可以监听网络状态变化，记录状态，而不是直接读ConnectivityManager和SharedPreference
        int mode = getImageLoadMode();
        boolean flag = true;
        switch (mode) {
            case Consts.MODE_ALWAYS_LOAD:
                flag = true;
                break;
            case Consts.MODE_NEVER_LOAD:
                flag = false;
                break;
            case Consts.MODE_LOAD_WHEN_WIFI:
                flag = isWifi();
                break;
        }
        return flag;
    }

    private static boolean isWifi() {
        ConnectivityManager manager = (ConnectivityManager) AppApplication.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info != null && info.getState() == NetworkInfo.State.CONNECTED;
    }

    public static int getImageLoadMode() {
        return SharedUtil.readInt(Consts.Key_Image_Load_Mode, Consts.MODE_ALWAYS_LOAD);
    }

    /**
     * 上传图片尺寸
     *
     * @return
     */
    public static int getUploadImageSizeRestrict() {
        return 720;
    }

    /**
     * 返回尾巴，html格式
     *
     * @return html格式的尾巴，评论、发贴、回答时都带，但是评论答案和评论问题不带
     */
    public static String getComplexReplyTail() {
        if (SharedUtil.readBoolean(Consts.Key_Use_Post_Tail, true)) {
            if (SharedUtil.readBoolean(Consts.key_Use_Default_Tail, true)) {
                return getDefaultComplexTail();
            } else {
                return SharedUtil.readString(Consts.Key_Custom_Tail, getDefaultComplexTail());
            }
        }
        return "";
    }

    /**
     * 返回默认尾巴
     *
     * @return 默认尾巴
     */
    public static String getDefaultComplexTail() {
        return "<p></p><p>来自 <a href=\"http://www.guokr.com/blog/798434/\" target=\"_blank\">SourceWall</a></p>";
    }

    /**
     * 返回尾巴，UBB格式
     *
     * @return html格式的尾巴，评论、发贴、回答时都带，但是评论答案和评论问题不带
     */
    public static String getSimpleReplyTail() {
        if (SharedUtil.readBoolean(Consts.Key_Use_Post_Tail, true)) {
            if (SharedUtil.readBoolean(Consts.key_Use_Default_Tail, true)) {
                return getDefaultSimpleReplyTail();
            } else {
                return SharedUtil.readString(Consts.Key_Custom_Tail, getDefaultSimpleReplyTail());
            }
        }
        return "";
    }

    /**
     * 返回默认尾巴
     *
     * @return 默认尾巴
     */
    public static String getDefaultSimpleReplyTail() {
        return "\n\n[blockquote]来自 [url=http://www.guokr.com/blog/798434/]SourceWall[/url][/blockquote]";
    }
}

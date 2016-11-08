package net.nashlegend.sourcewall.data;

import net.nashlegend.sourcewall.activities.BaseActivity;
import net.nashlegend.sourcewall.activities.Reply2Activity;
import net.nashlegend.sourcewall.activities.ReplyActivity;
import net.nashlegend.sourcewall.data.Consts.ImageLoadMode;
import net.nashlegend.sourcewall.data.Consts.Keys;
import net.nashlegend.sourcewall.util.DeviceUtil;
import net.nashlegend.sourcewall.util.PrefsUtil;

/**
 * Created by NashLegend on 2014/12/15 0015
 */
public class Config {

    public final static long throttleSpan = 500;
    public final static float longImageRatio = 2f;//长宽比超过此值，则认为是长图
    public final static float SUPER_LONG_IMAGE_RATIO = 3f;//长宽比超过此值，则认为是超长图,会自动滚动到顶部
    public final static int ExitTapsGap = 1200;

    /**
     * 是否使用Html方式回复
     */
    public static Class<? extends BaseActivity> getReplyActivity() {
        return PrefsUtil.readBoolean(Keys.Key_Reply_With_Simple, false) ? ReplyActivity.class
                : Reply2Activity.class;
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

}

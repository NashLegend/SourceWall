package net.nashlegend.sourcewall.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.nashlegend.sourcewall.BuildConfig;
import net.nashlegend.sourcewall.R;

import java.io.ByteArrayOutputStream;

/**
 * Created by NashLegend on 2015/3/12 0012
 */
public class ShareUtil {

    public static final String WEIXIN_APP_ID_DEBUG = "wxb38f35b29cf6703d";
    public static final String WEIXIN_APP_ID_RELEASE = "wx6383bc21d7a89367";

    public static void shareToWeiXinCircle(Context context, String url, String title, String summary, Bitmap bitmap) {
        String appid = getWeixinAppId();
        IWXAPI api = WXAPIFactory.createWXAPI(context, appid, false);
        if (api.isWXAppInstalled()) {
            WXWebpageObject webPage = new WXWebpageObject();
            webPage.webpageUrl = url;
            WXMediaMessage msg = new WXMediaMessage(webPage);
            msg.title = title;
            msg.description = summary;
            msg.setThumbImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_guokr_logo));
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = String.valueOf(System.currentTimeMillis());
            req.message = msg;
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
            api.sendReq(req);
        } else {
            ToastUtil.toastSingleton(R.string.hint_wechat_not_installed);
        }
    }

    public static void shareToWeiXinFriends(Context context, String url, String title, String summary, Bitmap bitmap) {
        String appid = getWeixinAppId();
        IWXAPI api = WXAPIFactory.createWXAPI(context, appid, false);
        if (api.isWXAppInstalled()) {
            WXWebpageObject webPage = new WXWebpageObject();
            webPage.webpageUrl = url;
            WXMediaMessage msg = new WXMediaMessage(webPage);
            msg.title = title;
            msg.description = summary;
            msg.setThumbImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_guokr_logo));
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = String.valueOf(System.currentTimeMillis());
            req.message = msg;
            req.scene = SendMessageToWX.Req.WXSceneSession;
            api.sendReq(req);
        } else {
            ToastUtil.toastSingleton(R.string.hint_wechat_not_installed);
        }
    }

    public static String getWeixinAppId() {
        if (BuildConfig.DEBUG) {
            return WEIXIN_APP_ID_DEBUG;
        } else {
            return WEIXIN_APP_ID_RELEASE;
        }
    }

    public static byte[] bmpToByteArray(Bitmap bmp, boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}

package net.nashlegend.sourcewall.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;

import java.io.ByteArrayOutputStream;

/**
 * Created by NashLegend on 2015/3/12 0012
 */
public class ShareUtil {

    public static final String WEIXIN_APP_ID = "wxb38f35b29cf6703d";
    public static final String WEIXIN_SECRET = "9534b42035a74acad28a572c62fdeff5";

    public static void shareToWeiXin(Context context, String url, String title, String summary, Bitmap mBitmap, boolean tag) {
        IWXAPI api = WXAPIFactory.createWXAPI(context, WEIXIN_APP_ID, false);
        if (api.isWXAppInstalled()) {
            api.registerApp(WEIXIN_APP_ID);

            WXWebpageObject webpage = new WXWebpageObject();
            webpage.webpageUrl = url;
            WXMediaMessage msg = new WXMediaMessage(webpage);
            msg.title = title;
            msg.description = summary;

            if (null != mBitmap && !mBitmap.isRecycled()) {
                if (tag) {
                    int width = (int) Math.sqrt(80 * 1024 * 8 * mBitmap.getWidth() / mBitmap.getHeight()) / 16;
                    int height = mBitmap.getHeight() * width / mBitmap.getWidth();
                    Bitmap bitmap = ThumbnailUtils.extractThumbnail(mBitmap, width, height);
                    msg.thumbData = bmpToByteArray(bitmap, true);
                }
                msg.mediaObject = new WXImageObject(mBitmap);
            } else {
                Bitmap thumb = BitmapFactory
                        .decodeResource(context.getResources(), R.drawable.ic_guokr_logo);
                msg.thumbData = bmpToByteArray(thumb, true);
                thumb.recycle();
            }
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = String.valueOf(System.currentTimeMillis());
            req.message = msg;
            req.scene = tag ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
            api.sendReq(req);
        } else {
            ToastUtil.toastSingleton(R.string.hint_wechat_not_installed);
        }
        if (tag) {
            MobclickAgent.onEvent(context, Mob.Event_Share_To_Wechat_Friends);
        } else {
            MobclickAgent.onEvent(context, Mob.Event_Share_To_Wechat_Circle);
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

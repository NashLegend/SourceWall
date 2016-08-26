package net.nashlegend.sourcewall.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.Utility;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.util.AccessTokenKeeper;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Consts.Extras;
import net.nashlegend.sourcewall.util.ShareUtil;

public class WeiboShareActivity extends BaseActivity implements IWeiboHandler.Response {

    private IWeiboShareAPI mWeiboShareAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo_share);
        setSwipeEnabled(false);
        findViewById(R.id.container).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return false;
            }
        });
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, ShareUtil.getWeiboAppKey());
        mWeiboShareAPI.registerApp();
        if (savedInstanceState != null) {
            mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
        }
        String url = getIntent().getStringExtra(Extras.Extra_Shared_Url);
        String title = getIntent().getStringExtra(Extras.Extra_Shared_Title);
        String summary = getIntent().getStringExtra(Extras.Extra_Shared_Summary);
        shareToWeibo(url, title, summary, null);
    }

    public void shareToWeibo(String url, String title, String summary, Bitmap bitmap) {
        if (summary == null) {
            summary = "";
        }
        if (title == null) {
            title = "";
        }
        if (url == null) {
            url = "";
        }
        if (summary.length() > 140) {
            summary = summary.substring(0, 135) + "...";
        }
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = title;
        mediaObject.description = summary;
        mediaObject.actionUrl = url;
        mediaObject.defaultText = summary;
        mediaObject.setThumbImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic_guokr_logo));
        TextObject textObject = new TextObject();
        textObject.text = summary;
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.mediaObject = mediaObject;
        weiboMessage.textObject = textObject;
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        AuthInfo authInfo = new AuthInfo(this, ShareUtil.getWeiboAppKey(), ShareUtil.REDIRECT_URL, ShareUtil.SCOPE);
        Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(getApplicationContext());
        String token = "";
        if (accessToken != null) {
            token = accessToken.getToken();
        }
        mWeiboShareAPI.sendRequest(this, request, authInfo, token, new WeiboAuthListener() {

            @Override
            public void onWeiboException(WeiboException arg0) {

            }

            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                AccessTokenKeeper.writeAccessToken(getApplicationContext(), newToken);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mWeiboShareAPI.handleWeiboResponse(intent, this);
    }

    @Override
    public void onResponse(BaseResponse baseResponse) {
        switch (baseResponse.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                Toast.makeText(this, R.string.ERR_OK, Toast.LENGTH_LONG).show();
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                Toast.makeText(this, R.string.ERR_USER_CANCEL, Toast.LENGTH_LONG).show();
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                Toast.makeText(this, getString(R.string.ERR_SENT_FAILED) + " : " + baseResponse.errMsg, Toast.LENGTH_LONG).show();
                break;
        }
        finish();
    }
}

package net.nashlegend.sourcewall.wxapi;

import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.BaseActivity;
import net.nashlegend.sourcewall.util.ShareUtil;

public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxentry);
        IWXAPI api = WXAPIFactory.createWXAPI(this, ShareUtil.getWeixinAppId(), false);
        api.registerApp(ShareUtil.getWeixinAppId());
        api.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        int result;
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = R.string.ERR_OK;
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.ERR_USER_CANCEL;
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.ERR_AUTH_DENIED;
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                result = R.string.ERR_SENT_FAILED;
                break;
            case BaseResp.ErrCode.ERR_COMM:
                result = R.string.ERR_COMM;
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                result = R.string.ERR_UNSUPPORT;
                break;
            default:
                result = R.string.ERR_UNKNOWN;
                break;
        }
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        finish();
    }
}

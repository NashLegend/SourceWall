package net.nashlegend.sourcewall.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.events.LoginStateChangedEvent;
import net.nashlegend.sourcewall.request.HttpUtil;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.PrefsUtil;
import net.nashlegend.sourcewall.util.UiUtil;

import de.greenrobot.event.EventBus;

public class LoginActivity extends BaseActivity {

    private WebView webView;
    private String cookieStr;

    @Override
    public void setTheme(int resid) {
        directlySetTheme(R.style.AppTheme);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MobclickAgent.onEvent(this, Mob.Event_Login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        CookieSyncManager.createInstance(App.getApp());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.hasCookies();
        cookieManager.removeSessionCookie();
        CookieSyncManager.getInstance().sync();

        webView = (WebView) findViewById(R.id.web_login);
        webView.setWebViewClient(webViewClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadUrl(Consts.LOGIN_URL);

    }

    @Override
    protected void onDestroy() {
        webView.stopLoading();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onDestroy();
    }

    private boolean parseRawCookie(String rawCookie) {
        try {
            String tmpToken = "";
            String tmpUkey = "";
            if (!TextUtils.isEmpty(rawCookie)) {
                String[] rawCookieParams = rawCookie.split(";");
                for (String rawCookieParam : rawCookieParams) {
                    String rawCookieParamNameAndValue[] = rawCookieParam.trim().split("=");
                    if (rawCookieParamNameAndValue.length != 2) {
                        continue;
                    }
                    String paramName = rawCookieParamNameAndValue[0].trim();
                    String paramValue = rawCookieParamNameAndValue[1].trim();
                    if (Consts.Cookie_Token_Key.equals(paramName)) {
                        UserAPI.setToken(paramValue);
                        tmpToken = paramValue;
                    } else if (Consts.Cookie_Ukey_Key.equals(paramName)) {
                        UserAPI.setUkey(paramValue);
                        tmpUkey = paramValue;
                    }
                }
                HttpUtil.setCookie(HttpUtil.getDefaultHttpClient());
            }
            boolean gotToken = !TextUtils.isEmpty(tmpUkey) && !TextUtils.isEmpty(tmpToken);
            return gotToken;
        } catch (Exception e) {
            return false;
        }
    }

    AlertDialog dialog;
    boolean tokenOk = false;

    WebViewClient webViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            if (url.equals(Consts.SUCCESS_URL_1) || url.equals(Consts.SUCCESS_URL_2)) {
                if (parseRawCookie(cookieStr)) {
                    tokenOk = true;
                    UiUtil.dismissDialog(dialog);
                    webView.stopLoading();
                    PrefsUtil.saveString(Consts.Key_Cookie, cookieStr);
                    delayFinish();
                } else {
                    delayPopTokenFailed();
                }
            } else {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            CookieManager cookieManager = CookieManager.getInstance();
            if (cookieManager != null) {
                cookieStr = cookieManager.getCookie(url);
                if (Consts.LOGIN_URL.equals(url)) {
                    webView.stopLoading();
                }
            }
            if (parseRawCookie(cookieStr)) {
                webView.stopLoading();
                PrefsUtil.saveString(Consts.Key_Cookie, cookieStr);
                setResult(RESULT_OK);
                delayFinish();
            } else {
                super.onPageFinished(view, url);
            }
        }
    };

    private void delayPopTokenFailed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!tokenOk) {
                    popTokenFailed();
                }
            }
        }, 2000);
    }

    private void popTokenFailed() {
        if (isFinishing()) {
            return;
        }
        dialog = new AlertDialog.Builder(LoginActivity.this)
                .setTitle(R.string.hint)
                .setMessage(R.string.user_handle_login_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String lazyLoad = "http://m.guokr.com/sso/mobile/?suppress_prompt=1&lazy=y&success=http%3A%2F%2Fm.guokr.com%2F";
                        webView.loadUrl(lazyLoad);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        setResult(RESULT_CANCELED);
                        delayFinish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                        setResult(RESULT_CANCELED);
                        delayFinish();
                    }
                }).create();
        dialog.show();
    }

    private void delayFinish() {
        if (UserAPI.isLoggedIn()) {
            EventBus.getDefault().post(new LoginStateChangedEvent());
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    finish();
                }
            }
        }, 300);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}

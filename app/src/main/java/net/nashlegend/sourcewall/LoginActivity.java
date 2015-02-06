package net.nashlegend.sourcewall;

import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import net.nashlegend.sourcewall.connection.HttpFetcher;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedUtil;

import org.apache.http.impl.cookie.BasicClientCookie;


public class LoginActivity extends SwipeActivity {

    private WebView webView;
    private String cookieStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(net.nashlegend.sourcewall.R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(net.nashlegend.sourcewall.R.id.action_bar);
        setSupportActionBar(toolbar);

        CookieSyncManager.createInstance(AppApplication.getApplication());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.hasCookies();
        cookieManager.removeSessionCookie();
        CookieSyncManager.getInstance().sync();

        webView = (WebView) findViewById(net.nashlegend.sourcewall.R.id.web_login);
        webView.setWebViewClient(webViewClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadUrl(Consts.LOGIN_URL);
    }

    private boolean parseRawCookie(String rawCookie) {
        String tmpToken = "";
        String tmpUkey = "";
        if (!TextUtils.isEmpty(rawCookie)) {
            String[] rawCookieParams = rawCookie.split(";");
            for (int i = 1; i < rawCookieParams.length; i++) {
                String rawCookieParamNameAndValue[] = rawCookieParams[i].trim().split("=");
                if (rawCookieParamNameAndValue.length != 2) {
                    continue;
                }
                String paramName = rawCookieParamNameAndValue[0].trim();
                String paramValue = rawCookieParamNameAndValue[1].trim();
                BasicClientCookie clientCookie = new BasicClientCookie(paramName, paramValue);
                clientCookie.setDomain("guokr.com");
                clientCookie.setPath("/");
                HttpFetcher.getDefaultHttpClient().getCookieStore().addCookie(clientCookie);
                if (Consts.Cookie_Token_Key.equals(paramName)) {
                    SharedUtil.saveString(Consts.Key_Access_Token, paramValue);
                    tmpToken = paramValue;
                } else if (Consts.Cookie_Ukey_Key.equals(paramName)) {
                    SharedUtil.saveString(Consts.Key_Ukey, paramValue);
                    tmpUkey = paramValue;
                }
            }
        }
        if (TextUtils.isEmpty(tmpUkey) || TextUtils.isEmpty(tmpToken)) {
            Toast.makeText(this, "获取Token失败，\n\n  (ノ=Д=)ノ┻━┻ \n\n 请稍后重试登录……", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.equals(Consts.SUCCESS_URL_1) || url.equals(Consts.SUCCESS_URL_2)) {
                // login ok
                SharedUtil.saveString(Consts.Key_Cookie, cookieStr);
                parseRawCookie(cookieStr);
                setResult(RESULT_OK);
                finish();
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
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
            super.onReceivedLoginRequest(view, realm, account, args);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}

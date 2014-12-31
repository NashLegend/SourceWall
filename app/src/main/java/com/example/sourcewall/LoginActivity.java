package com.example.sourcewall;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.sourcewall.connection.api.UserAPI;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.SharedUtil;


public class LoginActivity extends SwipeActivity {

    WebView webView;
    String cookieStr;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        webView = (WebView) findViewById(R.id.web_login);
        webView.setWebViewClient(webViewClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadUrl(Consts.LOGIN_URL);
    }

    private void parseRawCookie(String rawCookie) {
        String[] rawCookieParams = rawCookie.split(";");
        //TODO check access_token valid
        for (int i = 1; i < rawCookieParams.length; i++) {
            String rawCookieParamNameAndValue[] = rawCookieParams[i].trim().split("=");
            if (rawCookieParamNameAndValue.length != 2) {
                continue;
            }
            String paramName = rawCookieParamNameAndValue[0].trim();
            String paramValue = rawCookieParamNameAndValue[1].trim();
            if (Consts.Cookie_Token_Key.equals(paramName)) {
                SharedUtil.saveString(Consts.Key_Access_Token, paramValue);
                AppApplication.tokenString = paramValue;
            } else if (Consts.Cookie_Ukey_Key.equals(paramName)) {
                SharedUtil.saveString(Consts.Key_Ukey, paramValue);
                AppApplication.ukeyString = paramValue;
            }
        }
        AppApplication.cookieString = rawCookie;
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
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}

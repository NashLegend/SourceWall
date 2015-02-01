package net.nashlegend.sourcewall;

import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
        webView = (WebView) findViewById(net.nashlegend.sourcewall.R.id.web_login);
        webView.setWebViewClient(webViewClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadUrl(Consts.LOGIN_URL);
    }

    private void parseRawCookie(String rawCookie) {
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

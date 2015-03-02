package net.nashlegend.sourcewall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
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
    public void setTheme(int resid) {
        directlySetTheme(R.style.AppTheme);
    }

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

    @Override
    protected void onDestroy() {
        webView.stopLoading();
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
            return !(TextUtils.isEmpty(tmpUkey) || TextUtils.isEmpty(tmpToken));
        } catch (Exception e) {
            return false;
        }
    }

    WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            if (url.equals(Consts.SUCCESS_URL_1) || url.equals(Consts.SUCCESS_URL_2)) {
                // login ok
                if (parseRawCookie(cookieStr)) {
                    webView.stopLoading();
                    SharedUtil.saveString(Consts.Key_Cookie, cookieStr);
                    setResult(RESULT_OK);
                    delayFinish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle(R.string.hint);
                    builder.setMessage(R.string.user_handle_login_message);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String lazyLoad = "http://m.guokr.com/sso/mobile/?suppress_prompt=1&lazy=y&success=http%3A%2F%2Fm.guokr.com%2F";
                            view.loadUrl(lazyLoad);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            setResult(RESULT_CANCELED);
                            delayFinish();
                        }
                    });
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.cancel();
                            setResult(RESULT_CANCELED);
                            delayFinish();
                        }
                    });
                    builder.show();
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
                SharedUtil.saveString(Consts.Key_Cookie, cookieStr);
                setResult(RESULT_OK);
                delayFinish();
            } else {
                super.onPageFinished(view, url);
            }
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

    private void delayFinish() {
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

}

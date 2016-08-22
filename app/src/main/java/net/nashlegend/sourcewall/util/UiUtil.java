package net.nashlegend.sourcewall.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;

import net.nashlegend.sourcewall.App;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by NashLegend on 16/7/27.
 */

public class UiUtil {


    public static void ensureWebView(WebView webView){
        fixedAccessibilityInjectorException(webView);
        disableAccessibility();
    }

    private static void fixedAccessibilityInjectorException(WebView webView) {
        try {
            if (Build.VERSION.SDK_INT == 17) {
                Object webViewProvider = WebView.class.getMethod("getWebViewProvider").invoke(webView);
                Method getAccessibilityInjector = webViewProvider.getClass().getDeclaredMethod("getAccessibilityInjector");
                getAccessibilityInjector.setAccessible(true);
                Object accessibilityInjector = getAccessibilityInjector.invoke(webViewProvider);
                getAccessibilityInjector.setAccessible(false);
                Field mAccessibilityManagerField = accessibilityInjector.getClass().getDeclaredField("mAccessibilityManager");
                mAccessibilityManagerField.setAccessible(true);
                Object mAccessibilityManager = mAccessibilityManagerField.get(accessibilityInjector);
                mAccessibilityManagerField.setAccessible(false);
                Field mIsEnabledField = mAccessibilityManager.getClass().getDeclaredField("mIsEnabled");
                mIsEnabledField.setAccessible(true);
                mIsEnabledField.set(mAccessibilityManager, false);
                mIsEnabledField.setAccessible(false);
            }
        } catch (Exception ignored) {

        }
    }

    private static void disableAccessibility() {
        try {
            if (Build.VERSION.SDK_INT == 17/*4.2 (Build.VERSION_CODES.JELLY_BEAN_MR1)*/) {
                try {
                    AccessibilityManager am = (AccessibilityManager) App.getApp().getSystemService(Context.ACCESSIBILITY_SERVICE);
                    if (!am.isEnabled()) {
                        return;
                    }
                    Method set = am.getClass().getDeclaredMethod("setState", int.class);
                    set.setAccessible(true);
                    set.invoke(am, 0);
                } catch (Exception ignored) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideIME(Activity activity) {
        try {
            if (activity.getCurrentFocus() == null) {
                return;
            }
            ((InputMethodManager) activity.getSystemService
                    (Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            ErrorUtils.onException(e);
        }
    }


    private static long lastClickTime = 0L;

    /**
     * 是否应该阻止第二次单击
     * if(shouldThrottle()){
     * return;
     * }
     *
     * @return
     */
    public static boolean shouldThrottle() {
        System.out.println("***");
        long crtTime = System.currentTimeMillis();
        System.out.println(crtTime - lastClickTime);
        boolean flag = Math.abs(crtTime - lastClickTime) <= Config.throttleSpan;
        lastClickTime = crtTime;
        return flag;
    }

    public static void cancelDialog(Dialog dialog) {
        try {
            dialog.cancel();
        } catch (Exception ignored) {

        }
    }

    public static void dismissDialog(Dialog dialog) {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception ignored) {

        }
    }
}

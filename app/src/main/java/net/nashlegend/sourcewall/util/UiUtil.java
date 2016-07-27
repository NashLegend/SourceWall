package net.nashlegend.sourcewall.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by NashLegend on 16/7/27.
 */

public class UiUtil {

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
        long crtTime = System.currentTimeMillis();
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

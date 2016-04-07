package net.nashlegend.sourcewall.util;

import android.app.Dialog;

/**
 * Created by NashLegend on 16/3/6.
 */
public class CommonUtil {

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
            dialog.dismiss();
        } catch (Exception ignored) {

        }
    }
}

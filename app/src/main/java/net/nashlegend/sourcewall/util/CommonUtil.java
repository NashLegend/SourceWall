package net.nashlegend.sourcewall.util;

import android.app.Dialog;

/**
 * Created by NashLegend on 16/3/6.
 */
public class CommonUtil {
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

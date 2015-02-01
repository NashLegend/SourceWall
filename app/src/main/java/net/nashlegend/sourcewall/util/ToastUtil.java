package net.nashlegend.sourcewall.util;

import android.widget.Toast;

import net.nashlegend.sourcewall.AppApplication;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class ToastUtil {

    public static Toast toast;

    public static void toast(String msg) {
        Toast.makeText(AppApplication.getApplication(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void toast(int resID) {
        Toast.makeText(AppApplication.getApplication(), resID, Toast.LENGTH_SHORT).show();
    }

    public static void toastSingleton(String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(AppApplication.getApplication(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void toastSingleton(int resID) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(AppApplication.getApplication(), resID, Toast.LENGTH_SHORT);
        toast.show();
    }
}

package net.nashlegend.sourcewall.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import net.nashlegend.sourcewall.App;

/**
 * Created by NashLegend on 2015/9/23 0023.
 * 打开设备某设置，获取设备信息
 */
public class DeviceUtil {

    /**
     * 网络是否可用，只有当wifi或者移动网络打开时才算，其他的不算
     *
     * @return
     */
    public static boolean isConnectionOK() {
        return isWifiConnected() || isMobileNetworkConnected();
    }

    @SuppressWarnings("deprecation")
    public static boolean isWifiConnected() {
        ConnectivityManager manager = (ConnectivityManager) App.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info != null && info.isConnected();
    }

    @SuppressWarnings("deprecation")
    public static boolean isMobileNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) App.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return info != null && info.isConnected();
    }

    public static void openNetwork(Activity activity) {
        openSetting(activity);
    }

    /**
     * 打开设置
     */
    public static void openSetting(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivity(intent);
        } catch (Exception e) {
            ErrorUtils.onException(e);
        }
    }
}

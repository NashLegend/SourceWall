package net.nashlegend.sourcewall.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class ToastUtil {

    public static Toast toast;

    @IntDef({Toast.LENGTH_SHORT, Toast.LENGTH_LONG})
    public @interface Duration {
    }

    public static void toast(final String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.getApp(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void toast(@StringRes final int resID) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.getApp(), resID, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void toastSingleton(final String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(App.getApp(), msg, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public static void toastSingleton(@StringRes final int resID) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(App.getApp(), resID, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }

    /**
     * 纯文字的toast
     */
    public static void toastBigSingleton(CharSequence text) {
        toastBigSingleton(text, Toast.LENGTH_SHORT);
    }

    /**
     * 纯文字的toast
     */
    public static void toastBigSingleton(@StringRes int resID) {
        toastBigSingleton(App.getApp().getResources().getText(resID), Toast.LENGTH_SHORT);
    }

    /**
     * 纯文字的toast
     */
    @SuppressLint("InflateParams")
    public static void toastBigSingleton(final CharSequence text, @Duration final int duration) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }
                toast = new Toast(App.getApp());
                LayoutInflater inflate = (LayoutInflater)
                        App.getApp().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = inflate.inflate(R.layout.toast_text, null);
                TextView tv = (TextView) v.findViewById(R.id.message);
                tv.setText(text);
                toast.setView(v);
                //普通纯文字都显示在中部
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setDuration(duration);
                toast.show();
            }
        });

    }


    /**
     * 纯文字的toast
     */
    public static void toastBig(CharSequence text) {
        toastBig(text, Toast.LENGTH_SHORT);
    }

    /**
     * 纯文字的toast
     */
    public static void toastBig(@StringRes int resID) {
        toastBig(App.getApp().getResources().getText(resID), Toast.LENGTH_SHORT);
    }

    /**
     * 纯文字的toast
     */
    @SuppressLint("InflateParams")
    public static void toastBig(final CharSequence text, @Duration final int duration) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = new Toast(App.getApp());
                LayoutInflater inflate = (LayoutInflater)
                        App.getApp().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = inflate.inflate(R.layout.toast_text, null);
                TextView tv = (TextView) v.findViewById(R.id.message);
                tv.setText(text);
                toast.setView(v);
                //普通纯文字都显示在中部
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setDuration(duration);
                toast.show();
            }
        });
    }
}

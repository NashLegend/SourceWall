package net.nashlegend.sourcewall.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import net.nashlegend.sourcewall.App;

import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by NashLegend on 2014/11/25 0025
 */
public class CachedUtil {

    public static boolean hasCleared = false;

    // 本地xml文件名
    private final static String SP_NAME = "request_cache";
    private final static int MAX_ITEMS = 1000;

    private static SharedPreferences mSharedPreferences;
    private static Editor mEditor;

    public static SharedPreferences getSharedPreferences() {
        if (mSharedPreferences == null) {
            mSharedPreferences = App.getApp().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }

    @SuppressLint("CommitPrefEdits")
    public static Editor getEditor() {
        if (mEditor == null) {
            mEditor = getSharedPreferences().edit();
        }
        return mEditor;
    }

    // 读String
    public static long readTime(String key) {
        return getSharedPreferences().getLong(key, 0);
    }

    // 写String
    public static void saveTime(String key, long value) {
        getEditor().putLong(key, value).apply();
    }

    // 删除一项
    public static void remove(String key) {
        getEditor().remove(key).apply();
    }

    // 全清空
    public static void clear() {
        getEditor().clear().apply();
    }

    public static void removeOld() {
        hasCleared = true;
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            Map map = getSharedPreferences().getAll();
                            if (map.size() > MAX_ITEMS) {
                                clear();
                            }
                        } catch (Exception ignored) {

                        }
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {

                    }
                });
    }
}

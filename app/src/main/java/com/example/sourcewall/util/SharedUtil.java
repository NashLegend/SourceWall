package com.example.sourcewall.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.example.sourcewall.AppApplication;

/**
 * Created by NashLegend on 2014/11/25 0025
 */
public class SharedUtil {

    // 本地xml文件名
    private final static String SP_NAME = "sourcewall";

    private static SharedPreferences mSharedPreferences;
    private static Editor mEditor;

    public static SharedPreferences getSharedPreferences() {
        if (mSharedPreferences == null) {
            mSharedPreferences = AppApplication.getApplication().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }

        return mSharedPreferences;
    }

    public static Editor getEditor() {
        if (mEditor == null) {
            mEditor = getSharedPreferences().edit();
        }

        return mEditor;
    }

    // 读String
    public static String readString(String key, String defaultvalue) {
        String str = getSharedPreferences().getString(key, defaultvalue);
        return str;
    }

    // 写String
    public static boolean saveString(String key, String value) {
        getEditor().putString(key, value);
        return getEditor().commit();
    }

    // 读Boolean
    public static boolean readBoolean(String key, boolean defaultvalue) {
        return getSharedPreferences().getBoolean(key, defaultvalue);
    }

    // 写Boolean
    public static boolean saveBoolean(String key, boolean value) {
        getEditor().putBoolean(key, value);
        return getEditor().commit();
    }

    // 读Int
    public static int readInt(String key, int defValue) {
        int n = getSharedPreferences().getInt(key, defValue);
        return n;
    }

    // 写Int
    public static boolean saveInt(String key, int value) {
        getEditor().putInt(key, value);
        return getEditor().commit();
    }

    // 布尔值读
    public static boolean isActive(String key, boolean defaultvalue) {
        return getSharedPreferences().getBoolean(key, defaultvalue);
    }

    // 布尔值写
    public static boolean saveActive(String key, boolean value) {
        getEditor().putBoolean(key, value);
        return getEditor().commit();
    }

    // 删除一项
    public static boolean remove(String key) {
        getEditor().remove(key);
        return getEditor().commit();
    }

    // 全清空
    public static boolean clear() {
        getEditor().clear();
        return getEditor().commit();
    }
}

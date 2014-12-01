package com.example.sourcewall.connection.api;

import android.content.Context;
import android.content.Intent;

import com.example.sourcewall.AppApplication;
import com.example.sourcewall.LoginActivity;

/**
 * Created by NashLegend on 2014/11/25 0025
 */
public class UserAPI extends APIBase {

    public static boolean testLogin() {
        return false;
    }

    public static void startLoginActivity(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    public static void getBaskets() {

    }

    public static String getToken(){
        return AppApplication.tokenString;
    }
}

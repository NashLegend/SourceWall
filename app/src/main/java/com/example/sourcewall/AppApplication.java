package com.example.sourcewall;

import android.app.Application;
import android.os.AsyncTask;

import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.UserAPI;
import com.example.sourcewall.util.ToastUtil;

/**
 * Created by NashLegend on 2014/9/24 0024
 */
public class AppApplication extends Application {

    static AppApplication application;
    public static String cookieString = "";
    public static String tokenString = "";
    public static String ukeyString = "";

    //TODO Network Monitor

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        TestLoginTask testLoginTask = new TestLoginTask();
        testLoginTask.execute();
    }

    public static Application getApplication() {
        return application;
    }

    class TestLoginTask extends AsyncTask<Void, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(Void... params) {
            return UserAPI.testLogin();
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                //ok
            } else {
                //clear
                ToastUtil.toast("Not Logged In");
                switch (resultObject.code) {
                    case CODE_NOT_LOGGED_IN:
                        break;
                    case CODE_NETWORK_ERROR:
                        break;
                    case CODE_JSON_ERROR:
                        break;
                    case CODE_UNKNOWN:
                        break;
                }
            }
        }
    }
}

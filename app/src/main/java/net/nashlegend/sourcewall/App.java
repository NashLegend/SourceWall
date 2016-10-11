package net.nashlegend.sourcewall;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.leakcanary.LeakCanary;

import net.nashlegend.sourcewall.data.Consts.Keys;
import net.nashlegend.sourcewall.util.ErrorUtils;
import net.nashlegend.sourcewall.util.ImageUtils;
import net.nashlegend.sourcewall.util.PrefsUtil;

import static android.support.v7.app.AppCompatDelegate.setDefaultNightMode;

/**
 * Created by NashLegend on 2014/9/24 0024
 */
public class App extends Application {

    private static App application;

    @Override
    public void onCreate() {
        application = this;
        if (PrefsUtil.readBoolean(Keys.Key_Is_Night_Mode, false)) {
            setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate();
        if (isMainProcess()) {
            initImageLoader(this);
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return;
            }
            LeakCanary.install(this);
        }
    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .denyCacheImageMultipleSizesInMemory()
                .defaultDisplayImageOptions(ImageUtils.defaultImageOptions)
                .memoryCacheSizePercentage(25)
                .threadPoolSize(Runtime.getRuntime().availableProcessors() * 2 + 1)
                .build();
        ImageLoader.getInstance().init(config);
    }

    public static Application getApp() {
        return application;
    }

    public static boolean isNightMode() {
        return (getApp().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static int getVersionInt() {
        try {
            PackageManager pm = getApp().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getApp().getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            ErrorUtils.onException(e);
        }
        return 1;
    }

    /**
     * 判定是否是本地主进程
     */
    public static boolean isMainProcess() {
        String processName = getCurProcessName();
        return !TextUtils.isEmpty(processName) && !processName.contains(":");
    }

    @Nullable
    private static String getCurProcessName() {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) App.getApp().getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        } catch (Exception ignored) {

        }
        return null;
    }
}

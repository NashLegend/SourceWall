package com.example.sourcewall.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 将下面两行添加到APP的Application文件的onCreate()方法里。 CrashReporter crashReporter=new
 * CrashReporter(getApplicationContext());
 * Thread.setDefaultUncaughtExceptionHandler(crashReporter);
 * 如果需要Application处理崩溃问题，比如关闭应用程序，则需要调用如下：
 * crashReporter.setOnCrashListener(xxx);
 * 如果在CrashListener.onCrash()里面想调用系统默认的Force Close对话框，可做如下操作：
 * 在执行Thread.setDefaultUncaughtExceptionHandler (crashReporter)之前先定义一个系统默认的
 * UncaughtExceptionHandler mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
 * 然后在CrashListener.onCrash()里面添加mUncaughtExceptionHandler
 * .uncaughtException(thread, ex);
 *
 * @author NashLegend
 */
public class CrashReporter implements UncaughtExceptionHandler {
    private Context mContext;
    private CrashListener onCrashListener;

    public CrashReporter(Context context) {
        mContext = context;
    }

    /*
     * 发生未能捕获错误的时候调用。
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        String infoString = "";
        TelephonyManager telephonyManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi;
        String versionCode = "";
        String versionName = "";
        try {
            pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            versionCode = pi.versionCode + "";
            versionName = pi.versionName == null ? "-1" : pi.versionName;
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        String mtypeString = android.os.Build.MODEL == null ? "-1"
                : android.os.Build.MODEL;
        String mSystem = android.os.Build.VERSION.RELEASE == null ? "-1"
                : android.os.Build.VERSION.RELEASE;
        String manufacturer = android.os.Build.MANUFACTURER == null ? "-1"
                : android.os.Build.MANUFACTURER;
        String networkOperator = telephonyManager.getNetworkOperatorName() == null ? "-1"
                : telephonyManager.getNetworkOperatorName();
        String IMEI = telephonyManager.getDeviceId() == null ? "-1"
                : telephonyManager.getDeviceId();

        infoString += "APP版本号：" + versionCode + "\n";
        infoString += "APP版本名：" + versionName + "\n";
        infoString += "手机型号：" + mtypeString + "\n";
        infoString += "系统版本：" + mSystem + "\n";
        infoString += "手机厂商：" + manufacturer + "\n";
        infoString += "运营商：" + networkOperator + "\n";
        infoString += "IMEI：" + IMEI + "\n";

        long threadId = thread.getId();
        infoString += ("ThreadInfo : Thread.getName()=" + thread.getName()
                + " id=" + threadId + " state=" + thread.getState() + "\n");
        try {
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            ex.printStackTrace(printWriter);
            String stackTrace = result.toString();
            infoString += stackTrace;

            infoString += "\n";
            infoString += "Cause : \n";
            infoString += "======= \n";

            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                infoString += result.toString();
                cause = cause.getCause();
            }
            result.close();
            printWriter.close();
        } catch (Exception e) {

        }

        writeCrashLog(infoString);

        if (onCrashListener != null) {
            onCrashListener.onCrash(infoString, thread, ex);
        }
    }

    /**
     * 向磁盘写入错误信息。
     *
     * @param info 错误信息。
     */
    private void writeCrashLog(String info) {
        FileOutputStream fos = null;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        File file = new File(mContext.getExternalFilesDir("log"), "CrashLog_" + date);
        try {
            if (file.exists()) {
                file.delete();
            } else {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            fos = new FileOutputStream(file);
            fos.write(info.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取存储在磁盘上的Error Log;
     *
     * @param context
     * @return
     */
    public static String getCrashLog(Context context) {
        String CrashLog = "";
        File file = new File(context.getExternalFilesDir("log"), "crashlog");
        FileInputStream fis = null;
        try {
            if (file.exists()) {
                byte[] data = new byte[(int) file.length()];
                fis = new FileInputStream(file);
                fis.read(data);
                CrashLog = new String(data);
                data = null;
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return CrashLog;
    }

    /**
     * 返回Crash Log文件；
     *
     * @param context
     * @return
     */
    public static File getCrashLogFile(Context context) {
        File file = new File(context.getExternalFilesDir("log"), "crashlog");
        return file;
    }

    public static void clearCrashLog(Context context) {
        File file = new File(context.getExternalFilesDir("log"), "crashlog");
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception e) {

            }
        }
    }

    /**
     * 设置崩溃时的回调
     *
     * @param crashListener
     */
    public void setOnCrashListener(CrashListener crashListener) {
        onCrashListener = crashListener;
    }

    /**
     * 发生UncaughtException时的回调
     *
     * @author NashLegend
     */
    public interface CrashListener {
        void onCrash(String info, Thread thread, Throwable ex);
    }

}

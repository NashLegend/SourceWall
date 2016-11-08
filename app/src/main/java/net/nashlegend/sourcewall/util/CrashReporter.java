package net.nashlegend.sourcewall.util;

import android.content.Context;
import android.util.Log;

import net.nashlegend.sourcewall.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;

/**
 * 将下面两行添加到APP的Application文件的onCreate()方法里。
 * CrashReporter crashReporter=new CrashReporter(getApplicationContext());
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
@SuppressWarnings("ResultOfMethodCallIgnored")
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
        long threadId = thread.getId();
        infoString += ("ThreadInfo : Thread.getName()=" + thread.getName()
                + " id=" + threadId + " state=" + thread.getState() + "\n");
        infoString += Log.getStackTraceString(ex);
        if (BuildConfig.DEBUG) {
            Log.e("crash", infoString);
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
        OutputStreamWriter writer = null;
        BufferedWriter out = null;
        File file = getCrashLogFile(mContext);
        try {
            if (file.exists()) {
                file.delete();
            } else {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("utf-8"));
            out = new BufferedWriter(writer, 4196);
            out.write(info);
            out.flush();
        } catch (Exception e) {
            ErrorUtils.onException(e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                ErrorUtils.onException(e);
            }
        }
    }

    /**
     * 读取存储在磁盘上的Error Log;
     */
    public static String getCrashLog(Context context) {
        String CrashLog = "";
        File file = getCrashLogFile(context);
        FileInputStream fis = null;
        try {
            if (file.exists()) {
                byte[] data = new byte[(int) file.length()];
                fis = new FileInputStream(file);
                fis.read(data);
                CrashLog = new String(data);
                CrashLog = new String(data, Charset.forName("utf-8"));
                fis.close();
            }
        } catch (Exception e) {
            ErrorUtils.onException(e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                ErrorUtils.onException(e);
            }
        }

        return CrashLog;
    }

    /**
     * 返回Crash Log文件；
     */
    public static File getCrashLogFile(Context context) {
        return new File(context.getExternalFilesDir("log"), "crashLog");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void clearCrashLog(Context context) {
        File file = getCrashLogFile(context);
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception ignored) {

            }
        }
    }

    /**
     * 设置崩溃时的回调
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

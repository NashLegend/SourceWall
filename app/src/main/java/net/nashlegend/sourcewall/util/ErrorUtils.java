package net.nashlegend.sourcewall.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.BuildConfig;
import net.nashlegend.sourcewall.request.ResponseObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by NashLegend on 16/7/5.
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ErrorUtils {

    public static void onException(Exception e) {
        onException(e, "default no message");
    }

    public static void onException(Exception e, String message) {
        if (BuildConfig.DEBUG) {
            Logger.e(e, message);
        }
    }

    /**
     * @param ro
     */
    public static void dumpRequestError(Throwable ex, ResponseObject ro) {
        if (BuildConfig.DEBUG) {
            dumpRequestError(ro, getErrorReport(ex, ro));
        }
    }

    private static String getErrorReport(@Nullable Throwable ex, @NonNull ResponseObject ro) {
        StringBuilder err = new StringBuilder();
        try {
            err.append("Is Wifi Connected:").append(DeviceUtil.isWifiConnected()).append("\n");
            err.append("Is Mobile Network Connected:").append(DeviceUtil.isMobileNetworkConnected()).append("\n\n");
            err.append(ro.dump());
            if (ex != null) {
                err.append("\n================================= \n\n");
                final Writer result = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(result);
                ex.printStackTrace(printWriter);
                String stacktrace = result.toString();
                err.append(stacktrace)
                        .append("\n")
                        .append("Cause : \n")
                        .append("\n================================= \n\n");
                Throwable cause = ex.getCause();
                while (cause != null) {
                    cause.printStackTrace(printWriter);
                    err.append(result.toString());
                    cause = cause.getCause();
                }
                result.close();
                printWriter.close();
            }
            if (BuildConfig.DEBUG) {
                Logger.e(err.toString());
            }
        } catch (Exception e) {
            ErrorUtils.onException(e);
        }
        return err.toString();
    }

    private static String dumpResponseObject(ResponseObject responseObject) {
        return responseObject.dump();
    }

    synchronized private static void dumpRequestError(ResponseObject ro, String info) {
        if (TextUtils.isEmpty(info)) {
            return;
        }
        OutputStreamWriter writer = null;
        BufferedWriter out = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS", Locale.CHINA);
            GregorianCalendar calendar = new GregorianCalendar();
            String tempName = format.format(new Date(calendar.getTimeInMillis()));
            if (ro != null && ro.requestObject != null && ro.requestObject.url != null) {
                tempName += "_" + ro.requestObject.url.replaceAll("/", "_").replaceAll(":", "") + ".txt";
            } else {
                tempName += "_other" + ".txt";
            }
            File file = new File(App.getApp().getExternalFilesDir("log" + File.separator + "request"), tempName);
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
}

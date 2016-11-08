package net.nashlegend.sourcewall.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.orhanobut.logger.Logger;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.BuildConfig;
import net.nashlegend.sourcewall.request.ResponseObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by NashLegend on 16/7/5.
 */

public class ErrorUtils {

    public static void onException(Throwable e) {
        onException(e, "default no message");
    }

    public static void onException(Throwable e, String message) {
        if (BuildConfig.DEBUG && e != null) {
            Logger.e(e, message);
        }
    }

    /**
     * @param responseObject
     */
    public static void dumpRequest(ResponseObject responseObject) {
        if (BuildConfig.DEBUG && responseObject != null) {
            String info = getRequestInfo(responseObject);
            if (TextUtils.isEmpty(info)) {
                return;
            }
            OutputStreamWriter writer = null;
            BufferedWriter out = null;
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS",
                        Locale.CHINA);
                GregorianCalendar calendar = new GregorianCalendar();
                String tempName = format.format(new Date(calendar.getTimeInMillis()));
                if (responseObject.requestObject != null
                        && responseObject.requestObject.url != null) {
                    tempName += "_" + responseObject.requestObject.url.replaceAll("/",
                            "_").replaceAll(":", "") + ".txt";
                } else {
                    tempName += "_other" + ".txt";
                }
                File file = new File(
                        App.getApp().getExternalFilesDir("log" + File.separator + "request"),
                        tempName);
                if (file.exists()) {
                    file.delete();
                } else {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
                writer = new OutputStreamWriter(new FileOutputStream(file),
                        Charset.forName("utf-8"));
                out = new BufferedWriter(writer, 4196);
                out.write(info);
                out.flush();
            } catch (Exception e) {
                ErrorUtils.onException(e);
            } finally {
                IOUtil.closeQuietly(out);
                IOUtil.closeQuietly(writer);
            }
        }
    }

    private static String getRequestInfo(@NonNull ResponseObject response) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append("Is Wifi Connected:").append(DeviceUtil.isWifiConnected()).append("\n");
            builder.append("Is Mobile Network Connected:").append(
                    DeviceUtil.isMobileNetworkConnected()).append("\n\n");
            builder.append(response.dump());
            if (response.throwable != null) {
                builder.append("\n================================= \n\n")
                        .append(Log.getStackTraceString(response.throwable))
                        .append("\n\n");
            }
            if (BuildConfig.DEBUG) {
                Logger.e(builder.toString());
            }
        } catch (Exception e) {
            ErrorUtils.onException(e);
        }
        return builder.toString();
    }
}

package net.nashlegend.sourcewall.util;

import java.io.Closeable;

/**
 * Created by NashLegend on 2016/10/28.
 */

public class IOUtil {
    public static boolean closeQuietly(Closeable closeable) {
        try {
            closeable.close();
            return true;
        } catch (Exception e) {
            ErrorUtils.onException(e);
            return false;
        }
    }
}

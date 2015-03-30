package net.nashlegend.sourcewall.util;

import android.graphics.Point;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by NashLegend on 2015/3/30 0030
 */
public class ImageSizeMap {

    private static final HashMap<String, Point> BitmapSizes = new HashMap<>();

    public static void put(String source, int width, int height) {
        put(source, new Point(width, height));
    }

    public static void put(String source, Point point) {
        if (!TextUtils.isEmpty(source) && point != null && point.x > 0 && point.y > 0) {
            BitmapSizes.put(source, point);
        }
    }

    public static Point get(String source) {
        return BitmapSizes.get(source);
    }
}

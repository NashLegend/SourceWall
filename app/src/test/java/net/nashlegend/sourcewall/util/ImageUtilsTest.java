package net.nashlegend.sourcewall.util;

import android.graphics.BitmapFactory;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by NashLegend on 16/9/1.
 */
public class ImageUtilsTest {
    @Test
    public void compressImage() throws Exception {
        float maxSize = 720;
        final int outWidth = 8888;
        final int outHeight = 6666;
        final int halfHeight = outHeight / 2;
        final int halfWidth = outWidth / 2;
        int sample = 1;
        while (halfWidth / sample > maxSize && halfHeight / sample > maxSize) {
            sample *= 2;
        }
        System.out.println(sample);
        System.out.println(outWidth / sample);
        System.out.println(outHeight / sample);
    }

}
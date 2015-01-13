/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squareup.picasso;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.net.NetworkInfo;

import java.io.IOException;

abstract class RequestHandler {
    public static final class Result {
        private final Picasso.LoadedFrom loadedFrom;
        private final Bitmap bitmap;
        private final int exifOrientation;

        public Result(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            this(bitmap, loadedFrom, 0);
        }

        Result(Bitmap bitmap, Picasso.LoadedFrom loadedFrom, int exifOrientation) {
            this.bitmap = bitmap;
            this.loadedFrom = loadedFrom;
            this.exifOrientation = exifOrientation;
        }

        /**
         * Returns the resulting {@link android.graphics.Bitmap} generated from a
         * load call.
         */
        public Bitmap getBitmap() {
            return bitmap;
        }

        /**
         * Returns the resulting {@link com.squareup.picasso.Picasso.LoadedFrom} generated from a
         * load call.
         */
        public Picasso.LoadedFrom getLoadedFrom() {
            return loadedFrom;
        }

        /**
         * Returns the resulting EXIF orientation generated from a
         * load call. This is only
         * accessible to built-in RequestHandlers.
         */
        int getExifOrientation() {
            return exifOrientation;
        }
    }

    public abstract boolean canHandleRequest(Request data);

    /**
     * Loads an image for the given {@link com.squareup.picasso.Request}.
     *
     * @param data the {@link android.net.Uri} to load the image from.
     * @return A {@link com.squareup.picasso.RequestHandler.Result} instance representing the result.
     */
    public abstract Result load(Request data, Cache cache) throws IOException;

    int getRetryCount() {
        return 0;
    }

    boolean shouldRetry(boolean airplaneMode, NetworkInfo info) {
        return false;
    }

    boolean supportsReplay() {
        return false;
    }

    /**
     * Lazily create {@link android.graphics.BitmapFactory.Options} based in given
     * {@link com.squareup.picasso.Request}, only instantiating them if needed.
     */
    public static Options createBitmapOptions(Request data) {
        final boolean justBounds = data.hasSize();
        final boolean hasConfig = data.config != null;
        Options options = null;
        if (justBounds || hasConfig) {
            options = new Options();
            options.inJustDecodeBounds = justBounds;
            if (hasConfig) {
                options.inPreferredConfig = data.config;
            }
        }
        return options;
    }

    static boolean requiresInSampleSize(Options options) {
        return options != null && options.inJustDecodeBounds;
    }

    static void calculateInSampleSize(int reqWidth, int reqHeight, Options options, Request request) {
        if (reqHeight <= 0) {
            request.targetHeight = reqWidth * options.outHeight / options.outWidth;
            reqHeight = request.targetHeight;
        }
        calculateInSampleSize(reqWidth, reqHeight, options.outWidth, options.outHeight, options, request);
    }

    static void calculateInSampleSize(int reqWidth, int reqHeight, int width, int height,
                                      Options options, Request request) {
        int sampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            final int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            sampleSize = request.centerInside ? Math.max(heightRatio, widthRatio) : Math.min(heightRatio, widthRatio);
        }

        //宽高不能超过4096
        int mHeight = height / sampleSize;
        while (mHeight > 4096) {
            sampleSize *= 2;
            mHeight /= 2;
        }

        int mWidth = width / sampleSize;
        while (mWidth > 4096) {
            sampleSize *= 2;
            mWidth /= 2;
        }

        if (request.targetSizeAsMax) {
            mHeight = height / sampleSize;
            while (mHeight > reqHeight) {
                sampleSize *= 2;
                mHeight /= 2;
            }

            mWidth = width / sampleSize;
            while (mWidth > reqWidth) {
                sampleSize *= 2;
                mWidth /= 2;
            }
        }

        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
    }

    public static void ensureMemory(Options options, Cache cache, Config config) {
        int multip = 4;
        if (config != null) {
            switch (config) {
                case RGB_565:
                    multip = 2;
                    break;
                case ALPHA_8:
                    multip = 1;
                    break;
                case ARGB_8888:
                    multip = 4;
                    break;

                default:
                    break;
            }
        }

        long bmpSize = options.outWidth * options.outHeight * multip / (options.inSampleSize * options.inSampleSize);
        if (bmpSize > Utils.getFreeMemorySize()) {
            int maxSize = (int) (cache.size() - bmpSize + Utils.getFreeMemorySize());
            cache.trimToSize(maxSize);
        }
    }
}

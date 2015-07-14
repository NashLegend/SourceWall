/*
 * Copyright (C) 2013 Square, Inc.
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

import android.app.Notification;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fluent API for building an image download request.
 */
@SuppressWarnings("UnusedDeclaration")
// Public API.
public class RequestCreator {
    private static int nextId = 0;

    private static int getRequestId() {
        if (Utils.isMain()) {
            return nextId++;
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger id = new AtomicInteger();
        Picasso.HANDLER.post(new Runnable() {
            @Override
            public void run() {
                id.set(getRequestId());
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (final InterruptedException e) {
            Picasso.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    throw new RuntimeException(e);
                }
            });
        }
        return id.get();
    }

    private final Picasso picasso;
    private final Request.Builder mRequestBuilder;

    private boolean skipMemoryCache;
    private boolean updateMemoryCache;
    private boolean noFade;
    private boolean fitFlag;
    private boolean canPixelSample;
    private float pixelDensity = -1;
    private int placeholderResId;
    private int errorResId;
    private Drawable placeholderDrawable;
    private Drawable errorDrawable;
    private Object tag;

    RequestCreator(Picasso picasso, Uri uri, int resourceId) {
        if (picasso.shutdown) {
            throw new IllegalStateException(
                    "Picasso instance already shut down. Cannot submit new requests.");
        }
        this.picasso = picasso;
        this.mRequestBuilder = new Request.Builder(uri, resourceId);
    }

    /**
     * A placeholder drawable to be used while the image is being loaded. If the
     * requested image is not immediately available in the memory cache then
     * this resource will be set on the target {@link android.widget.ImageView}.
     */
    public RequestCreator placeholder(int placeholderResId) {
        if (placeholderResId == 0) {
            throw new IllegalArgumentException("Placeholder image resource invalid.");
        }
        if (placeholderDrawable != null) {
            throw new IllegalStateException("Placeholder image already set.");
        }
        this.placeholderResId = placeholderResId;
        return this;
    }

    /**
     * A placeholder drawable to be used while the image is being loaded. If the
     * requested image is not immediately available in the memory cache then
     * this resource will be set on the target {@link android.widget.ImageView}.
     * <p/>
     * If you are not using a placeholder image but want to clear an existing
     * image (such as when used in an {@link android.widget.Adapter adapter}),
     * pass in {@code null}.
     */
    public RequestCreator placeholder(Drawable placeholderDrawable) {
        if (placeholderResId != 0) {
            throw new IllegalStateException("Placeholder image already set.");
        }
        this.placeholderDrawable = placeholderDrawable;
        return this;
    }

    /**
     * An error drawable to be used if the request image could not be loaded.
     */
    public RequestCreator error(int errorResId) {
        if (errorResId == 0) {
            throw new IllegalArgumentException("Error image resource invalid.");
        }
        if (errorDrawable != null) {
            throw new IllegalStateException("Error image already set.");
        }
        this.errorResId = errorResId;
        return this;
    }

    /**
     * An error drawable to be used if the request image could not be loaded.
     */
    public RequestCreator error(Drawable errorDrawable) {
        if (errorDrawable == null) {
            throw new IllegalArgumentException("Error image may not be null.");
        }
        if (errorResId != 0) {
            throw new IllegalStateException("Error image already set.");
        }
        this.errorDrawable = errorDrawable;
        return this;
    }

    /**
     * Assign a tag to this request. Tags are an easy way to logically associate
     * related requests that can be managed together e.g. paused, resumed, or
     * canceled.
     * <p/>
     * You can either use simple {@link String} tags or objects that naturally
     * define the scope of your requests within your app such as a
     * {@link android.content.Context}, an {@link android.app.Activity}, or a
     * {@link android.app.Fragment}.
     * <p/>
     * <strong>WARNING:</strong>: Picasso will keep a reference to the tag for
     * as long as this tag is paused and/or has active requests. Look out for
     * potential leaks.
     *
     * @see com.squareup.picasso.Picasso#cancelTag(Object)
     * @see com.squareup.picasso.Picasso#pauseTag(Object)
     * @see com.squareup.picasso.Picasso#resumeTag(Object)
     */
    public RequestCreator tag(Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag invalid.");
        }
        if (this.tag != null) {
            throw new IllegalStateException("Tag already set.");
        }
        this.tag = tag;
        return this;
    }

    public RequestCreator setLowerDensity() {
        return setPixelDensity(1f);
    }

    public RequestCreator setiPhoneDensity() {
        return setPixelDensity(2f);
    }

    public RequestCreator setPixelDensity(float d) {
        if (d > 0) {
            pixelDensity = d;
        } else {
            pixelDensity = -1;
        }
        return this;
    }

    /**
     * Attempt to resize the image to fit exactly into the target
     * {@link android.widget.ImageView}'s bounds. This will result in delayed execution of the
     * request until the {@link android.widget.ImageView} has been laid out.
     * <p/>
     * <em>Note:</em> This method works only when your target is an
     * {@link android.widget.ImageView}.
     */
    public RequestCreator fit() {
        fitFlag = true;
        canPixelSample = true;
        return this;
    }

    /**
     * Internal use only. Used by
     * {@link DeferredRequestCreator}.
     */
    RequestCreator unfit() {
        fitFlag = false;
        return this;
    }

    /**
     * Resize the image to the specified dimension size.
     */
    public RequestCreator resizeDimen(int targetWidthResId, int targetHeightResId) {
        canPixelSample = true;
        Resources resources = picasso.context.getResources();
        int targetWidth = resources.getDimensionPixelSize(targetWidthResId);
        int targetHeight = resources.getDimensionPixelSize(targetHeightResId);
        return resize(targetWidth, targetHeight);
    }

    /**
     * Resize the image to the specified size in pixels.
     */
    public RequestCreator resize(int targetWidth, int targetHeight) {
        mRequestBuilder.resize(targetWidth, targetHeight);
        return this;
    }

    /**
     * 是否将targetWidth和targetHeight当作maxWidth和maxHeight。
     * 如果设置为true，最终出来的图片大小将不会是targetWidth * targetHeight，而将小于这个尺寸
     * 如果没有设置targetWidth和targetHeight，那么设置这个没用
     *
     * @param targetAsMax
     * @return
     */
    public RequestCreator setTargetSizeAsMax(boolean targetAsMax) {
        mRequestBuilder.setTargetSizeAsMax(targetAsMax);
        return this;
    }

    /**
     * Crops an image inside of the bounds specified by
     * {@link #resize(int, int)} rather than distorting the aspect ratio. This
     * cropping technique scales the image so that it fills the requested bounds
     * and then crops the extra.
     */
    public RequestCreator centerCrop() {
        mRequestBuilder.centerCrop();
        return this;
    }

    /**
     * Centers an image inside of the bounds specified by
     * {@link #resize(int, int)}. This scales the image so that both dimensions
     * are equal to or less than the requested bounds.
     */
    public RequestCreator centerInside() {
        mRequestBuilder.centerInside();
        return this;
    }

    /**
     * Rotate the image by the specified degrees.
     */
    public RequestCreator rotate(float degrees) {
        mRequestBuilder.rotate(degrees);
        return this;
    }

    /**
     * Rotate the image by the specified degrees around a pivot point.
     */
    public RequestCreator rotate(float degrees, float pivotX, float pivotY) {
        mRequestBuilder.rotate(degrees, pivotX, pivotY);
        return this;
    }

    /**
     * Indicate that this action should not use the memory cache for attempting
     * to load or save the image. This can be useful when you know an image will
     * only ever be used once (e.g., loading an image from the filesystem and
     * uploading to a remote server).
     */
    public RequestCreator skipMemoryCache() {
        if (updateMemoryCache) {
            throw new IllegalStateException("updateMemoryCache been set, set skipMemory lead to updateMemoryCache inoperative");
        }
        skipMemoryCache = true;
        return this;
    }

    public RequestCreator updateMemoryCache() {
        if (skipMemoryCache) {
            throw new IllegalStateException("skipMemoryCache been set, so updateMemoryCache inoperative");
        }
        updateMemoryCache = true;
        return this;
    }

    /**
     * Attempt to decode the image using the specified config.
     * <p/>
     * Note: This value may be ignored by {@link android.graphics.BitmapFactory}. See
     * {@link android.graphics.BitmapFactory.Options#inPreferredConfig its documentation} for
     * more details.
     */
    public RequestCreator config(Bitmap.Config config) {
        mRequestBuilder.config(config);
        return this;
    }

    /**
     * Set the priority of this request.
     * <p/>
     * This will affect the order in which the requests execute but does not
     * guarantee it. By default, all requests have
     * {@link com.squareup.picasso.Picasso.Priority#NORMAL} priority, except for {@link #fetch()}
     * requests, which have {@link com.squareup.picasso.Picasso.Priority#LOW} priority by default.
     */
    public RequestCreator priority(Picasso.Priority priority) {
        mRequestBuilder.priority(priority);
        return this;
    }

    /**
     * Add a custom transformation to be applied to the image.
     * <p/>
     * Custom transformations will always be run after the built-in
     * transformations.
     */
    // TODO show example of calling resize after a transform in the javadoc
    public RequestCreator transform(Transformation transformation) {
        mRequestBuilder.transform(transformation);
        return this;
    }

    /**
     * Disable brief fade in of images loaded from the disk cache or network.
     */
    public RequestCreator noFade() {
        noFade = true;
        return this;
    }

    public RequestCreator setCacheKey(String key) {
        mRequestBuilder.setCacheKey(key);
        return this;
    }

    public void download() {
        try {
            long started = System.nanoTime();
            Utils.checkNotMain();
            Request finalData = createRequest(started);
            String key = Utils.createKey(finalData, new StringBuilder());
            Action action = new GetAction(picasso, finalData, skipMemoryCache, updateMemoryCache, key, tag);
            BitmapHunter.forRequest(picasso, picasso.dispatcher, picasso.cache, picasso.stats, action).download();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InputStream getFileStream() {
        try {
            long started = System.nanoTime();
            Utils.checkNotMain();
            Request finalData = createRequest(started);
            String key = Utils.createKey(finalData, new StringBuilder());
            Action action = new GetAction(picasso, finalData, skipMemoryCache, updateMemoryCache, key, tag);
            return BitmapHunter.forRequest(picasso, picasso.dispatcher, picasso.cache, picasso.stats, action).getStream();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Synchronously fulfill this request. Must not be called from the main
     * thread.
     * <p/>
     * <em>Note</em>: The result of this operation is not cached in memory
     * because the underlying {@link Cache} implementation
     * is not guaranteed to be thread-safe.
     */
    public Bitmap get() throws IOException {
        long started = System.nanoTime();
        Utils.checkNotMain();

        if (fitFlag) {
            throw new IllegalStateException("Fit cannot be used with get.");
        }
        if (!mRequestBuilder.hasImage()) {
            return null;
        }

        Request finalData = createRequest(started);
        String key = Utils.createKey(finalData, new StringBuilder());

        Action action = new GetAction(picasso, finalData, skipMemoryCache, updateMemoryCache, key, tag);
        return BitmapHunter.forRequest(picasso, picasso.dispatcher, picasso.cache, picasso.stats, action).hunt();
    }

    /**
     * 异步预加载图片到内存 Asynchronously fulfills the request without a
     * {@link android.widget.ImageView} or {@link Target}. This is useful
     * when you want to warm up the cache with an image.
     * <p/>
     * <em>Note:</em> It is safe to invoke this method from any thread.
     */
    public void fetch() {
        long started = System.nanoTime();

        if (fitFlag) {
            throw new IllegalStateException("Fit cannot be used with fetch.");
        }
        if (mRequestBuilder.hasImage()) {
            // Fetch requests have lower priority by default.
            if (!mRequestBuilder.hasPriority()) {
                mRequestBuilder.priority(Picasso.Priority.LOW);
            }

            Request request = createRequest(started);
            String key = Utils.createKey(request, new StringBuilder());

            Action action = new FetchAction(picasso, request, skipMemoryCache, updateMemoryCache, key, tag);
            picasso.submit(action);
        }
    }

    /**
     * Asynchronously fulfills the request into the specified
     * {@link Target}. In most cases, you should use this
     * when you are dealing with a custom {@link android.view.View View} or view
     * holder which should implement the {@link Target}
     * interface.
     * <p/>
     * Implementing on a {@link android.view.View View}: <blockquote>
     * <p/>
     * <pre>
     * public class ProfileView extends FrameLayout implements Target {
     *   {@literal @}Override public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
     *     setBackgroundDrawable(new BitmapDrawable(bitmap));
     *   }
     *
     *   {@literal @}Override public void onBitmapFailed() {
     *     setBackgroundResource(R.drawable.profile_error);
     *   }
     *
     *   {@literal @}Override public void onPrepareLoad(Drawable placeHolderDrawable) {
     *     frame.setBackgroundDrawable(placeHolderDrawable);
     *   }
     * }
     * </pre>
     * <p/>
     * </blockquote> Implementing on a view holder object for use inside of an
     * adapter: <blockquote>
     * <p/>
     * <pre>
     * public class ViewHolder implements Target {
     *   public FrameLayout frame;
     *   public TextView name;
     *
     *   {@literal @}Override public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
     *     frame.setBackgroundDrawable(new BitmapDrawable(bitmap));
     *   }
     *
     *   {@literal @}Override public void onBitmapFailed() {
     *     frame.setBackgroundResource(R.drawable.profile_error);
     *   }
     *
     *   {@literal @}Override public void onPrepareLoad(Drawable placeHolderDrawable) {
     *     frame.setBackgroundDrawable(placeHolderDrawable);
     *   }
     * }
     * </pre>
     * <p/>
     * </blockquote>
     * <p/>
     * <em>Note:</em> This method keeps a weak reference to the
     * {@link Target} instance and will be garbage
     * collected if you do not keep a strong reference to it. To receive
     * callbacks when an image is loaded use
     * {@link #into(android.widget.ImageView, Callback)}.
     */
    public void into(Target target) {
        long started = System.nanoTime();
        Utils.checkMain();

        if (target == null) {
            throw new IllegalArgumentException("Target must not be null.");
        }
        if (fitFlag) {
            throw new IllegalStateException("Fit cannot be used with a Target.");
        }

        Drawable drawable = placeholderResId != 0 ? picasso.context.getResources().getDrawable(
                placeholderResId)
                : placeholderDrawable;

        if (!mRequestBuilder.hasImage()) {
            picasso.cancelRequest(target);
            target.onPrepareLoad(drawable);
            return;
        }

        Request request = createRequest(started);
        String requestKey = Utils.createKey(request);

        if (!skipMemoryCache) {
            Bitmap bitmap = picasso.quickMemoryCacheCheck(requestKey);
            if (bitmap != null) {
                picasso.cancelRequest(target);
                target.onBitmapLoaded(bitmap, Picasso.LoadedFrom.MEMORY);
                return;
            }
        }

        target.onPrepareLoad(drawable);

        Action action = new TargetAction(picasso, target, request, skipMemoryCache, updateMemoryCache, errorResId, errorDrawable,
                requestKey, tag);
        picasso.enqueueAndSubmit(action);
    }

    /**
     * Asynchronously fulfills the request into the specified
     * {@link android.widget.RemoteViews} object with the given {@code viewId}. This is used
     * for loading bitmaps into a {@link android.app.Notification}.
     */
    public void into(RemoteViews remoteViews, int viewId, int notificationId,
                     Notification notification) {
        long started = System.nanoTime();
        Utils.checkMain();

        if (remoteViews == null) {
            throw new IllegalArgumentException("RemoteViews must not be null.");
        }
        if (notification == null) {
            throw new IllegalArgumentException("Notification must not be null.");
        }
        if (fitFlag) {
            throw new IllegalStateException("Fit cannot be used with RemoteViews.");
        }
        if (placeholderDrawable != null || errorDrawable != null) {
            throw new IllegalArgumentException(
                    "Cannot use placeholder or error drawables with remote views.");
        }

        Request request = createRequest(started);
        String key = Utils.createKey(request);

        RemoteViewsAction action = new RemoteViewsAction.NotificationAction(picasso, request, remoteViews, viewId,
                notificationId, notification, skipMemoryCache, updateMemoryCache, errorResId, key, tag);
        performRemoteViewInto(action);
    }

    /**
     * Asynchronously fulfills the request into the specified
     * {@link android.widget.RemoteViews} object with the given {@code viewId}. This is used
     * for loading bitmaps into all instances of a widget.
     */
    public void into(RemoteViews remoteViews, int viewId, int[] appWidgetIds) {
        long started = System.nanoTime();
        Utils.checkMain();

        if (remoteViews == null) {
            throw new IllegalArgumentException("remoteViews must not be null.");
        }
        if (appWidgetIds == null) {
            throw new IllegalArgumentException("appWidgetIds must not be null.");
        }
        if (fitFlag) {
            throw new IllegalStateException("Fit cannot be used with remote views.");
        }
        if (placeholderDrawable != null || errorDrawable != null) {
            throw new IllegalArgumentException(
                    "Cannot use placeholder or error drawables with remote views.");
        }

        Request request = createRequest(started);
        String key = Utils.createKey(request);

        RemoteViewsAction action = new RemoteViewsAction.AppWidgetAction(picasso, request, remoteViews, viewId,
                appWidgetIds, skipMemoryCache, updateMemoryCache, errorResId, key, tag);
        performRemoteViewInto(action);
    }

    /**
     * Asynchronously fulfills the request into the specified {@link android.widget.ImageView}.
     * <p/>
     * <em>Note:</em> This method keeps a weak reference to the
     * {@link android.widget.ImageView} instance and will automatically support object
     * recycling.
     */
    public void into(ImageView target) {
        into(target, null);
    }

    /**
     * Asynchronously fulfills the request into the specified {@link android.widget.ImageView}
     * and invokes the target {@link Callback} if it's not
     * {@code null}.
     * <p/>
     * <em>Note:</em> The {@link Callback} param is a
     * strong reference and will prevent your {@link android.app.Activity} or
     * {@link android.app.Fragment} from being garbage collected. If you use
     * this method, it is <b>strongly</b> recommended you invoke an adjacent
     * {@link com.squareup.picasso.Picasso#cancelRequest(android.widget.ImageView)} call to prevent
     * temporary leaking.
     */
    public void into(ImageView target, Callback callback) {
        long started = System.nanoTime();
        Utils.checkMain();

        if (target == null) {
            throw new IllegalArgumentException("Target must not be null.");
        }

        if (!mRequestBuilder.hasImage()) {
            picasso.cancelRequest(target);
            PicassoDrawable.setPlaceholder(target, placeholderResId, placeholderDrawable);
            return;
        }

        if (fitFlag) {
            if (mRequestBuilder.hasSize()) {
                throw new IllegalStateException("Fit cannot be used with resize.");
            }
            int width = target.getWidth();
            int height = target.getHeight();
            if (width == 0 || height == 0) {
                PicassoDrawable.setPlaceholder(target, placeholderResId, placeholderDrawable);
                picasso.defer(target, new DeferredRequestCreator(this, target, callback));
                return;
            }
            mRequestBuilder.resize(width, height);
        }

        Request request = createRequest(started);
        String requestKey = Utils.createKey(request);

        if (!skipMemoryCache) {
            Bitmap bitmap = picasso.quickMemoryCacheCheck(requestKey);
            if (bitmap != null) {
                picasso.cancelRequest(target);
                PicassoDrawable.setBitmap(target, picasso.context, bitmap,
                        Picasso.LoadedFrom.MEMORY, noFade,
                        picasso.indicatorsEnabled);
                if (picasso.loggingEnabled) {
                    Utils.log(Utils.OWNER_MAIN, Utils.VERB_COMPLETED, request.plainId(), "from "
                            + Picasso.LoadedFrom.MEMORY);
                }
                if (callback != null) {
                    callback.onSuccess();
                }
                return;
            }
        }

        PicassoDrawable.setPlaceholder(target, placeholderResId, placeholderDrawable);

        Action action = new ImageViewAction(picasso, target, request, skipMemoryCache, updateMemoryCache, noFade, errorResId,
                errorDrawable, requestKey, tag, callback);
        picasso.enqueueAndSubmit(action);
    }

    /**
     * Create the request optionally passing it through the request transformer.
     */
    private Request createRequest(long started) {
        int id = getRequestId();

        if (canPixelSample && pixelDensity > 0) {
            float den = picasso.context.getResources().getDisplayMetrics().density;
            if (pixelDensity < den) {
                float downSize = pixelDensity / den;
                mRequestBuilder.downSample(downSize);
            }
        }

        Request request = mRequestBuilder.build();
        request.id = id;
        request.started = started;

        boolean loggingEnabled = picasso.loggingEnabled;
        if (loggingEnabled) {
            Utils.log(Utils.OWNER_MAIN, Utils.VERB_CREATED, request.plainId(), request.toString());
        }

        Request transformed = picasso.transformRequest(request);
        if (transformed != request) {
            // If the request was changed, copy over the id and timestamp from
            // the original.
            transformed.id = id;
            transformed.started = started;

            if (loggingEnabled) {
                Utils.log(Utils.OWNER_MAIN, Utils.VERB_CHANGED, transformed.logId(), "into "
                        + transformed);
            }
        }

        return transformed;
    }

    private void performRemoteViewInto(RemoteViewsAction action) {
        if (!skipMemoryCache) {
            Bitmap bitmap = picasso.quickMemoryCacheCheck(action.getKey());
            if (bitmap != null) {
                action.complete(bitmap, Picasso.LoadedFrom.MEMORY);
                return;
            }
        }

        if (placeholderResId != 0) {
            action.setImageResource(placeholderResId);
        }

        picasso.enqueueAndSubmit(action);
    }
}

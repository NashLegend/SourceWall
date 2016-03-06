package net.nashlegend.sourcewall.request;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import net.nashlegend.sourcewall.App;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RequestCache {

    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB

    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 50; // 50MB

    private static final int DISK_CACHE_INDEX = 0;

    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

    private DiskLruCache mDiskLruCache;
    private LruCache<String, String> mMemoryCache;
    private RequestCacheParams mCacheParams;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static RequestCache requestCache;

    private RequestCache(RequestCacheParams cacheParams) {
        init(cacheParams);
    }

    synchronized public static RequestCache getInstance() {
        if (requestCache == null) {
            requestCache = new RequestCache(new RequestCacheParams(App.getApp(), "request.cache"));
            requestCache.initDiskCache();
        }
        return requestCache;
    }

    private void init(RequestCacheParams cacheParams) {
        mCacheParams = cacheParams;
        if (mCacheParams.memoryCacheEnabled) {
            mMemoryCache = new LruCache<String, String>(mCacheParams.memCacheSize) {
                /**
                 * 以k计算cache
                 */
                @Override
                protected int sizeOf(String key, String value) {
                    final int bitmapSize = value.getBytes().length / 1024;
                    return bitmapSize == 0 ? 1 : bitmapSize;
                }
            };
        }

        if (cacheParams.initDiskCacheOnCreate) {
            initDiskCache();
        }
    }

    /**
     * 通常在开始时onCreate等
     */
    public void initDiskCache() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                File diskCacheDir = mCacheParams.diskCacheDir;
                if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
                    if (!diskCacheDir.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        diskCacheDir.mkdirs();
                    }
                    if (getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
                        try {
                            mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
                        } catch (final IOException e) {
                            mCacheParams.diskCacheDir = null;
                        }
                    }
                }
            }
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    int IO_BUFFER_SIZE = 8 * 1024;

    public void addStreamToCacheForceUpdate(String data, InputStream inputStream) {
        if (data == null || inputStream == null) {
            return;
        }
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    mDiskLruCache.remove(key);
                    final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                    if (editor != null) {
                        out = editor.newOutputStream(DISK_CACHE_INDEX);
                        byte[] buff = new byte[IO_BUFFER_SIZE];
                        int len;
                        while ((len = inputStream.read(buff)) != -1) {
                            out.write(buff, 0, len);
                        }
                        editor.commit();
                        out.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 返回缓存的文件地址
     *
     * @param data
     * @return
     */
    @Nullable
    public String getCachedFile(String data) {
        File file = new File(getDiskCacheDir(App.getApp(), "request.cache"), hashKeyForDisk(data) + ".0");
        if (file.exists()) {
            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    public void addStringToCacheForceUpdate(String data, String value) {
        if (data == null || value == null) {
            return;
        }
        if (mMemoryCache != null) {
            mMemoryCache.put(data, value);
        }
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    mDiskLruCache.remove(key);
                    final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                    if (editor != null) {
                        out = editor.newOutputStream(DISK_CACHE_INDEX);
                        out.write(value.getBytes("utf-8"));
                        editor.commit();
                        out.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void addStringToCache(String data, String value) {
        if (data == null || value == null) {
            return;
        }
        if (mMemoryCache != null) {
            mMemoryCache.put(data, value);
        }
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            out.write(value.getBytes("utf-8"));
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Nullable
    public String getStringFromCache(String data) {
        if (mMemoryCache != null) {
            String memValue = mMemoryCache.get(data);
            if (memValue != null) {
                return memValue;
            }
        }
        final String key = hashKeyForDisk(data);
        String request = null;
        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mDiskLruCache != null) {
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        request = snapshot.getString(DISK_CACHE_INDEX);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return request;
        }
    }

    public void clear() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mDiskLruCache = null;
                initDiskCache();
            }
        }
    }

    /**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.通常在onPause时执行
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Closes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.通常在onDestroy时执行
     */
    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * A holder class that contains cache parameters.
     */
    public static class RequestCacheParams {
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        public File diskCacheDir;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
        public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

        public RequestCacheParams(Context context, String diskCacheDirectoryName) {
            diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
        }

        public void setMemCacheSizePercent(float percent) {
            if (percent < 0.01f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be " + "between 0.01 and 0.8 (inclusive)");
            }
            memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
        }
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        File file = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable()) {
            file = getExternalCacheDir(context);
        }
        if (file == null || (!file.exists() && !file.mkdirs())) {
            file = context.getCacheDir();
        }
        return new File(file, uniqueName);
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     * otherwise.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isExternalStorageRemovable() {
        return !Utils.hasGingerbread() || Environment.isExternalStorageRemovable();
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
        if (Utils.hasFroyo()) {
            return context.getExternalCacheDir();
        }
        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static long getUsableSpace(File path) {
        if (Utils.hasGingerbread()) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;

    //执行flush等操作有可能还没有完成初始化requestCache？

    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer) params[0]) {
                case MESSAGE_CLEAR:
                    if (requestCache != null) {
                        requestCache.clear();
                    }
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    if (requestCache != null) {
                        requestCache.initDiskCache();
                    }
                    break;
                case MESSAGE_FLUSH:
                    if (requestCache != null) {
                        requestCache.flush();
                    }
                    break;
                case MESSAGE_CLOSE:
                    if (requestCache != null) {
                        requestCache.close();
                        requestCache = null;
                    }
                    break;
            }
            return null;
        }
    }

}

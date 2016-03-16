package net.nashlegend.sourcewall.swrequest;

import android.support.annotation.NonNull;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.util.HashMap;

/**
 * Created by NashLegend on 16/3/16.
 */
public class RequestDelegate {

    private OkHttpClient defaultHttpClient;

    /**
     * 取消相关tag的请求
     *
     * @param tag
     */
    public void cancelRequestByTag(Object tag) {
        getDefaultHttpClient().cancel(tag);
    }

    public RequestDelegate(@NonNull OkHttpClient defaultHttpClient) {
        this.defaultHttpClient = defaultHttpClient;
    }

    public OkHttpClient getDefaultHttpClient() {
        return defaultHttpClient;
    }

    public Call get(String url, Object tag) throws Exception {
        Request request = new Request.Builder().get().url(url).tag(tag).build();
        return getDefaultHttpClient().newCall(request);
    }

    public Call get(String url, HashMap<String, String> params, Object
            tag) throws Exception {
        StringBuilder paramString = new StringBuilder("");
        if (params == null) {
            params = new HashMap<>();
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return get(url + paramString.toString(), tag);
    }

    public Call post(String url, HashMap<String, String> params, Object
            tag) throws Exception {
        if (params == null) {
            params = new HashMap<>();
        }
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().post(formBody).url(url).tag(tag).build();
        return getDefaultHttpClient().newCall(request);
    }

    public Call put(String url, HashMap<String, String> params, Object
            tag) throws Exception {
        if (params == null) {
            params = new HashMap<>();
        }
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().put(formBody).url(url).tag(tag).build();
        return getDefaultHttpClient().newCall(request);
    }

    public Call put(String url, Object tag) throws Exception {
        return put(url, null, tag);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有……
     */
    public Call delete(String url, Object tag) throws Exception {
        Request request = new Request.Builder().delete().url(url).tag(tag).build();
        return getDefaultHttpClient().newCall(request);
    }

    public Call delete(String url, HashMap<String, String> params,
                       Object tag) throws Exception {
        StringBuilder paramString = new StringBuilder("");
        if (params == null) {
            params = new HashMap<>();
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return delete(url + paramString.toString(), tag);
    }

    synchronized public Call getAsync(String url, Callback defCallBack, Object tag) {
        Request request = new Request.Builder().get().url(url).tag(tag).build();
        Call call = getDefaultHttpClient().newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public Call getAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        StringBuilder paramString = new StringBuilder("");
        if (params == null) {
            params = new HashMap<>();
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return getAsync(url + paramString.toString(), defCallBack, tag);
    }

    synchronized public Call postAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        if (params == null) {
            params = new HashMap<>();
        }
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        Request request = new Request.Builder().post(builder.build()).url(url).tag(tag).build();
        Call call = getDefaultHttpClient().newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public Call putAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        if (params == null) {
            params = new HashMap<>();
        }
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().put(formBody).url(url).tag(tag).build();
        Call call = getDefaultHttpClient().newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public Call putAsync(String url, Callback defCallBack, Object tag) {
        return putAsync(url, null, defCallBack, tag);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有……
     */
    synchronized public Call deleteAsync(String url, Callback defCallBack, Object tag) {
        Request request = new Request.Builder().delete().url(url).tag(tag).build();
        Call call = getDefaultHttpClient().newCall(request);
        call.enqueue(defCallBack);
        return call;
    }

    synchronized public Call deleteAsync(String url, HashMap<String, String> params, Callback defCallBack, Object tag) {
        StringBuilder paramString = new StringBuilder("");
        if (params == null) {
            params = new HashMap<>();
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return deleteAsync(url + paramString.toString(), defCallBack, tag);
    }
}

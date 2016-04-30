package net.nashlegend.sourcewall.request;

import android.text.TextUtils;

import net.nashlegend.sourcewall.request.api.UserAPI;

import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class HttpFetcher {

    public static ResponseObject<String> get(String url) throws Exception {
        ResponseObject<String> ResponseObject = new ResponseObject<>();
        Request request = new Request.Builder().get().url(url).build();
        Response response = HttpUtil.getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        ResponseObject.statusCode = statusCode;
        ResponseObject.result = result;
        return ResponseObject;
    }

    public static ResponseObject<String> get(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        StringBuilder paramString = new StringBuilder("");
        String token = UserAPI.getToken();
        if (params == null) {
            params = new HashMap<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.put("access_token", token);
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return get(url + paramString.toString());
    }

    public static ResponseObject<String> get(String url, HashMap<String, String> params) throws Exception {
        return get(url, params, true);
    }

    public static ResponseObject<String> post(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        ResponseObject<String> ResponseObject = new ResponseObject<>();
        String token = UserAPI.getToken();
        if (params == null) {
            params = new HashMap<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.put("access_token", token);
        }

        FormBody.Builder builder = new FormBody.Builder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().post(formBody).url(url).build();
        Response response = HttpUtil.getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        ResponseObject.statusCode = statusCode;
        ResponseObject.result = result;
        return ResponseObject;
    }

    public static ResponseObject<String> post(String url, HashMap<String, String> params) throws Exception {
        return post(url, params, true);
    }

    public static ResponseObject<String> put(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        ResponseObject<String> ResponseObject = new ResponseObject<>();
        String token = UserAPI.getToken();
        if (params == null) {
            params = new HashMap<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.put("access_token", token);
        }
        FormBody.Builder builder = new FormBody.Builder();
        if (params.size() > 0) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder().put(formBody).url(url).build();
        Response response = HttpUtil.getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        ResponseObject.statusCode = statusCode;
        ResponseObject.result = result;
        return ResponseObject;
    }

    public static ResponseObject<String> put(String url) throws Exception {
        return put(url, null, true);
    }

    public static ResponseObject<String> put(String url, HashMap<String, String> params) throws Exception {
        return put(url, params, true);
    }

    /**
     * Delete 也是需要RequestBody的，然而这里并没有……
     */
    public static ResponseObject<String> delete(String url) throws Exception {
        ResponseObject<String> ResponseObject = new ResponseObject<>();
        Request request = new Request.Builder().delete().url(url).build();
        Response response = HttpUtil.getDefaultHttpClient().newCall(request).execute();
        int statusCode = response.code();
        String result = response.body().string();
        ResponseObject.statusCode = statusCode;
        ResponseObject.result = result;
        return ResponseObject;
    }

    public static ResponseObject<String> delete(String url, HashMap<String, String> params) throws Exception {
        return delete(url, params, true);
    }

    public static ResponseObject<String> delete(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        StringBuilder paramString = new StringBuilder("");
        String token = UserAPI.getToken();
        if (params == null) {
            params = new HashMap<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.put("access_token", token);
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return delete(url + paramString.toString());
    }
}

package com.example.sourcewall.connection.api;

import com.example.sourcewall.AppApplication;
import com.example.sourcewall.connection.HttpFetcher;
import com.example.sourcewall.connection.ResultObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class APIBase {

    public APIBase() {
        // TODO Auto-generated constructor stub
    }

    public static ResultObject uploadImage(String path, boolean watermark) {
        ResultObject resultObject = new ResultObject();
        File file = new File(path);
        if (file != null && file.exists() && !file.isDirectory() && file.length() >= 0) {
            HttpClient httpClient = HttpFetcher.getDefaultHttpClient();
            httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                    HttpVersion.HTTP_1_1);
            HttpPost httpPost = new HttpPost(
                    "http://www.guokr.com/apis/image.json?enable_watermark=" + (watermark ? "true" : "false"));
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("upload_file", file);
            builder.addTextBody("access_token", UserAPI.getToken());
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            HttpResponse response;
            String result = "";
            try {
                response = httpClient.execute(httpPost);
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, HTTP.UTF_8);
                    JSONObject object = new JSONObject(result);
                    if (getJsonBoolean(object, "ok")) {
                        JSONObject resultJson = getJsonObject(object, "result");
                        String url = getJsonString(resultJson, "url");
                        resultObject.ok = true;
                        resultObject.result = url;
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return resultObject;
    }

    public static int getJsonInt(JSONObject jsonObject, String key) throws JSONException {
        if ((jsonObject.has(key)) && (!jsonObject.isNull(key))) {
            return jsonObject.getInt(key);
        } else {
            return 0;
        }
    }

    public static boolean getJsonBoolean(JSONObject jsonObject, String key) throws JSONException {
        if ((jsonObject.has(key)) && (!jsonObject.isNull(key))) {
            return jsonObject.getBoolean(key);
        } else {
            return false;
        }
    }

    public static String getJsonString(JSONObject jsonObject, String key) throws JSONException {
        if ((jsonObject.has(key)) && (!jsonObject.isNull(key))) {
            return jsonObject.getString(key);
        } else {
            return "";
        }
    }

    public static JSONObject getJsonObject(JSONObject jsonObject, String key) throws JSONException {
        if ((jsonObject.has(key)) && (!jsonObject.isNull(key))) {
            return jsonObject.getJSONObject(key);
        } else {
            return new JSONObject();
        }
    }

    public static JSONArray getJsonArray(JSONObject jsonObject, String key) throws JSONException {
        if ((jsonObject.has(key)) && (!jsonObject.isNull(key))) {
            return jsonObject.getJSONArray(key);
        } else {
            return new JSONArray();
        }
    }

    public static JSONObject getUniversalJsonObject(String json) throws JSONException {
        ResultObject resultObject = new ResultObject();
        JSONObject object = new JSONObject(json);
        if (getJsonBoolean(object, "ok")) {
            resultObject.ok = getJsonBoolean(object, "ok");
            return getJsonObject(object, "result");
        }
        return null;
    }
}

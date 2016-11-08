package net.nashlegend.sourcewall.request;

import android.support.annotation.NonNull;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.ErrorUtils;
import net.nashlegend.sourcewall.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by NashLegend on 2015/9/23 0023.
 */
public class JsonHandler {

    @NonNull
    public static JSONObject getJsonObjectSafely(JSONObject jsonObject, String key)
            throws JSONException {
        JSONObject subObject = jsonObject.optJSONObject(key);
        if (subObject == null) {
            subObject = new JSONObject();
        }
        return subObject;
    }

    @NonNull
    public static JSONArray getJsonArraySafely(JSONObject jsonObject, String key)
            throws JSONException {
        JSONArray jsonArray = jsonObject.optJSONArray(key);
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }
        return jsonArray;
    }

    /**
     * 果壳json返回的格式是固定的，这个可以先判断是否成功并剥离出有用信息。
     * 这里还可以做一些token过期失败问题的处理，省得在每个地方都判断了。
     *
     * @param json 要进行json解析的文本内容
     * @return JSONObject
     */
    public static JSONObject getUniversalJsonObject(String json, ResponseObject responseObject)
            throws JSONException {
        JSONObject object = new JSONObject(json);
        if (object.optBoolean("ok")) {
            responseObject.ok = true;
            return getJsonObjectSafely(object, "result");
        } else {
            handleBadJson(object, responseObject);
        }
        return null;
    }

    /**
     * 果壳json返回的格式是固定的，这个可以先判断是否成功并剥离出有用信息。
     * 这里还可以做一些token过期失败问题的处理，省得在每个地方都判断了。
     *
     * @param json 要进行json解析的文本内容
     * @return JSONArray
     */
    public static JSONArray getUniversalJsonArray(String json, ResponseObject responseObject)
            throws JSONException {
        JSONObject object = new JSONObject(json);
        if (object.optBoolean("ok")) {
            responseObject.ok = true;
            return getJsonArraySafely(object, "result");
        } else {
            handleBadJson(object, responseObject);
        }
        return null;
    }

    /**
     * 果壳json返回的格式是固定的，这个可以先判断是否成功并剥离出有用信息。
     * 这里还可以做一些token过期失败问题的处理，省得在每个地方都判断了。
     *
     * @param json 要进行json解析的文本内容
     * @return 是否为true
     */
    public static boolean getUniversalJsonSimpleBoolean(String json, ResponseObject responseObject)
            throws JSONException {
        JSONObject object = new JSONObject(json);
        if (object.optBoolean("ok")) {
            responseObject.ok = true;
            return true;
        } else {
            handleBadJson(object, responseObject);
        }
        return false;
    }

    /**
     * json返回的格式是固定的，这个可以先判断是否成功并剥离出有用信息。
     * 这里还可以做一些token过期失败问题的处理，省得在每个地方都判断了。
     * <p/>
     * 直接返回
     *
     * @param json 要进行json解析的文本内容
     * @return 是否为true
     */
    public static String getUniversalJsonSimpleString(String json, ResponseObject
            responseObject) throws JSONException {
        JSONObject object = new JSONObject(json);
        if (object.optBoolean("ok")) {
            responseObject.ok = true;
            return object.getString("content");
        } else {
            handleBadJson(object, responseObject);
            return "";
        }
    }

    /**
     * Bad Json指返回结果不为true的，而不是格式不对的
     */
    public static void handleBadJson(JSONObject object, ResponseObject responseObject)
            throws JSONException {
        responseObject.ok = false;
        int error_code = object.optInt("error_code", ResponseCode.CODE_UNKNOWN);
        responseObject.message = object.optString("error");
        responseObject.code = error_code;
        switch (responseObject.code) {
            case ResponseCode.CODE_TOKEN_INVALID:
                ToastUtil.toastBigSingleton(R.string.token_invalid);
                UserAPI.logout();
                break;
            default:
                responseObject.code = ResponseCode.CODE_UNKNOWN;
                break;
        }
    }

    /**
     * 处理所有请求的错误信息
     *
     * @param e        要处理的错误信息
     * @param response ResponseObject
     */
    public static void handleRequestException(Throwable e, ResponseObject response) {
        response.ok = false;
        response.throwable = e;
        if (e == null) {
            response.error_message = "unknown";
        } else {
            ErrorUtils.onException(e);
            response.error_message = e.getMessage();
            if (e instanceof IOException) {
                if (e instanceof SocketTimeoutException) {
                    response.error = ResponseError.TIME_OUT;
                } else {
                    response.error = ResponseError.NETWORK_ERROR;
                }
                if ("Cancelled".equals(e.getMessage())) {
                    response.error = ResponseError.CANCELLED;
                }
            } else if (e instanceof JSONException) {
                response.error = ResponseError.JSON_ERROR;
            } else {
                response.error = ResponseError.UNKNOWN;
            }
        }
    }
}

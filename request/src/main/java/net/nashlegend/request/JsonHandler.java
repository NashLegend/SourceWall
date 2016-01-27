package net.nashlegend.request;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by NashLegend on 2015/9/23 0023.
 */
public class JsonHandler {

    public static int getJsonInt(JSONObject jsonObject, String key) throws JSONException {
        if ((jsonObject.has(key)) && (!jsonObject.isNull(key))) {
            return jsonObject.getInt(key);
        } else {
            return 0;
        }
    }

    public static long getJsonLong(JSONObject jsonObject, String key) throws JSONException {
        if ((jsonObject.has(key)) && (!jsonObject.isNull(key))) {
            return jsonObject.getLong(key);
        } else {
            return 0L;
        }
    }

    public static boolean getJsonBoolean(JSONObject jsonObject, String key) throws JSONException {
        return (jsonObject.has(key)) && (!jsonObject.isNull(key)) && jsonObject.getBoolean(key);
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

    /**
     * 果壳json返回的格式是固定的，这个可以先判断是否成功并剥离出有用信息。
     * 这里还可以做一些token过期失败问题的处理，省得在每个地方都判断了。
     *
     * @param json 要进行json解析的文本内容
     * @return JSONObject
     * @throws JSONException
     */
    public static JSONObject getUniversalJsonObject(String json, ResponseObject responseObject) throws JSONException {
        JSONObject object = new JSONObject(json);
        if (getJsonBoolean(object, "ok")) {
            return getJsonObject(object, "result");
        } else {
            responseObject.ok = false;
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
     * @throws JSONException
     */
    public static JSONArray getUniversalJsonArray(String json, ResponseObject responseObject) throws JSONException {
        JSONObject object = new JSONObject(json);
        if (getJsonBoolean(object, "ok")) {
            //这里不处理responseObject.ok，因为返回后，其他地方可能报错
            return getJsonArray(object, "result");
        } else {
            responseObject.ok = false;
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
     * @throws JSONException
     */
    public static boolean getUniversalJsonSimpleBoolean(String json, ResponseObject responseObject) throws JSONException {
        JSONObject object = new JSONObject(json);
        if (getJsonBoolean(object, "ok")) {
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
     * @throws JSONException
     */
    public static String getUniversalJsonSimpleString(String json, ResponseObject
            responseObject) throws JSONException {
        JSONObject object = new JSONObject(json);

        if (getJsonBoolean(object, "ok")) {
            responseObject.ok = true;
            return object.getString("content");
        } else {
            handleBadJson(object, responseObject);
            return "";
        }
    }


    /**
     * Bad Json指返回结果不为true的，而不是格式不对的
     *
     * @param object
     * @param responseObject
     * @throws JSONException
     */
    public static void handleBadJson(JSONObject object, ResponseObject responseObject) throws JSONException {
        int error_code = getJsonInt(object, "error_code");
        responseObject.message = getJsonString(object, "error");
        switch (error_code) {
            case 200004:
                responseObject.code = ResponseCode.CODE_TOKEN_INVALID;
                // TODO: 16/1/28
//                UserAPI.clearMyInfo();
                break;
            case 240004:
                responseObject.code = ResponseCode.CODE_ALREADY_LIKED;
                break;
            case 242033:
                responseObject.code = ResponseCode.CODE_ALREADY_THANKED;
                break;
            case 242013:
                responseObject.code = ResponseCode.CODE_ALREADY_BURIED;
                break;
            default:
                responseObject.code = ResponseCode.CODE_UNKNOWN;
                break;
        }
    }

    /**
     * 处理所有请求的错误信息
     *
     * @param e              要处理的错误信息
     * @param responseObject ResponseObject
     */
    public static void handleRequestException(Exception e, ResponseObject responseObject) {
        responseObject.ok = false;
        if (e == null) {
            responseObject.error_message = "unknown";
            responseObject.error = ResponseError.UNKNOWN_ERROR;
        } else {
            e.printStackTrace();
            responseObject.error_message = e.getMessage();
            if (e instanceof IOException) {
                if (e instanceof SocketTimeoutException) {
                    responseObject.error = ResponseError.TIME_OUT;
                } else {
                    responseObject.error = ResponseError.NETWORK_ERROR;
                }
                if ("Cancelled".equals(e.getMessage())) {
                    responseObject.error = ResponseError.CANCELLED;
                }
            } else if (e instanceof JSONException) {
                responseObject.error = ResponseError.JSON_ERROR;
            } else {
                responseObject.error = ResponseError.UNKNOWN_ERROR;
            }
        }
    }
}

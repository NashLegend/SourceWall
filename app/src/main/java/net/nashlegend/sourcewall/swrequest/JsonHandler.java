package net.nashlegend.sourcewall.swrequest;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.events.LoginStateChangedEvent;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import de.greenrobot.event.EventBus;

/**
 * Created by NashLegend on 2015/9/23 0023.
 */
public class JsonHandler {

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
        if (object.optBoolean("ok")) {
            responseObject.ok = true;
            return getJsonObject(object, "result");
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
     * @throws JSONException
     */
    public static JSONArray getUniversalJsonArray(String json, ResponseObject responseObject) throws JSONException {
        JSONObject object = new JSONObject(json);
        if (object.optBoolean("ok")) {
            responseObject.ok = true;
            return getJsonArray(object, "result");
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
     * @throws JSONException
     */
    public static boolean getUniversalJsonSimpleBoolean(String json, ResponseObject responseObject) throws JSONException {
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
     * @throws JSONException
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
     *
     * @param object
     * @param responseObject
     * @throws JSONException
     */
    public static void handleBadJson(JSONObject object, ResponseObject responseObject) throws JSONException {
        //TODO 老版本是error，新版本是code，不再有error，以后再改，此处TODO
        responseObject.ok = false;
        int error_code = object.optInt("error_code", ResponseCode.CODE_UNKNOWN);
        responseObject.message = object.optString("error");
        responseObject.code = error_code;
        switch (responseObject.code) {
            case ResponseCode.CODE_TOKEN_INVALID:
                ToastUtil.toastSingleton(R.string.token_invalid);
                UserAPI.clearMyInfo();
                EventBus.getDefault().post(new LoginStateChangedEvent());
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
    public static void handleRequestException(Throwable e, ResponseObject responseObject) {
        responseObject.ok = false;
        responseObject.throwable = e;
        if (e == null) {
            responseObject.error_message = "unknown";
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
                responseObject.error = ResponseError.UNKNOWN;
            }
        }
    }
}

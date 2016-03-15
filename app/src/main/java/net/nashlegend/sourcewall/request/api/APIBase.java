package net.nashlegend.sourcewall.request.api;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.request.HttpFetcher;
import net.nashlegend.sourcewall.swrequest.RequestObject;
import net.nashlegend.sourcewall.swrequest.RequestObject.CallBack;
import net.nashlegend.sourcewall.swrequest.ResponseCode;
import net.nashlegend.sourcewall.swrequest.ResponseError;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.ImageUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class APIBase {

    public APIBase() {

    }

    /**
     * 统一回复，回复主题站、帖子、问题
     *
     * @return ResponseObject
     */
    @Nullable
    public static RequestObject<String> reply(AceModel data, String content, CallBack<String> callBack) {
        if (data instanceof Article) {
            return ArticleAPI.replyArticle(((Article) data).getId(), content + Config.getSimpleReplyTail(), callBack);
        } else if (data instanceof Post) {
            return PostAPI.replyPost(((Post) data).getId(), content + Config.getSimpleReplyTail(), callBack);
        } else if (data instanceof Question) {
            return QuestionAPI.answerQuestion(((Question) data).getId(), content + Config.getSimpleReplyTail(), callBack);
        } else {
            return null;
        }
    }

    /**
     * 上传图片
     * TODO 目前RequestBuilder尚未搞上传下载
     *
     * @param path      要上传图片的路径
     * @param watermark 是否打水印
     * @return 返回ResponseObject，resultObject.result是上传后的图片地址，果壳并不会对图片进行压缩
     */
    @Deprecated
    public static ResponseObject<String> uploadImage(String path, boolean watermark) {
        ResponseObject<String> resultObject = new ResponseObject<>();
        File file = new File(path);
        if (file.exists() && !file.isDirectory() && file.length() >= 0) {
            try {
                File tmpFile = new File(ImageUtils.compressImage(file.getAbsolutePath()));
                if (!tmpFile.equals(file)) {
                    file = tmpFile;
                }
                String uploadUrl = "http://www.guokr.com/apis/image.json?enable_watermark=" + String.valueOf(watermark);
                RequestBody requestBody = new MultipartBuilder().type(MultipartBuilder.FORM).addFormDataPart("access_token", UserAPI.getToken()).addFormDataPart("upload_file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file)).build();
                Request request = new Request.Builder().url(uploadUrl).post(requestBody).build();
                Response response = HttpFetcher.getDefaultUploadHttpClient().newCall(request).execute();
                if (response.isSuccessful()) {
                    JSONObject object = getUniversalJsonObject(response.body().string(), resultObject);
                    if (object != null) {
                        String url = object.optString("url", "");
                        resultObject.ok = true;
                        resultObject.result = url;
                    }
                }
            } catch (Exception e) {
                handleRequestException(e, resultObject);
            }
        }
        return resultObject;
    }

    /**
     * 使用github的接口转换markdown为html.
     * TODO 目前RequestBuilder尚未搞RequestBody
     *
     * @param text 要转换的文本内容
     * @return ResponseObject
     */
    @Deprecated
    public static ResponseObject<String> parseMarkdownByGitHub(String text) {

        ResponseObject<String> resultObject = new ResponseObject<>();
        if (TextUtils.isEmpty(text)) {
            resultObject.ok = true;
            resultObject.result = "";
        } else {
            String url = "https://api.github.com/markdown";
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("text", text);
                jsonObject.put("mode", "gfm");
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                Request request = new Request.Builder().post(body).url(url).build();
                Response response = HttpFetcher.getDefaultHttpClient().newCall(request).execute();
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    resultObject.ok = true;
                    resultObject.result = result;
                }
            } catch (Exception e) {
                handleRequestException(e, resultObject);
            }
        }
        return resultObject;
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
    @Deprecated
    public static JSONObject getUniversalJsonObject(String json, ResponseObject resultObject) throws JSONException {
        JSONObject object = new JSONObject(json);
        if (object.optBoolean("ok", false)) {
            return object.optJSONObject("result");
        } else {
            resultObject.ok = false;
            handleBadJson(object, resultObject);
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
    @Deprecated
    public static JSONArray getUniversalJsonArray(String json, ResponseObject resultObject) throws JSONException {
        JSONObject object = new JSONObject(json);
        if (object.optBoolean("ok", false)) {
            //这里不处理resultObject.ok，因为返回后，其他地方可能报错
            return object.optJSONArray("result");
        } else {
            resultObject.ok = false;
            handleBadJson(object, resultObject);
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
    public static boolean getUniversalJsonSimpleBoolean(String json, ResponseObject resultObject) throws JSONException {
        JSONObject object = new JSONObject(json);
        if (object.optBoolean("ok", false)) {
            resultObject.ok = true;
            return true;
        } else {
            handleBadJson(object, resultObject);
        }
        return false;
    }

    /**
     * Bad Json指返回结果不为true的，而不是格式不对的
     *
     * @param object
     * @param resultObject
     * @throws JSONException
     */
    public static void handleBadJson(JSONObject object, ResponseObject resultObject) throws JSONException {
        int error_code = object.optInt("error_code", ResponseCode.CODE_UNKNOWN);
        resultObject.code = ResponseCode.CODE_NONE;
        resultObject.message = object.optString("error", "");
        switch (error_code) {
            case 200004:
                resultObject.error = ResponseError.TOKEN_INVALID;
                UserAPI.clearMyInfo();
                break;
            case 240004:
                resultObject.error = ResponseError.ALREADY_LIKED;
                break;
            case 242033:
                resultObject.error = ResponseError.ALREADY_THANKED;
                break;
            case 242013:
                resultObject.error = ResponseError.ALREADY_BURIED;
                break;
            default:
                resultObject.error = ResponseError.UNKNOWN;
                break;
        }
    }

    /**
     * 处理所有请求的错误信息
     *
     * @param e            要处理的错误信息
     * @param resultObject ResponseObject
     */
    public static void handleRequestException(Exception e, ResponseObject resultObject) {
        e.printStackTrace();
        resultObject.ok = false;
        resultObject.error_message = e.getMessage();
        if (e instanceof IOException) {
            resultObject.error = ResponseError.NETWORK_ERROR;
        } else if (e instanceof JSONException) {
            resultObject.error = ResponseError.JSON_ERROR;
        } else {
            resultObject.error = ResponseError.UNKNOWN;
        }
    }

    /**
     * 将时间转换成可见的。话说果壳返回的时间格式是什么标准
     *
     * @param dateString 传入的时间字符串
     * @return 解析后的时间 yyyy-mm-dd hh:mm:ss
     */
    @SuppressLint("SimpleDateFormat")
    public static String parseDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateString.replace("T", " ").replaceAll("[\\+\\.]\\S+$", "");
        try {
            Date date = sdf.parse(time);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            GregorianCalendar now = new GregorianCalendar();
            int diff;
            if (now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                diff = calendar.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
            } else {
                int yearDiff = calendar.get(Calendar.YEAR) - now.get(Calendar.YEAR);
                if (yearDiff == 1) {
                    int max = now.isLeapYear(now.get(Calendar.YEAR)) ? 366 : 365;
                    diff = calendar.get(Calendar.DAY_OF_YEAR) + max - now.get(Calendar.DAY_OF_YEAR);
                } else if (yearDiff == -1) {
                    int max = calendar.isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365;
                    diff = calendar.get(Calendar.DAY_OF_YEAR) - max - now.get(Calendar.DAY_OF_YEAR);
                } else {
                    diff = (int) (calendar.getTimeInMillis() / 86400000 - new GregorianCalendar().getTimeInMillis() / 86400000);
                }
            }
            if (diff < -1 || diff > 0) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            } else if (diff == -1) {
                sdf = new SimpleDateFormat("昨天HH:mm");
            } else if (diff == 0) {
                sdf = new SimpleDateFormat("今天HH:mm");
            }
            time = sdf.format(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
}

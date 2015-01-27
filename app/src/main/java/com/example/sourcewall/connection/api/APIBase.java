package com.example.sourcewall.connection.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.text.TextUtils;

import com.example.sourcewall.AppApplication;
import com.example.sourcewall.connection.HttpFetcher;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.model.AceModel;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.model.Post;
import com.example.sourcewall.model.Question;
import com.example.sourcewall.util.Config;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class APIBase {

    public APIBase() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 统一回复，回复主题站、帖子、问题
     *
     * @return
     */
    public static ResultObject reply(AceModel data, String content) {
        ResultObject resultObject = new ResultObject();
        if (data instanceof Article) {
            return ArticleAPI.replyArticle(((Article) data).getId(), content + Config.getSimpleReplyTail());
        } else if (data instanceof Post) {
            return PostAPI.replyPost(((Post) data).getId(), content + Config.getSimpleReplyTail());
        } else if (data instanceof Question) {
            return QuestionAPI.answerQuestion(((Question) data).getId(), content + Config.getSimpleReplyTail());
        }
        return resultObject;
    }

    /**
     * 压缩图片，差不多可以压缩到140k左右
     *
     * @param path 要压缩的图片路径
     * @return 是否成功压缩
     * @throws IOException
     */
    public static String compressImage(String path) throws IOException {
        float maxSize = Config.getUploadImageSizeRestrict();//将其中一边至少压缩到maxSize，而不是两边都压缩到maxSize，否则有可能图片很不清楚
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        final int outWidth = options.outWidth;
        final int outHeight = options.outHeight;
        final int halfHeight = outHeight / 2;
        final int halfWidth = outWidth / 2;
        int sample = 1;
        while (halfWidth / sample > maxSize && halfHeight / sample > maxSize) {
            sample *= 2;
        }
        if (outWidth > maxSize && outHeight > maxSize) {
            options.inJustDecodeBounds = false;
            options.inSampleSize = sample;
            Bitmap finalBitmap = BitmapFactory.decodeFile(path, options);
            int finalWidth = finalBitmap.getWidth();
            int finalHeight = finalBitmap.getHeight();
            float scale = (finalWidth < finalHeight) ? maxSize / finalWidth : maxSize / finalHeight;
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            Bitmap compressedBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, finalWidth, finalHeight, matrix, false);

            String parentPath;
            File pFile = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                pFile = AppApplication.getApplication().getExternalCacheDir();
            }
            if (pFile == null) {
                pFile = AppApplication.getApplication().getCacheDir();
            }
            parentPath = pFile.getAbsolutePath();
            String cachePath = new File(parentPath, System.currentTimeMillis() + ".jpg").getAbsolutePath();
            FileOutputStream outputStream;
            outputStream = new FileOutputStream(cachePath);
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);//jpg速度远快于png，并且体积要小
            outputStream.flush();
            outputStream.close();
            return cachePath;
        } else {
            return path;
        }
    }

    /**
     * 上传图片
     *
     * @param path      要上传图片的路径
     * @param watermark 是否打水印
     * @return 返回ResultObject，resultObject.result是上传后的图片地址，果壳并不会对图片进行压缩
     */
    public static ResultObject uploadImage(String path, boolean watermark) {
        ResultObject resultObject = new ResultObject();
        File file = new File(path);
        if (file.exists() && !file.isDirectory() && file.length() >= 0) {
            try {
                File tmpFile = new File(compressImage(file.getAbsolutePath()));
                if (!tmpFile.equals(file)) {
                    file = tmpFile;
                }
                HttpClient httpClient = HttpFetcher.getDefaultUploadHttpClient();
                httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                        HttpVersion.HTTP_1_1);
                HttpPost httpPost = new HttpPost(
                        "http://www.guokr.com/apis/image.json?enable_watermark=" + (watermark ? "true" : "false"));
                MultipartEntity multipartEntity = new MultipartEntity();
                multipartEntity.addPart("upload_file", new FileBody(file));
                multipartEntity.addPart("access_token", new StringBody(UserAPI.getToken()));
                httpPost.setEntity(multipartEntity);
                HttpResponse response;
                String result = "";
                response = httpClient.execute(httpPost);
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, HTTP.UTF_8);
                    JSONObject object = getUniversalJsonObject(result, resultObject);
                    if (object != null) {
                        String url = getJsonString(object, "url");
                        resultObject.ok = true;
                        resultObject.result = url;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultObject;
    }

    /**
     * 使用github的接口转换markdown为html
     *
     * @param text
     * @return
     */
    public static ResultObject parseMarkdownByGitHub(String text) {

        ResultObject resultObject = new ResultObject();
        if (TextUtils.isEmpty(text)) {
            resultObject.ok = true;
            resultObject.result = "";
        } else {
            String url = "https://api.github.com/markdown";
            try {
                HttpClient httpClient = HttpFetcher.getDefaultHttpClient();
                httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                        HttpVersion.HTTP_1_1);
                HttpPost httpPost = new HttpPost(url);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("text", text);
                jsonObject.put("mode", "gfm");
                StringEntity entity = new StringEntity(jsonObject.toString(), HTTP.UTF_8);
                httpPost.setEntity(entity);
                HttpResponse response;

                response = httpClient.execute(httpPost);
                HttpEntity resEntity = response.getEntity();

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && resEntity != null) {
                    String result = EntityUtils.toString(resEntity, HTTP.UTF_8);
                    resultObject.ok = true;
                    resultObject.result = result;
                }
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

    /**
     * 果壳json返回的格式是固定的，这个可以先判断是否成功并剥离出有用信息。
     * 这里还可以做一些token过期失败问题的处理，省得在每个地方都判断了。
     *
     * @param json
     * @return
     * @throws JSONException
     */
    public static JSONObject getUniversalJsonObject(String json, ResultObject resultObject) throws JSONException {
        JSONObject object = new JSONObject(json);
        if (getJsonBoolean(object, "ok")) {
            resultObject.ok = getJsonBoolean(object, "ok");
            return getJsonObject(object, "result");
        } else {
            handleBadJson(object, resultObject);
        }
        return null;
    }

    /**
     * 果壳json返回的格式是固定的，这个可以先判断是否成功并剥离出有用信息。
     * 这里还可以做一些token过期失败问题的处理，省得在每个地方都判断了。
     *
     * @param json
     * @return
     * @throws JSONException
     */
    public static JSONArray getUniversalJsonArray(String json, ResultObject resultObject) throws JSONException {
        JSONObject object = new JSONObject(json);
        if (getJsonBoolean(object, "ok")) {
            resultObject.ok = getJsonBoolean(object, "ok");
            return getJsonArray(object, "result");
        } else {
            handleBadJson(object, resultObject);
        }
        return null;
    }

    /**
     * 果壳json返回的格式是固定的，这个可以先判断是否成功并剥离出有用信息。
     * 这里还可以做一些token过期失败问题的处理，省得在每个地方都判断了。
     *
     * @param json
     * @return
     * @throws JSONException
     */
    public static boolean getUniversalJsonSimpleBoolean(String json, ResultObject resultObject) throws JSONException {
        JSONObject object = new JSONObject(json);
        if (getJsonBoolean(object, "ok")) {
            resultObject.ok = getJsonBoolean(object, "ok");
            return true;
        } else {
            handleBadJson(object, resultObject);
        }
        return false;
    }

    public static void handleBadJson(JSONObject object, ResultObject resultObject) throws JSONException {
        int error_code = getJsonInt(object, "error_code");
        String error_msg = getJsonString(object, "error");
        resultObject.message = error_msg;
        switch (error_code) {
            case 200004:
                resultObject.code = ResultObject.ResultCode.CODE_TOKEN_INVALID;
                UserAPI.clearMyInfo();
                break;
            case 240004:
                resultObject.code = ResultObject.ResultCode.CODE_ALREADY_LIKED;
                break;
            default:
                resultObject.code = ResultObject.ResultCode.CODE_UNKNOWN;
                break;
        }
        //String invalidToken = " {\"error_code\": 200004, \"request_uri\": \"/apis/community/rn_num.json?_=1422011885139&access_token=51096037c7aa15ccd08c12c3fba8f856ae65d672cda50f25cec883343f3597a6\", \"ok\": false, \"error\": \"Illegal access token.\"}\n";
        //String alreadyLiked = "{\"error_code\": 240004, \"request_uri\": \"/apis/group/post_reply_liking.json\", \"ok\": false, \"error\": \"You have already liked this reply!\"}";

    }

    /**
     * 将时间转换成可见的。话说果壳返回的时间格式是什么标准
     *
     * @param dateString
     * @return
     */
    public static String parseDate(String dateString) {
        return dateString.replace("T", " ").replaceAll("[\\+\\.]\\S+$", "");
    }
}

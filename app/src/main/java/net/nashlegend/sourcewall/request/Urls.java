package net.nashlegend.sourcewall.request;

import android.text.TextUtils;

import java.util.List;

import okhttp3.HttpUrl;

/**
 * Created by NashLegend on 2015/10/6 0006.
 */
public class Urls {

    /**
     * 将地址与参数拼合,地址本身可以含有参数
     *
     * @param url
     * @param params
     * @return
     */
    public static String combine(String url, List<Param> params) {
        return HttpUrl.parse(url).newBuilder().query(getQueryString(params)).build().toString();
    }

    /**
     * 生成queryString,如:a=b&c=d&e=f
     *
     * @param params
     * @return
     */
    public static String getQueryString(List<Param> params) {
        StringBuilder paramString = new StringBuilder("");
        if (params != null && params.size() > 0) {
            for (Param param : params) {
                if (TextUtils.isEmpty(param.key) || TextUtils.isEmpty(param.value)) {
                    continue;
                }
                paramString.append(param.key).append("=").append(param.value).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return paramString.toString();
    }
}

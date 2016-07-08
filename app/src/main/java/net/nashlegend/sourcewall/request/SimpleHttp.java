package net.nashlegend.sourcewall.request;

import net.nashlegend.sourcewall.request.parsers.StringParser;

import java.util.HashMap;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class SimpleHttp {

    public static ResponseObject<String> get(String url) throws Exception {
        return get(url, null, true);
    }

    public static ResponseObject<String> get(String url, HashMap<String, String> params) throws Exception {
        return get(url, params, true);
    }

    public static ResponseObject<String> get(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        return new RequestBuilder<String>().get().url(url).params(params).withToken(needToken).parser(new StringParser()).requestSync();
    }

    public static ResponseObject<String> post(String url, HashMap<String, String> params) throws Exception {
        return post(url, params, true);
    }

    public static ResponseObject<String> post(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        return new RequestBuilder<String>().post().url(url).params(params).withToken(needToken).parser(new StringParser()).requestSync();
    }

    public static ResponseObject<String> put(String url) throws Exception {
        return put(url, null, true);
    }

    public static ResponseObject<String> put(String url, HashMap<String, String> params) throws Exception {
        return put(url, params, true);
    }

    public static ResponseObject<String> put(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        return new RequestBuilder<String>().put().url(url).params(params).withToken(needToken).parser(new StringParser()).requestSync();
    }

    public static ResponseObject<String> delete(String url) throws Exception {
        return delete(url, null, true);
    }

    public static ResponseObject<String> delete(String url, HashMap<String, String> params) throws Exception {
        return delete(url, params, true);
    }

    public static ResponseObject<String> delete(String url, HashMap<String, String> params, boolean needToken) throws Exception {
        return new RequestBuilder<String>().delete().url(url).params(params).withToken(needToken).parser(new StringParser()).requestSync();
    }
}

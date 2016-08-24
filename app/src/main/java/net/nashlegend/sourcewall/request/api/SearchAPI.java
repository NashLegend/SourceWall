package net.nashlegend.sourcewall.request.api;

import net.nashlegend.sourcewall.model.SearchItem;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.RequestBuilder;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.RequestObject.RequestCallBack;
import net.nashlegend.sourcewall.request.parsers.SearchListParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by NashLegend on 16/3/15.
 */
public class SearchAPI {

    public static final String TYPE_ALL = "all";
    public static final String TYPE_ARTICLE = "article";
    public static final String TYPE_POST = "post";
    public static final String TYPE_QUESTION = "question";
    public static final String TYPE_BLOG = "blog";

    /**
     * @param type    搜索内容类型
     * @param page    搜索第几页,从1开始
     * @param keyword 搜索的关键字
     * @return NetworkTask<ArrayList<SearchItem>>
     */
    public static NetworkTask<ArrayList<SearchItem>>
    getSearchedItems(String type, int page, String keyword, RequestCallBack<ArrayList<SearchItem>> callBack) {
        String url = "http://m.guokr.com/search/" + type + "/";
        return new RequestBuilder<ArrayList<SearchItem>>()
                .get()
                .url(url)
                .addParam("page", page)
                .addParam("wd", keyword)
                .withToken(false)
                .parser(new SearchListParser())
                .callback(callBack)
                .requestAsync();
    }
}

package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by NashLegend on 16/7/8.
 */

public class SimpleArticleParser implements Parser<Article> {
    @Override
    public Article parse(String response, ResponseObject<Article> responseObject) throws Exception {
        JSONArray articlesArray = JsonHandler.getUniversalJsonArray(response, responseObject);
        if (articlesArray == null) throw new NullPointerException("articlesArray is null");
        JSONObject articleObject = articlesArray.getJSONObject(0);
        return Article.fromJsonSimple(articleObject);
    }
}

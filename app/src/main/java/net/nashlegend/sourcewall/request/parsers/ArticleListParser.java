package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/5/2.
 */
public class ArticleListParser implements Parser<ArrayList<Article>> {
    @Override
    public ArrayList<Article> parse(String jString,
            ResponseObject<ArrayList<Article>> responseObject) throws Exception {
        ArrayList<Article> articleList = new ArrayList<>();
        JSONArray articles = JsonHandler.getUniversalJsonArray(jString, responseObject);
        assert articles != null;
        for (int i = 0; i < articles.length(); i++) {
            JSONObject jo = articles.getJSONObject(i);
            articleList.add(Article.fromJsonSimple(jo));
        }
        return articleList;
    }
}

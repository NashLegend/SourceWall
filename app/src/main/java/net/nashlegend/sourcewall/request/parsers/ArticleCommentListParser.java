package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/7/8.
 */

public class ArticleCommentListParser implements Parser<ArrayList<UComment>> {

    int offset = 0;
    String id = "";

    public ArticleCommentListParser(int offset, String id) {
        this.offset = offset;
        this.id = id;
    }

    @Override
    public ArrayList<UComment> parse(String response,
            ResponseObject<ArrayList<UComment>> responseObject) throws Exception {
        ArrayList<UComment> list = new ArrayList<>();
        JSONArray articles = JsonHandler.getUniversalJsonArray(response, responseObject);
        if (articles != null) {
            for (int i = 0; i < articles.length(); i++) {
                JSONObject jo = articles.getJSONObject(i);
                UComment comment = UComment.fromArticleJson(id, "", jo);
                comment.setFloor((offset + i + 1) + "楼");
                list.add(comment);
            }
        }
        return list;
    }
}

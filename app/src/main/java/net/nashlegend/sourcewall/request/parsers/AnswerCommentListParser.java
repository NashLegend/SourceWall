package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/5/2.
 */
public class AnswerCommentListParser implements Parser<ArrayList<UComment>> {
    @Override
    public ArrayList<UComment> parse(String response,
            ResponseObject<ArrayList<UComment>> responseObject) throws Exception {
        JSONArray comments = JsonHandler.getUniversalJsonArray(response, responseObject);
        ArrayList<UComment> list = new ArrayList<>();
        assert comments != null;
        for (int i = 0; i < comments.length(); i++) {
            JSONObject jsonObject = comments.getJSONObject(i);
            UComment comment = UComment.fromAnswerJson(jsonObject);
            list.add(comment);
        }
        return list;
    }
}

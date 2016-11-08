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

public class PostCommentListParser implements Parser<ArrayList<UComment>> {

    @Override
    public ArrayList<UComment> parse(String response,
            ResponseObject<ArrayList<UComment>> responseObject) throws Exception {
        ArrayList<UComment> list = new ArrayList<>();
        JSONArray comments = JsonHandler.getUniversalJsonArray(response, responseObject);
        assert comments != null;
        for (int i = 0; i < comments.length(); i++) {
            JSONObject jo = comments.getJSONObject(i);
            UComment comment = UComment.fromPostJson(jo);
            list.add(comment);
        }
        return list;
    }
}

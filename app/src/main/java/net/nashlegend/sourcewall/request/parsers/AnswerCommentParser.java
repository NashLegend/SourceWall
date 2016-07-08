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
public class AnswerCommentParser implements Parser<UComment> {
    @Override
    public UComment parse(String response, ResponseObject<UComment> responseObject) throws Exception {
        JSONObject jsonObject = JsonHandler.getUniversalJsonObject(response, responseObject);
        return UComment.fromAnswerJson(jsonObject);
    }
}

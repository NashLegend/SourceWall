package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONObject;

/**
 * Created by NashLegend on 16/5/2.
 */
public class QuestionCommentParser implements Parser<UComment> {
    @Override
    public UComment parse(String response, ResponseObject<UComment> responseObject) throws Exception {
        JSONObject jsonObject = JsonHandler.getUniversalJsonObject(response, responseObject);
        return UComment.fromQuestionJson(jsonObject);
    }
}

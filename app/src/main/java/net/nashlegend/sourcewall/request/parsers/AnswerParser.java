package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Message;
import net.nashlegend.sourcewall.model.QuestionAnswer;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONObject;

import static net.nashlegend.sourcewall.request.JsonHandler.getUniversalJsonObject;

/**
 * Created by NashLegend on 16/7/8.
 */

public class AnswerParser implements Parser<QuestionAnswer> {
    @Override
    public QuestionAnswer parse(String str, ResponseObject<QuestionAnswer> responseObject) throws Exception {
        return QuestionAnswer.fromJson(getUniversalJsonObject(str, responseObject));
    }
}

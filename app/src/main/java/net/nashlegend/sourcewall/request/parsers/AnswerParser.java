package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Answer;
import net.nashlegend.sourcewall.request.ResponseObject;

import static net.nashlegend.sourcewall.request.JsonHandler.getUniversalJsonObject;

/**
 * Created by NashLegend on 16/7/8.
 */

public class AnswerParser implements Parser<Answer> {
    @Override
    public Answer parse(String str, ResponseObject<Answer> responseObject) throws Exception {
        return Answer.fromJson(getUniversalJsonObject(str, responseObject));
    }
}

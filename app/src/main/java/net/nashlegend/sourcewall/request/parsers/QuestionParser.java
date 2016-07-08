package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.request.ResponseObject;

import static net.nashlegend.sourcewall.request.JsonHandler.getUniversalJsonObject;

/**
 * Created by NashLegend on 16/7/8.
 */

public class QuestionParser implements Parser<Question> {
    @Override
    public Question parse(String str, ResponseObject<Question> responseObject) throws Exception {
        return Question.fromJson(getUniversalJsonObject(str, responseObject));
    }
}

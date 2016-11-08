package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.request.ResponseObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/5/2.
 */
public class QuestionHtmlListParser implements Parser<ArrayList<Question>> {
    @Override
    public ArrayList<Question> parse(String response,
            ResponseObject<ArrayList<Question>> responseObject) throws Exception {
        ArrayList<Question> questions = Question.fromHtmlList(response);
        responseObject.ok = true;
        return questions;
    }
}

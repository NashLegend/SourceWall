package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/5/2.
 */
public class QuestionListParser implements Parser<ArrayList<Question>> {
    @Override
    public ArrayList<Question> parse(String response,
            ResponseObject<ArrayList<Question>> responseObject) throws Exception {
        ArrayList<Question> questions = new ArrayList<>();
        JSONArray results = JsonHandler.getUniversalJsonArray(response, responseObject);
        assert results != null;
        for (int i = 0; i < results.length(); i++) {
            JSONObject jsonObject;
            Object object = results.get(i);
            if (object instanceof JSONObject) {
                jsonObject = (JSONObject) object;
            } else {
                continue;
            }
            Question question = Question.fromJson(jsonObject);
            questions.add(question);
        }
        return questions;
    }
}

package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.QuestionAnswer;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/5/2.
 */
public class QuestionAnswerListParser implements Parser<ArrayList<QuestionAnswer>> {
    @Override
    public ArrayList<QuestionAnswer> parse(String response, ResponseObject<ArrayList<QuestionAnswer>> responseObject) throws Exception {
        ArrayList<QuestionAnswer> answers = new ArrayList<>();
        JSONArray comments = JsonHandler.getUniversalJsonArray(response, responseObject);
        assert comments != null;
        for (int i = 0; i < comments.length(); i++) {
            JSONObject jo = comments.getJSONObject(i);
            QuestionAnswer ans = QuestionAnswer.fromListJson(jo);
            answers.add(ans);
        }
        return answers;
    }
}

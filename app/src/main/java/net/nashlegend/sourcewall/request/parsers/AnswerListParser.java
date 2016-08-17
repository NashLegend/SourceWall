package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Answer;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/5/2.
 */
public class AnswerListParser implements Parser<ArrayList<Answer>> {
    @Override
    public ArrayList<Answer> parse(String response, ResponseObject<ArrayList<Answer>> responseObject) throws Exception {
        ArrayList<Answer> answers = new ArrayList<>();
        JSONArray comments = JsonHandler.getUniversalJsonArray(response, responseObject);
        assert comments != null;
        for (int i = 0; i < comments.length(); i++) {
            JSONObject jo = comments.getJSONObject(i);
            Answer ans = Answer.fromListJson(jo);
            answers.add(ans);
        }
        return answers;
    }
}

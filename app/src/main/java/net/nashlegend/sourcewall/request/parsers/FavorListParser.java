package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Favor;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/5/2.
 */
public class FavorListParser implements Parser<ArrayList<Favor>> {
    @Override
    public ArrayList<Favor> parse(String jString, ResponseObject<ArrayList<Favor>> responseObject) throws Exception {
        ArrayList<Favor> articleList = new ArrayList<>();
        JSONArray favors = JsonHandler.getUniversalJsonArray(jString, responseObject);
        assert favors != null;
        for (int i = 0; i < favors.length(); i++) {
            JSONObject jo = favors.getJSONObject(i);
            articleList.add(Favor.fromJson(jo));
        }
        return articleList;
    }
}

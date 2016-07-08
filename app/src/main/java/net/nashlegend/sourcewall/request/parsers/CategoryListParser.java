package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Category;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/7/8.
 */

public class CategoryListParser implements Parser<ArrayList<Category>> {
    @Override
    public ArrayList<Category> parse(String str, ResponseObject<ArrayList<Category>> responseObject) throws Exception {
        JSONArray jsonArray = JsonHandler.getUniversalJsonArray(str, responseObject);
        ArrayList<Category> categories = new ArrayList<>();
        assert jsonArray != null;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject subObject = jsonArray.getJSONObject(i);
            categories.add(Category.fromJson(subObject));
        }
        return categories;
    }
}

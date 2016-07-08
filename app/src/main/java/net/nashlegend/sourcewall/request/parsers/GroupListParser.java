package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/7/8.
 */

public class GroupListParser implements Parser<ArrayList<SubItem>> {
    @Override
    public ArrayList<SubItem> parse(String response, ResponseObject<ArrayList<SubItem>> responseObject) throws Exception {
        ArrayList<SubItem> list = new ArrayList<>();
        JSONArray subItems = JsonHandler.getUniversalJsonArray(response, responseObject);
        assert subItems != null;
        for (int i = 0; i < subItems.length(); i++) {
            JSONObject jo = subItems.getJSONObject(i).optJSONObject("group");
            SubItem subItem = new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, jo.optString("name"), jo.optString("id"));
            list.add(subItem);
        }
        return list;
    }
}

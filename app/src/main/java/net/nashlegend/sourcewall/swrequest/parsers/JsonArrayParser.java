package net.nashlegend.sourcewall.swrequest.parsers;

import net.nashlegend.sourcewall.swrequest.JsonHandler;
import net.nashlegend.sourcewall.swrequest.ResponseObject;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by NashLegend on 2015/9/23 0023.
 */
public class JsonArrayParser implements Parser<JSONArray> {
    @Override
    public JSONArray parse(String str, ResponseObject<JSONArray> responseObject) throws
            JSONException {
        return JsonHandler.getUniversalJsonArray(str, responseObject);
    }
}

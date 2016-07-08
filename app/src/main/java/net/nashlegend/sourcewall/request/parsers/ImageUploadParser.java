package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONObject;

/**
 * Created by NashLegend on 16/7/8.
 */

public class ImageUploadParser implements Parser<String> {
    @Override
    public String parse(String response, ResponseObject<String> responseObject) throws Exception {
        JSONObject object = JsonHandler.getUniversalJsonObject(response, responseObject);
        if (object != null) {
            return object.getString("url");
        } else {
            responseObject.ok = false;
            return "";
        }
    }
}

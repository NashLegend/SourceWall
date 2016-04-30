package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONObject;

/**
 * Created by NashLegend on 2015/10/13 0013.
 * 返回一个content json 的String值
 */
public class ContentValueForKeyParser implements Parser<String> {

    private String key = "ContentValueForKeyParser";

    public ContentValueForKeyParser(String key) {
        this.key = key;
    }

    @Override
    public String parse(String str, ResponseObject<String> responseObject) throws Exception {
        JSONObject jsonObject = JsonHandler.getUniversalJsonObject(str, responseObject);
        if (jsonObject != null) {
            return jsonObject.getString(key);
        } else {
            responseObject.ok = false;
            return null;
        }
    }
}

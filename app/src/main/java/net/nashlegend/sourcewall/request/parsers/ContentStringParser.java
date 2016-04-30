package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

/**
 * Created by NashLegend on 2015/10/13 0013.
 * 返回一个json 的string content
 */
public class ContentStringParser implements Parser<String> {
    @Override
    public String parse(String str, ResponseObject<String> responseObject) throws Exception {
        return JsonHandler.getUniversalJsonSimpleString(str, responseObject);
    }
}

package net.nashlegend.request.parsers;

import net.nashlegend.request.JsonHandler;
import net.nashlegend.request.ResponseObject;

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

package net.nashlegend.sourcewall.swrequest.parsers;

import net.nashlegend.sourcewall.swrequest.JsonHandler;
import net.nashlegend.sourcewall.swrequest.ResponseObject;

/**
 * Created by NashLegend on 2015/10/26 0026.
 */
public class ContentBooleanParser implements Parser<Boolean> {
    @Override
    public Boolean parse(String str, ResponseObject<Boolean> responseObject) throws Exception {
        String db = JsonHandler.getUniversalJsonSimpleString(str, responseObject);
        return Boolean.valueOf(db);
    }
}

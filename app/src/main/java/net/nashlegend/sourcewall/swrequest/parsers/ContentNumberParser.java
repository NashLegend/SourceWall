package net.nashlegend.sourcewall.swrequest.parsers;

import net.nashlegend.sourcewall.swrequest.JsonHandler;
import net.nashlegend.sourcewall.swrequest.ResponseObject;

/**
 * Created by NashLegend on 2015/10/13 0013.
 * 返回一个json 的数字
 */
public class ContentNumberParser implements Parser<Double> {

    @Override
    public Double parse(String str, ResponseObject<Double> responseObject) throws Exception {
        String db = JsonHandler.getUniversalJsonSimpleString(str, responseObject);
        return Double.valueOf(db);
    }
}

package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.request.ResponseObject;

/**
 * Created by NashLegend on 2015/9/23 0023.
 * 啥也不干，原样返回http的response
 */
public class StringParser implements Parser<String> {

    @Override
    public String parse(String str, ResponseObject<String> responseObject) throws Exception {
        responseObject.ok = true;
        return str;
    }
}

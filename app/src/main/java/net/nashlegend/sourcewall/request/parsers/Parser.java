package net.nashlegend.sourcewall.request.parsers;


import net.nashlegend.sourcewall.request.ResponseObject;

/**
 * Created by NashLegend on 2015/9/23 0023.
 */
public interface Parser<T> {
    T parse(String response, ResponseObject<T> responseObject) throws Exception;
}

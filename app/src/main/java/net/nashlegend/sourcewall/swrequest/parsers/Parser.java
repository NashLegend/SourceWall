package net.nashlegend.sourcewall.swrequest.parsers;


import net.nashlegend.sourcewall.swrequest.ResponseObject;

/**
 * Created by NashLegend on 2015/9/23 0023.
 */
public interface Parser<T> {
    T parse(String str, ResponseObject<T> responseObject) throws Exception;
}

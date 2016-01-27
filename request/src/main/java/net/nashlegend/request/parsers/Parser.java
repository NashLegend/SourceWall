package net.nashlegend.request.parsers;


import net.nashlegend.request.ResponseObject;

/**
 * Created by NashLegend on 2015/9/23 0023.
 */
public interface Parser<T> {
     T parse(String str, ResponseObject<T> responseObject) throws Exception;
}

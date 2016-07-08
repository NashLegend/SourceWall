package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

/**
 * Created by NashLegend on 16/7/8.
 */

public class BasketParser implements Parser<Basket> {
    @Override
    public Basket parse(String response, ResponseObject<Basket> responseObject) throws Exception {
        return Basket.fromJson(JsonHandler.getUniversalJsonObject(response, responseObject));
    }
}

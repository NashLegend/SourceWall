package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/7/8.
 */

public class BasketListParser implements Parser<ArrayList<Basket>> {
    @Override
    public ArrayList<Basket> parse(String response, ResponseObject<ArrayList<Basket>> responseObject) throws Exception {
        JSONArray jsonArray = JsonHandler.getUniversalJsonArray(response, responseObject);
        if (jsonArray == null) throw new NullPointerException("basket list is null");
        ArrayList<Basket> baskets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Basket basket = Basket.fromJson(jsonArray.getJSONObject(i));
            baskets.add(basket);
        }
        return baskets;
    }
}

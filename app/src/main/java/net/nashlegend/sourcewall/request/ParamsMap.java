package net.nashlegend.sourcewall.request;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/7/26.
 */

public class ParamsMap {
    public final ArrayList<Param> params = new ArrayList<>();

    public ParamsMap put(String key, Object value) {
        params.add(new Param(key, value));
        return this;
    }
}

package net.nashlegend.sourcewall.model;

import org.json.JSONObject;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class Category {
    String id = "";
    String name = "";

    public static Category fromJson(JSONObject jsonObject) throws Exception {
        Category category = new Category();
        category.setId(jsonObject.optString("id"));
        category.setName(jsonObject.optString("name"));
        return category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

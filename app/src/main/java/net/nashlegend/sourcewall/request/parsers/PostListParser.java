package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/5/2.
 */
public class PostListParser implements Parser<ArrayList<Post>> {
    @Override
    public ArrayList<Post> parse(String jString, ResponseObject<ArrayList<Post>> responseObject)
            throws Exception {
        ArrayList<Post> list = new ArrayList<>();
        JSONArray articles = JsonHandler.getUniversalJsonArray(jString, responseObject);
        assert articles != null;
        for (int i = 0; i < articles.length(); i++) {
            JSONObject jo = articles.getJSONObject(i);
            Post post = Post.fromJson(jo);
            // 无法获取titleImageUrl，也用不着，太TMD费流量了
            list.add(post);
        }
        return list;
    }
}

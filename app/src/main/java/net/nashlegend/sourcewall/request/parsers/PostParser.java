package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONObject;

/**
 * Created by NashLegend on 16/5/2.
 */
public class PostParser implements Parser<Post> {
    @Override
    public Post parse(String jString, ResponseObject<Post> responseObject) throws Exception {
        JSONObject postResult = JsonHandler.getUniversalJsonObject(jString, responseObject);
        return Post.fromJson(postResult);
    }
}

package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.Message;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/5/2.
 */
public class MessageListParser implements Parser<ArrayList<Message>> {
    @Override
    public ArrayList<Message> parse(String str, ResponseObject<ArrayList<Message>> responseObject) throws Exception {
        JSONArray notices = JsonHandler.getUniversalJsonArray(str, responseObject);
        ArrayList<Message> noticeList = new ArrayList<>();
        assert notices != null;
        for (int i = 0; i < notices.length(); i++) {
            JSONObject noticesObject = notices.getJSONObject(i);
            Message message = Message.fromJson(noticesObject);
            noticeList.add(message);
        }
        return noticeList;
    }
}

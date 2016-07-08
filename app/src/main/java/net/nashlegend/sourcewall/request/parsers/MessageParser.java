package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Message;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONObject;

/**
 * Created by NashLegend on 16/7/8.
 */

public class MessageParser implements Parser<Message> {
    @Override
    public Message parse(String str, ResponseObject<Message> responseObject) throws Exception {
        JSONObject noticesObject = JsonHandler.getUniversalJsonObject(str, responseObject);
        return Message.fromJson(noticesObject);
    }
}

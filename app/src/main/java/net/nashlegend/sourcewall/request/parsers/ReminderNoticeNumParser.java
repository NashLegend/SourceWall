package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONObject;

/**
 * Created by NashLegend on 16/5/2.
 */
public class ReminderNoticeNumParser implements Parser<ReminderNoticeNum> {
    @Override
    public ReminderNoticeNum parse(String jString, ResponseObject<ReminderNoticeNum> responseObject) throws Exception {
        return ReminderNoticeNum.fromJson(JsonHandler.getUniversalJsonObject(jString, responseObject));
    }
}

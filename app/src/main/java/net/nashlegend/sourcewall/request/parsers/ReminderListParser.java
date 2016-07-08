package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Reminder;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/5/2.
 */
public class ReminderListParser implements Parser<ArrayList<Reminder>> {
    @Override
    public ArrayList<Reminder> parse(String str, ResponseObject<ArrayList<Reminder>> responseObject) throws Exception {
        JSONArray reminders = JsonHandler.getUniversalJsonArray(str, responseObject);
        if (reminders == null) throw new NullPointerException("reminders is null");
        ArrayList<Reminder> noticeList = new ArrayList<>();
        for (int i = 0; i < reminders.length(); i++) {
            noticeList.add(Reminder.fromJson(reminders.getJSONObject(i)));
        }
        return noticeList;
    }
}

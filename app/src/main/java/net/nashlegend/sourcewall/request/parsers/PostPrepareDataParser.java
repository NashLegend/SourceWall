package net.nashlegend.sourcewall.request.parsers;

import android.text.TextUtils;

import net.nashlegend.sourcewall.model.PrepareData;
import net.nashlegend.sourcewall.request.Param;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/7/8.
 */

public class PostPrepareDataParser implements Parser<PrepareData> {
    @Override
    public PrepareData parse(String str, ResponseObject<PrepareData> responseObject) throws Exception {
        Document doc = Jsoup.parse(str);
        Element selects = doc.getElementById("topic");
        ArrayList<Param> pairs = new ArrayList<>();
        String csrf = doc.getElementById("csrf_token").attr("value");
        if (selects != null) {
            Elements elements = selects.getElementsByTag("option");
            if (elements != null && elements.size() > 0) {
                for (int i = 0; i < elements.size(); i++) {
                    Element topic = elements.get(i);
                    String name = topic.text();
                    String value = topic.attr("value");
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                        pairs.add(new Param(name, value));
                    }
                }
            }
        }
        PrepareData prepareData = new PrepareData();
        responseObject.ok = !TextUtils.isEmpty(csrf);
        if (!TextUtils.isEmpty(csrf)) {
            prepareData.setCsrf(csrf);
            prepareData.setPairs(pairs);
        }
        return prepareData;
    }
}

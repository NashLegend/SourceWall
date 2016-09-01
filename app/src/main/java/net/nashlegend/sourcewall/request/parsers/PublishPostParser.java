package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.request.ResponseObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NashLegend on 16/7/8.
 */

public class PublishPostParser implements Parser<String> {
    @Override
    public String parse(String response, ResponseObject<String> responseObject) throws Exception {
        try {
            Document document = Jsoup.parse(response);
            String url = document.getElementsByTag("a").get(0).text();
            Matcher matcher = Pattern.compile("/post/(\\d+)/").matcher(url);
            responseObject.ok = matcher.find();
            if (responseObject.ok) {
                return matcher.group(1);
            } else {
                return "";
            }
        } catch (Exception e) {
            if (response.contains("Redirecting")) {
                responseObject.ok = true;
                return "";
            } else {
                throw e;
            }
        }
    }
}

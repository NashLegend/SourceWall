package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.request.ResponseObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NashLegend on 16/7/8.
 */

public class PublishPostParser implements Parser<String> {
    @Override
    public String parse(String response, ResponseObject<String> responseObject) throws Exception {
        Document document = Jsoup.parse(response);
        Elements elements = document.getElementsByTag("a");
        Matcher matcher = Pattern.compile("^/post/(\\d+)/$").matcher(elements.get(0).text());
        return matcher.group(1);
    }
}

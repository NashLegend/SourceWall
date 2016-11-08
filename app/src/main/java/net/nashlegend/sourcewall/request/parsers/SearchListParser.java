package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.SearchItem;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by NashLegend on 16/5/2.
 */
public class SearchListParser implements Parser<ArrayList<SearchItem>> {
    @Override
    public ArrayList<SearchItem> parse(String jString,
            ResponseObject<ArrayList<SearchItem>> responseObject) throws Exception {
        Document document = Jsoup.parse(jString);
        Elements elements = document.getElementsByClass("title-detail");
        ArrayList<SearchItem> searchItemArrayList = new ArrayList<>();
        for (Element element : elements) {
            searchItemArrayList.add(SearchItem.fromHtml(element));
        }
        responseObject.ok = true;
        return searchItemArrayList;
    }
}

package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NashLegend on 16/5/2.
 */
public class PostHtmlListParser implements Parser<ArrayList<Post>> {
    @Override
    public ArrayList<Post> parse(String html, ResponseObject<ArrayList<Post>> responseObject)
            throws Exception {
        ArrayList<Post> list = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByClass(" post-index-list");
        if (elements.size() == 1) {
            Elements postlist = elements.get(0).getElementsByTag("li");
            for (Element aPostlist : postlist) {
                Post item = new Post();
                Element link = aPostlist.getElementsByClass("post").get(0);
                String postTitle = link.text();
                String postUrl = link.attr("href");
                String postImageUrl = "";
                int postLike = Integer.valueOf(aPostlist.getElementsByClass(
                        "like-num").text().replaceAll("\\D*", ""));
                int postComm = Integer.valueOf(aPostlist.getElementsByClass(
                        "post-reply-num").text().replaceAll("\\D*", ""));
                String content = aPostlist.getElementsByClass("post-info-content").text();
                String reg = "(\\S+)\\s+发表于\\s+(\\S+)";
                Matcher matcher = Pattern.compile(reg).matcher(content);
                if (matcher.find()) {
                    item.setGroupName(matcher.group(2));
                    item.getAuthor().setName(matcher.group(1));
                }
                item.setTitle(postTitle);
                item.setUrl(postUrl);
                item.setId(postUrl.replaceAll("\\?.*$", "").replaceAll("\\D+", ""));
                item.setTitleImageUrl(postImageUrl);
                item.setLikeNum(postLike);
                item.setReplyNum(postComm);
                item.setFeatured(false);
                list.add(item);
            }
            responseObject.ok = true;
        } else {
            responseObject.ok = false;
        }
        return list;
    }
}

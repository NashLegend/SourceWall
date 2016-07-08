package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.request.ResponseObject;

/**
 * Created by NashLegend on 16/7/8.
 */

public class ArticleHtmlParser implements Parser<Article> {
    String id = "";

    public ArticleHtmlParser(String id) {
        this.id = id;
    }

    @Override
    public Article parse(String response, ResponseObject<Article> responseObject) throws Exception {
        Article article = Article.fromHtmlDetail(id, response);
        responseObject.ok = true;
        return article;
    }
}

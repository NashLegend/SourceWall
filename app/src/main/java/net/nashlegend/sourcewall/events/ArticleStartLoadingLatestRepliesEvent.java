package net.nashlegend.sourcewall.events;

import net.nashlegend.sourcewall.model.Article;

/**
 * Created by NashLegend on 16/9/6.
 */

public class ArticleStartLoadingLatestRepliesEvent {
    public Article article;

    public ArticleStartLoadingLatestRepliesEvent(Article article) {
        this.article = article;
    }
}

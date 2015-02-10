package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.commonview.WWebView;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedUtil;
import net.nashlegend.sourcewall.util.StyleChecker;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class ArticleView extends AceView<Article> {

    private TextView titleView;
    private WWebView contentView;
    private TextView authorView;
    private TextView dateView;

    public Article getArticle() {
        return article;
    }

    private Article article;

    public ArticleView(Context context) {
        super(context);
        initViews();
    }

    public ArticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ArticleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    private void initViews() {
        if (SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background_night));
        } else {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background));
        }

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_article_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        contentView = (WWebView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);

        Resources.Theme theme = getContext().getTheme();
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{R.attr.cardBackgroundColor});
        int colorBack = typedArray.getColor(0, 0);
        typedArray.recycle();
        contentView.setBackgroundColor(colorBack);
    }

    @Override
    public void setData(Article model) {
        if (article == null) {
            article = model;
            titleView.setText(article.getTitle());
            authorView.setText(article.getAuthor());
            dateView.setText(article.getDate());
            String html = StyleChecker.getArticleHtml(article.getContent());
            contentView.loadDataWithBaseURL(Consts.Base_Url, html, "text/html", "charset=UTF-8", null);
        }
    }

    @Override
    public Article getData() {
        return article;
    }
}

package com.example.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.commonview.WWebView;
import com.example.sourcewall.model.Post;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.SharedUtil;
import com.example.sourcewall.util.StyleChecker;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class PostView extends AceView<Post> {
    private Post post;
    private TextView titleView;
    private WWebView contentView;
    private TextView authorView;
    private TextView dateView;

    public PostView(Context context) {
        super(context);
        if (SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background_night));
        } else {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background));
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_post_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        contentView = (WWebView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
    }

    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Post model) {
        if (post == null) {
            post = model;
            titleView.setText(post.getTitle());
            authorView.setText(post.getAuthor());
            dateView.setText(post.getDate());
            String html = StyleChecker.getPostHtml(post.getContent());
            contentView.setBackgroundColor(0);
            contentView.loadDataWithBaseURL(Consts.Base_Url, html, "text/html", "charset=UTF-8", null);
        }
    }

    @Override
    public Post getData() {
        return post;
    }
}

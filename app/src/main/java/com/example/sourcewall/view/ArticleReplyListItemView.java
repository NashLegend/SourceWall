package com.example.sourcewall.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.model.UComment;
import com.example.sourcewall.util.DisplayUtil;
import com.squareup.picasso.Picasso;

/**
 * Created by NashLegend on 2014/9/18 0018.
 * 没有被使用过
 */
public class ArticleReplyListItemView extends AceView<UComment> {

    private WebView contentView;
    private TextView authorView;
    private TextView dateView;
    private TextView likesView;
    private TextView floorView;
    private ImageView avatarImage;
    private UComment comment;

    public ArticleReplyListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_article_reply_item_view, this);
        contentView = (WebView) findViewById(R.id.text_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        likesView = (TextView) findViewById(R.id.text_like_num);
        floorView = (TextView) findViewById(R.id.text_floor);
        avatarImage = (ImageView) findViewById(R.id.image_avatar);
    }

    public ArticleReplyListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArticleReplyListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(UComment model) {
        comment = model;
        authorView.setText(comment.getAuthor());
        dateView.setText(comment.getDate());
        likesView.setText(comment.getLikeNum() + "");
        floorView.setText(comment.getFloor());
        String html = "";
        contentView.getSettings().setDefaultTextEncodingName("UTF-8");
        contentView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "charset=UTF-8", null);
        if (TextUtils.isEmpty(comment.getAuthorAvatarUrl())) {
            avatarImage.setImageResource(R.drawable.ic_launcher);
        } else {
            Picasso.with(getContext()).load(comment.getAuthorAvatarUrl())
                    .resize(DisplayUtil.getScreenWidth(getContext()), -1)
                    .into(avatarImage);
        }
    }

    @Override
    public UComment getData() {
        return comment;
    }
}

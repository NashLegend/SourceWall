package com.example.outerspace.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.outerspace.R;
import com.example.outerspace.model.AceModel;
import com.example.outerspace.model.Post;
import com.example.outerspace.util.DisplayUtil;
import com.example.outerspace.util.ImageFetcher.ImageFetcher;
import com.squareup.picasso.Picasso;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class PostListItemView extends AceView<Post> {

    public Post getPost() {
        return mPost;
    }

    private Post mPost;
    private TextView titleView;
    private TextView authorView;
    private TextView replyView;
    private TextView likesView;
    ImageFetcher titleImageFetcher;

    public PostListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_post_item_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        authorView = (TextView) findViewById(R.id.text_author);
        replyView = (TextView) findViewById(R.id.text_replies_num);
        likesView = (TextView) findViewById(R.id.text_like_num);
        titleImageFetcher = new ImageFetcher(getContext(), DisplayUtil.getScreenWidth(getContext()), 0);
    }

    public PostListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Post model) {
        mPost = model;
        titleView.setText(mPost.getTitle());
        authorView.setText(mPost.getAuthor());
        replyView.setText(mPost.getReplyNum() + "");
        likesView.setText(mPost.getLikeNum() + "");
    }
}

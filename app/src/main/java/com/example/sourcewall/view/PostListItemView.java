package com.example.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.model.Post;

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
    private TextView groupView;

    public PostListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_post_item_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        authorView = (TextView) findViewById(R.id.text_author);
        replyView = (TextView) findViewById(R.id.text_replies_num);
        likesView = (TextView) findViewById(R.id.text_like_num);
        groupView = (TextView) findViewById(R.id.text_group);
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
        groupView.setText(mPost.getGroupName());
    }

    @Override
    public Post getData() {
        return mPost;
    }
}

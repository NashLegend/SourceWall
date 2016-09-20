package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.util.DateTimeUtil;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class PostListItemView extends AceView<Post> {

    private Post mPost;
    private TextView titleView;
    private TextView authorView;
    private TextView dateView;
    private TextView replyView;
    private TextView likesView;
    private TextView groupView;
    private TextView sticky;

    public PostListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_post_item_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        replyView = (TextView) findViewById(R.id.text_replies_num);
        likesView = (TextView) findViewById(R.id.text_like_num);
        groupView = (TextView) findViewById(R.id.text_group);
        sticky = (TextView) findViewById(R.id.tag_sticky);
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
        authorView.setText(mPost.getAuthor().getName());
        dateView.setText(DateTimeUtil.time2HumanReadable(mPost.getDate()));
        replyView.setText(String.valueOf(mPost.getReplyNum()));
        likesView.setText(String.valueOf(mPost.getLikeNum()));
        if (TextUtils.isEmpty(mPost.getAuthor().getName())) {
            authorView.setVisibility(GONE);
        } else {
            authorView.setVisibility(VISIBLE);
        }

        if (TextUtils.isEmpty(mPost.getDate())) {
            dateView.setVisibility(GONE);
        } else {
            dateView.setVisibility(VISIBLE);
        }

        if (mPost.isFeatured()) {
            groupView.setText("");
            groupView.setVisibility(GONE);
            dateView.setVisibility(VISIBLE);
        } else {
            groupView.setText(mPost.getGroupName());
            groupView.setVisibility(VISIBLE);
            dateView.setVisibility(GONE);
        }

        if (mPost.isStick()) {
            sticky.setVisibility(VISIBLE);
        } else {
            sticky.setVisibility(GONE);
        }

    }

    @Override
    public Post getData() {
        return mPost;
    }
}

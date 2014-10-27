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
public class PostListItemView extends AceView {

    public Post getPost() {
        return mPost;
    }

    private Post mPost;
    private TextView titleView;
    private TextView authorView;
    private TextView dateView;
    private TextView replyView;
    private TextView likesView;
    private ImageView titleImage;
    ImageFetcher titleImageFetcher;

    public PostListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_post_item_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        replyView = (TextView) findViewById(R.id.text_replies_num);
        likesView = (TextView) findViewById(R.id.text_like_num);
        titleImage = (ImageView) findViewById(R.id.image_title);
        titleImageFetcher = new ImageFetcher(getContext(), DisplayUtil.getScreenWidth(getContext()), 0);
    }

    public PostListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(AceModel model) {
        mPost = (Post) model;
        titleView.setText(mPost.getTitle());
        authorView.setText(mPost.getAuthor());
        dateView.setText(mPost.getDate());
        replyView.setText(mPost.getReplyNum() + "");
        likesView.setText(mPost.getLikeNum() + "");
        if (TextUtils.isEmpty(mPost.getTitleImageUrl())) {
            titleImage.setImageResource(R.drawable.ic_launcher);
        } else {
            Picasso.with(getContext()).load(mPost.getTitleImageUrl())
                    .resizeDimen(R.dimen.list_post_item_title_image_dimen, R.dimen.list_post_item_title_image_dimen)
                    .into(titleImage);
        }
    }
}

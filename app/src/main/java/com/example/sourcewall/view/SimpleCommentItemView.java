package com.example.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.model.UComment;
import com.squareup.picasso.Picasso;

/**
 * Created by NashLegend on 2014/9/18 0018.
 * Shared by QuestionComment and QuestionAnswerComment
 */
public class SimpleCommentItemView extends AceView<UComment> {
    private TextView contentView;
    private TextView authorView;
    private TextView dateView;
    private ImageView avatarImage;
    private UComment comment;

    public SimpleCommentItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_simple_comment_item_view, this);
        contentView = (TextView) findViewById(R.id.text_comment);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        avatarImage = (ImageView) findViewById(R.id.image_avatar);
    }

    public SimpleCommentItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleCommentItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(UComment model) {
        comment = model;
        authorView.setText(comment.getAuthor());
        dateView.setText(comment.getDate());
        contentView.setText(comment.getContent());
        Picasso.with(getContext()).load(comment.getAuthorAvatarUrl())
                .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen)
                .into(avatarImage);
    }

    @Override
    public UComment getData() {
        return comment;
    }
}

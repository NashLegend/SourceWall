package com.example.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.commonview.TTextView;
import com.example.sourcewall.model.UComment;
import com.example.sourcewall.util.Config;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.SharedUtil;
import com.example.sourcewall.util.TextHtmlHelper;
import com.squareup.picasso.Picasso;

/**
 * Created by NashLegend on 2014/9/18 0018.
 * Shared by PostComment and ArticleComment
 */
public class MediumListItemView extends AceView<UComment> {

    private TTextView contentView;
    private TextView authorView;
    private TextView dateView;
    private TextView likesView;
    private TextView floorView;
    private ImageView avatarImage;
    private UComment comment;
    private TextHtmlHelper htmlHelper;

    public MediumListItemView(Context context) {
        super(context);
        if (SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background_night));
        } else {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background));
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_medium_comment_item_view, this);
        htmlHelper = new TextHtmlHelper(context);
        contentView = (TTextView) findViewById(R.id.text_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        likesView = (TextView) findViewById(R.id.text_like_num);
        floorView = (TextView) findViewById(R.id.text_floor);
        avatarImage = (ImageView) findViewById(R.id.image_avatar);
    }

    public MediumListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediumListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(UComment model) {
        comment = model;
        authorView.setText(comment.getAuthor());
        dateView.setText(comment.getDate());
        likesView.setText(comment.getLikeNum() + "");
        floorView.setText(comment.getFloor());
        htmlHelper.load(contentView, comment.getContent());
        if (Config.shouldLoadImage()) {
            Picasso.with(getContext()).load(comment.getAuthorAvatarUrl())
                    .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen)
                    .into(avatarImage);
        } else {
            avatarImage.setImageResource(R.drawable.default_avatar);
        }
    }

    @Override
    public UComment getData() {
        return comment;
    }
}

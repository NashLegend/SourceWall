package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.data.Config;
import net.nashlegend.sourcewall.util.DateTimeUtil;
import net.nashlegend.sourcewall.util.ImageUtils;

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
        authorView.setText(comment.getAuthor().getName());
        dateView.setText(DateTimeUtil.time2HumanReadable(comment.getDate()));
        contentView.setText(comment.getContent());
        if (Config.shouldLoadImage()) {
            ImageLoader.getInstance().displayImage(comment.getAuthor().getAvatar(), avatarImage, ImageUtils.avatarOptions);
        } else {
            avatarImage.setImageResource(R.drawable.default_avatar);
        }
    }

    @Override
    public UComment getData() {
        return comment;
    }
}

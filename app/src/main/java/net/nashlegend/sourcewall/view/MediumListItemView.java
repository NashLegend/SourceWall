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
import net.nashlegend.sourcewall.view.common.TTextView;

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

    public MediumListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_medium_comment_item_view, this);
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

    public void plusOneLike() {
        likesView.setText(String.valueOf(comment.getLikeNum()));
    }

    @Override
    public void setData(UComment model) {
        if (comment != null && comment.getID() != null && comment.getID().equals(model.getID())) {
            return;
        }
        comment = model;
        authorView.setText((comment.isHostAuthor() ? "(楼主)" : "") + comment.getAuthor().getName());
        dateView.setText(DateTimeUtil.time2HumanReadable(comment.getDate()));
        likesView.setText(String.valueOf(comment.getLikeNum() + ""));
        floorView.setText(comment.getFloor());
        contentView.loadHtml(comment.getContent());
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

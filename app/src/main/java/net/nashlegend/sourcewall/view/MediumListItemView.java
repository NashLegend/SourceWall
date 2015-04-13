package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.commonview.TTextView;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.RoundTransformation;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;

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
    private ImageButton imageButton;
    private UComment comment;
    private View authorLayout;

    public MediumListItemView(Context context) {
        super(context);
        if (SharedPreferencesUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background_night));
        } else {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background));
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_medium_comment_item_view, this);
        contentView = (TTextView) findViewById(R.id.text_content);
        authorLayout = findViewById(R.id.layout_author);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        likesView = (TextView) findViewById(R.id.text_like_num);
        floorView = (TextView) findViewById(R.id.text_floor);
        avatarImage = (ImageView) findViewById(R.id.image_avatar);
        imageButton = (ImageButton) findViewById(R.id.button_overflow);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popMenu();
            }
        });
    }

    public MediumListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediumListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void plusOneLike() {
        likesView.setText(comment.getLikeNum() + "");
    }

    @Override
    public void setData(UComment model) {
        if (comment != null && comment.getID() != null && comment.getID().equals(model.getID())) {
            return;
        }
        comment = model;
        authorView.setText(comment.getAuthor());
        dateView.setText(comment.getDate());
        likesView.setText(comment.getLikeNum() + "");
        floorView.setText(comment.getFloor());
        contentView.loadHtml(comment.getContent());
        if (Config.shouldLoadImage()) {
            Picasso.with(getContext()).load(comment.getAuthorAvatarUrl())
                    .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen).placeholder(R.drawable.default_avatar)
                    .transform(new RoundTransformation(Color.parseColor("#00000000"), 0, true)).into(avatarImage);
        } else {
            avatarImage.setImageResource(R.drawable.default_avatar);
        }
    }

    @Override
    public UComment getData() {
        return comment;
    }

    public void popMenu() {

    }
}

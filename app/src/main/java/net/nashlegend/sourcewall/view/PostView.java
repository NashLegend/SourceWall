package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.commonview.WWebView;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedUtil;
import net.nashlegend.sourcewall.util.StyleChecker;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class PostView extends AceView<Post> {
    private Post post;
    private TextView titleView;
    private WWebView contentView;
    private TextView authorView;
    private TextView dateView;
    private ImageView avatarImage;

    public PostView(Context context) {
        super(context);
        if (SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background_night));
        } else {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background));
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_post_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        contentView = (WWebView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        avatarImage = (ImageView) findViewById(R.id.image_avatar);

        Resources.Theme theme = getContext().getTheme();
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{R.attr.cardBackgroundColor});
        int colorBack = typedArray.getColor(0, 0);
        typedArray.recycle();
        contentView.setBackgroundColor(colorBack);
    }

    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Post model) {
        if (post == null) {
            post = model;
            titleView.setText(post.getTitle());
            authorView.setText(post.getAuthor());
            dateView.setText(post.getDate());
            String html = StyleChecker.getPostHtml(post.getContent());
            contentView.loadDataWithBaseURL(Consts.Base_Url, html, "text/html", "charset=UTF-8", null);
            if (Config.shouldLoadImage()) {
                Picasso.with(getContext()).load(post.getAuthorAvatarUrl())
                        .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen).placeholder(R.drawable.default_avatar)
                        .into(avatarImage);
            } else {
                avatarImage.setImageResource(R.drawable.default_avatar);
            }
        }
    }

    @Override
    public Post getData() {
        return post;
    }
}

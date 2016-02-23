package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.model.QuestionAnswer;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.ImageUtils;
import net.nashlegend.sourcewall.util.RegUtil;
import net.nashlegend.sourcewall.util.RoundTransformation;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class AnswerListItemView extends AceView<QuestionAnswer> {
    private QuestionAnswer answer;
    private TextView contentView;
    private TextView authorView;
    private ImageView avatar;
    private TextView dateView;
    private TextView supportView;
    private TextView authorTitleView;

    public AnswerListItemView(Context context) {
        super(context);
        if (SharedPreferencesUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background_night));
        } else {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background));
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_answer_item_view, this);
        contentView = (TextView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        supportView = (TextView) findViewById(R.id.text_num_support);
        authorTitleView = (TextView) findViewById(R.id.text_author_title);
        dateView = (TextView) findViewById(R.id.text_date);
        avatar = (ImageView) findViewById(R.id.image_avatar);
    }

    public AnswerListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnswerListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(QuestionAnswer model) {
        answer = model;
        supportView.setText(String.valueOf(answer.getUpvoteNum()));
        authorView.setText(answer.getAuthor().getName());
        authorTitleView.setText(answer.getAuthor().getTitle());
        dateView.setText(answer.getDate_created());
        if (Config.shouldLoadImage()) {
            ImageLoader.getInstance().displayImage(answer.getAuthor().getAvatar(),avatar, ImageUtils.avatarOptions);
        } else {
            avatar.setImageResource(R.drawable.default_avatar);
        }
        String simplifiedStr = RegUtil.tryGetStringByLength(RegUtil.html2PlainTextWithImageTag(answer.getContent()), 100);
        contentView.setText(simplifiedStr);
    }

    @Override
    public QuestionAnswer getData() {
        return answer;
    }
}

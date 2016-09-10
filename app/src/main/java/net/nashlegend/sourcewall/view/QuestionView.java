package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.SimpleReplyActivity;
import net.nashlegend.sourcewall.data.Consts.Extras;
import net.nashlegend.sourcewall.data.Mob;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.util.DateTimeUtil;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.view.common.TTextView;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class QuestionView extends AceView<Question> {
    private Question question;
    private TextView titleView;
    private TextView authorView;
    private TextView dateView;
    private TTextView contentView;
    private TextView commentNumView;

    public QuestionView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_question_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        contentView = (TTextView) findViewById(R.id.web_content);
        commentNumView = (TextView) findViewById(R.id.text_replies_num);
        View layoutComments = findViewById(R.id.layout_comment);
        layoutComments.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UiUtil.shouldThrottle()) {
                    return;
                }
                MobclickAgent.onEvent(getContext(), Mob.Event_Open_Question_Comment);
                Intent intent = new Intent(getContext(), SimpleReplyActivity.class);
                intent.putExtra(Extras.Extra_Ace_Model, question);
                getContext().startActivity(intent);
            }
        });
    }

    public QuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuestionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Question model) {
        if (question == null) {
            question = model;
            commentNumView.setText(String.valueOf(question.getCommentNum()));
            titleView.setText(question.getTitle());
            authorView.setText(question.getAuthor().getName());
            dateView.setText(DateTimeUtil.time2HumanReadable(question.getDate()));
            contentView.loadHtml(question.getContent());
        }
    }

    @Override
    public Question getData() {
        return question;
    }
}

package com.example.sourcewall.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.SimpleReplyActivity;
import com.example.sourcewall.model.Question;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.StyleChecker;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class QuestionView extends AceView<Question> {
    private Question question;
    private TextView titleView;
    private TextView authorView;
    private TextView dateView;
    private WebView contentView;
    private View layoutComments;
    private TextView commentNumView;

    public QuestionView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_question_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        contentView = (WebView) findViewById(R.id.web_content);
        commentNumView = (TextView) findViewById(R.id.text_replies_num);
        layoutComments = findViewById(R.id.layout_comment);
        layoutComments.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SimpleReplyActivity.class);
                intent.putExtra(Consts.Extra_Ace_Model, question);
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
            commentNumView.setText(question.getCommentNum() + "");
            titleView.setText(question.getTitle());
            authorView.setText(question.getAuthor());
            dateView.setText(question.getDate());
            String html = StyleChecker.getQuestionHtml(question.getContent());
            contentView.setBackgroundColor(0);
            contentView.getSettings().setDefaultTextEncodingName("UTF-8");
            contentView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "charset=UTF-8", null);
        }
    }

    @Override
    public Question getData() {
        return question;
    }
}

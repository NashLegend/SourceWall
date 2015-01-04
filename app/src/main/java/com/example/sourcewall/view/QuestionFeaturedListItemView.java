package com.example.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.model.Question;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.SharedUtil;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class QuestionFeaturedListItemView extends AceView<Question> {
    private boolean featured = false;
    private Question question;
    private TextView titleView;
    private TextView summaryView;
    private TextView likeView;

    public QuestionFeaturedListItemView(Context context) {
        super(context);
        if (SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background_night));
        } else {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background));
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_question_featured_item_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        summaryView = (TextView) findViewById(R.id.text_summary);
        likeView = (TextView) findViewById(R.id.text_like_num);
    }

    public QuestionFeaturedListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuestionFeaturedListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Question model) {
        question = (Question) model;
        titleView.setText(question.getTitle().replaceAll("\\s", ""));
        String text = question.getSummary().replaceAll("\\s", "");
        summaryView.setText(text);
        if (text == "") {
            summaryView.setVisibility(GONE);
        } else {
            summaryView.setVisibility(VISIBLE);
        }
        likeView.setText(question.getRecommendNum() + "");
    }

    @Override
    public Question getData() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}

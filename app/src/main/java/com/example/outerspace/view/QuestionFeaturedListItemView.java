package com.example.outerspace.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.example.outerspace.R;
import com.example.outerspace.model.AceModel;
import com.example.outerspace.model.Question;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class QuestionFeaturedListItemView extends AceView {
    private boolean featured = false;
    private Question question;
    private TextView titleView;
    private TextView summaryView;
    private TextView likeView;

    public QuestionFeaturedListItemView(Context context) {
        super(context);
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
    public void setData(AceModel model) {
        if (model instanceof Question) {
            question = (Question) model;
            titleView.setText(question.getTitle());
            summaryView.setText(question.getSummary());
            likeView.setText(question.getRecommendNum() + "");
        }
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}

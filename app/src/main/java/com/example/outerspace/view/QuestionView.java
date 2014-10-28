package com.example.outerspace.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.example.outerspace.R;
import com.example.outerspace.model.AceModel;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class QuestionView extends AceView {
    public QuestionView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_question_view, this);
    }

    public QuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuestionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(AceModel model) {

    }
}

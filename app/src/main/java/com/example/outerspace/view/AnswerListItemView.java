package com.example.outerspace.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.example.outerspace.R;
import com.example.outerspace.model.AceModel;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class AnswerListItemView extends AceView {

    public AnswerListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_answer_item_view, this);
    }

    public AnswerListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnswerListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(AceModel model) {

    }
}

package com.example.outerspace.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.example.outerspace.model.AceModel;

/**
 * Created by NashLegend on 2014/9/18 0018.
 * Shared by QuestionComment and QuestionAnswerComment
 */
public class SimpleCommentItemView extends AceView {
    public SimpleCommentItemView(Context context) {
        super(context);
    }

    public SimpleCommentItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleCommentItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(AceModel model) {

    }
}

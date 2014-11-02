package com.example.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;

import com.example.sourcewall.model.SimpleComment;

/**
 * Created by NashLegend on 2014/9/18 0018.
 * Shared by QuestionComment and QuestionAnswerComment
 */
public class SimpleCommentItemView extends AceView<SimpleComment> {
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
    public void setData(SimpleComment model) {

    }
}

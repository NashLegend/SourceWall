package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedUtil;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class QuestionListItemView extends AceView<Question> {
    private Question question;
    private TextView titleView;
    private TextView summaryView;

    public QuestionListItemView(Context context) {
        super(context);
        if (SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background_night));
        } else {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background));
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_question_item_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        summaryView = (TextView) findViewById(R.id.text_summary);
    }

    public QuestionListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuestionListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Question model) {
        question = model;
        titleView.setText(question.getTitle().replaceAll("\\s", ""));
        String text = question.getSummary().replaceAll("\\s", "");
        if (text.equals("")) {
            summaryView.setText("暂无描述");
        } else {
            summaryView.setText(text);
        }
    }

    @Override
    public Question getData() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}

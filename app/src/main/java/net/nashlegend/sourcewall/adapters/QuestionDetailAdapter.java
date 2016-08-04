package net.nashlegend.sourcewall.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.view.AceView;
import net.nashlegend.sourcewall.view.AnswerListItemView;
import net.nashlegend.sourcewall.view.QuestionView;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class QuestionDetailAdapter extends AceAdapter<AceModel> {

    private static final int Type_Question = 0;
    private static final int Type_Answer = 1;

    public QuestionDetailAdapter(Context context) {
        super(context);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? Type_Question : Type_Answer;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int tp = getItemViewType(position);
        if (convertView == null) {
            if (tp == Type_Question) {
                convertView = new QuestionView(getContext());
            } else {
                convertView = new AnswerListItemView(getContext());
            }
        }
        ((AceView) convertView).setData(list.get(position));
        return convertView;
    }
}

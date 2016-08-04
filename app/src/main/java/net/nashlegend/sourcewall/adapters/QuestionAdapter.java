package net.nashlegend.sourcewall.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.view.QuestionListItemView;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class QuestionAdapter extends AceAdapter<Question> {
    public QuestionAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new QuestionListItemView(getContext());
        }
        ((QuestionListItemView) convertView).setData(list.get(position));
        return convertView;
    }
}

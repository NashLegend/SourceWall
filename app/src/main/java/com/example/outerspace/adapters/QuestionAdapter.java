package com.example.outerspace.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.outerspace.model.Question;
import com.example.outerspace.view.AceView;
import com.example.outerspace.view.QuestionFeaturedListItemView;

/**
 * Created by NashLegend on 2014/9/15 0015.
 */
public class QuestionAdapter extends AceAdapter<Question> {
    public QuestionAdapter(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new QuestionFeaturedListItemView(getContext());
        }
        ((AceView) convertView).setData(list.get(position));
        return convertView;
    }
}

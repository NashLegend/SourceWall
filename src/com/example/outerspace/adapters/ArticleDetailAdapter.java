package com.example.outerspace.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.outerspace.model.AceModel;
import com.example.outerspace.view.AceView;
import com.example.outerspace.view.ArticleView;
import com.example.outerspace.view.MediumListItemView;

/**
 * Created by NashLegend on 2014/9/18 0018.
 */
public class ArticleDetailAdapter extends AceAdapter<AceModel> {

    private static final int Type_Article = 0;
    private static final int Type_Comment = 1;

    public ArticleDetailAdapter(Context context) {
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
    public int getItemViewType(int position) {
        return position == 0 ? Type_Article : Type_Comment;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int tp = getItemViewType(position);
        if (convertView == null) {
            if (tp == Type_Article) {
                convertView = new ArticleView(getContext());
            } else {
                convertView = new MediumListItemView(getContext());
            }
        }
        ((AceView) convertView).setData(list.get(position));
        return convertView;
    }
}

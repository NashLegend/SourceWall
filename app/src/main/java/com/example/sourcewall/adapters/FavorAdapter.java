package com.example.sourcewall.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.sourcewall.model.Basket;
import com.example.sourcewall.view.FavorItemView;
import com.example.sourcewall.view.FavorView;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class FavorAdapter extends AceAdapter<Basket> {
    String link = "";
    String title = "";

    public FavorAdapter(Context context, String link, String title) {
        super(context);
        this.link = link;
        this.title = title;
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
            convertView = new FavorItemView(getContext());
        }
        ((FavorItemView) convertView).setData(list.get(position), link, title);
        return convertView;
    }
}

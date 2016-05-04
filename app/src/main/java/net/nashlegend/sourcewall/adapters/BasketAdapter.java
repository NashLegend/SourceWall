package net.nashlegend.sourcewall.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.view.BasketItemView;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class BasketAdapter extends AceAdapter<Basket> {
    String link = "";
    String title = "";

    public BasketAdapter(Context context, String link, String title) {
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
            convertView = new BasketItemView(getContext());
        }
        ((BasketItemView) convertView).setData(list.get(position), link, title);
        return convertView;
    }
}

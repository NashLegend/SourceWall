package net.nashlegend.sourcewall.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.model.Favor;
import net.nashlegend.sourcewall.view.FavorListItemView;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class FavorAdapter extends AceAdapter<Favor> {

    public FavorAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new FavorListItemView(getContext());
        }
        ((FavorListItemView) convertView).setData(list.get(position));
        return convertView;
    }
}

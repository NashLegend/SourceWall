package net.nashlegend.sourcewall.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.view.PostListItemView;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class PostAdapter extends AceAdapter<Post> {

    public PostAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new PostListItemView(getContext());
        }
        ((PostListItemView) convertView).setData(list.get(position));
        return convertView;
    }
}

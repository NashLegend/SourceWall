package net.nashlegend.sourcewall.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.view.ArticleListItemView;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class ArticleAdapter extends AceAdapter<Article> {

    public ArticleAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new ArticleListItemView(getContext());
        }
        ((ArticleListItemView) convertView).setData(list.get(position));
        return convertView;
    }
}

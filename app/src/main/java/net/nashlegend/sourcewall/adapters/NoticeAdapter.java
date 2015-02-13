package net.nashlegend.sourcewall.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.view.NoticeView;

/**
 * Created by NashLegend on 2015/2/12 0012
 */
public class NoticeAdapter extends AceAdapter<Notice> {

    public NoticeAdapter(Context context) {
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
            convertView = new NoticeView(getContext());
            ((NoticeView) convertView).setAdapter(this);
        }
        ((NoticeView) convertView).setData(list.get(position));
        return convertView;
    }

    public boolean removeItemByID(String id) {
        if (list.size() > 0) {
            if (id != null) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    if (id.equals(list.get(i).getId())) {
                        list.remove(i);
                        return true;
                    }
                }
            } else {
                for (int i = list.size() - 1; i >= 0; i--) {
                    if (list.get(i).getId() == null) {
                        list.remove(i);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

package net.nashlegend.sourcewall.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2015/3/31 0031
 */
public class ImageAdapter extends PagerAdapter {
    private Context mContext;
    private final ArrayList<String> list = new ArrayList<>();

    public ImageAdapter(Context context, ArrayList<String> images) {
        mContext = context;
        list.addAll(images);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    public void clear() {
        list.clear();
    }

    public void add(String imageInfo) {
        list.add(imageInfo);
    }

    public void add(int index, String imageInfo) {
        list.add(index, imageInfo);
    }

    public void addAll(ArrayList<String> list) {
        this.list.addAll(list);
    }

    public void addAll(int index, ArrayList<String> list) {
        this.list.addAll(index, list);
    }

    public void remove(String imageInfo) {
        list.remove(imageInfo);
    }

    public void remove(int index) {
        list.remove(index);
    }
}

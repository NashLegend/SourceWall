package net.nashlegend.sourcewall.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.util.ErrorUtils;
import net.nashlegend.sourcewall.view.ImageViewer;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2015/3/31 0031
 */
public class ImageAdapter extends PagerAdapter {
    private Context mContext;
    private final ArrayList<String> list = new ArrayList<>();

    public ImageAdapter(Context context) {
        mContext = context;
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
    public void destroyItem(ViewGroup container, int position, Object object) {
        try {
            if (object instanceof ImageViewer) {
                container.removeView((View) object);
            }
        } catch (Exception e) {
            ErrorUtils.onException(e);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageViewer imageViewer = new ImageViewer(mContext);
        imageViewer.load(list.get(position));
        container.addView(imageViewer);
        return imageViewer;
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

package com.example.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;

import com.example.sourcewall.model.Basket;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class FavorItemView extends AceView<Basket> {
    public FavorItemView(Context context) {
        super(context);
    }

    public FavorItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FavorItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Basket basket) {

    }
}

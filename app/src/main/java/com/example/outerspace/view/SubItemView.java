package com.example.outerspace.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.example.outerspace.R;
import com.example.outerspace.model.SubItem;

/**
 * Created by NashLegend on 2014/10/31 0031
 */
public class SubItemView extends AceView<SubItem> {
    private SubItem item;
    private TextView textView;

    public SubItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_subitem_view, this);
        textView = (TextView) findViewById(R.id.text_subitem);
    }

    public SubItem getSubItem() {
        return item;
    }

    @Override
    public void setData(SubItem subItem) {
        item = subItem;
        textView.setText(item.getName());
    }

}

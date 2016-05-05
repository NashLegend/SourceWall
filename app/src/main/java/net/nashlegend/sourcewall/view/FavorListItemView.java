package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.model.Favor;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class FavorListItemView extends AceView<Favor> {

    @BindView(R.id.text_title)
    TextView title;
    @BindView(R.id.text_date)
    TextView textDate;

    private Favor mFavor;

    public FavorListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_favor_item_view, this);
        ButterKnife.bind(this);
    }

    public FavorListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FavorListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Favor model) {
        mFavor = model;
        title.setText(mFavor.getTitle());
        textDate.setText(mFavor.getCreateTime());
    }

    @Override
    public Favor getData() {
        return mFavor;
    }
}

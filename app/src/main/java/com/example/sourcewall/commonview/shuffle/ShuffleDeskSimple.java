package com.example.sourcewall.CommonView.shuffle;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.example.sourcewall.R;
import com.example.sourcewall.util.DisplayUtil;

import java.util.ArrayList;

public class ShuffleDeskSimple extends RelativeLayout {
    private ArrayList<MovableButton> buttons = new ArrayList<>();
    private int buttonHeightDip = 48;
    private ShuffleCardSimple senator;
    private LinearLayout senatorLayout;

    public ShuffleDeskSimple(Context context, ScrollView scrollView) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_shuffle_simple, this);
        senatorLayout = (LinearLayout) findViewById(R.id.SenatorLayout);
        senator = (ShuffleCardSimple) findViewById(R.id.senator);
        senator.setDeskSimple(this, senatorLayout, scrollView);
    }

    public void InitDatas() {

        ShuffleDesk.vGap = dip2px(ShuffleDesk.vGapDip, getContext());
        ShuffleDesk.hGap = dip2px(ShuffleDesk.hGapDip, getContext());

        ShuffleDesk.buttonCellWidth = DisplayUtil.getScreenWidth(getContext()) / ShuffleDesk.Columns;
        ShuffleDesk.buttonHeight = dip2px(buttonHeightDip, getContext());

        ShuffleDesk.buttonWidth = ShuffleDesk.buttonCellWidth - ShuffleDesk.hGap * 2;
        ShuffleDesk.buttonCellHeight = ShuffleDesk.buttonHeight + ShuffleDesk.vGap * 2;

        ShuffleDesk.minSelectedZoneHeight = ShuffleDesk.buttonCellHeight * 3;
        senator.setStandardMinHeight(ShuffleDesk.minSelectedZoneHeight);

        senator.setList(buttons);
    }

    public void initView() {
        shuffleButtons();
    }

    private void shuffleButtons() {
        senator.shuffleButtons();
    }

    public void setButtons(ArrayList<MovableButton> buttons) {
        this.buttons = buttons;
    }

    public ArrayList<MovableButton> getButtons() {
        ArrayList<MovableButton> buttons = new ArrayList<MovableButton>();
        buttons.addAll(senator.getSortedList());
        return buttons;
    }

    public static int dip2px(float dp, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2dip(float px, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public ShuffleCardSimple getSenator() {
        return senator;
    }
}

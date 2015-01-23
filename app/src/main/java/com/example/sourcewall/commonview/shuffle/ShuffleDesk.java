package com.example.sourcewall.commonview.shuffle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.sourcewall.R;
import com.example.sourcewall.util.DisplayUtil;

import java.util.ArrayList;

public class ShuffleDesk extends RelativeLayout {
    private ArrayList<MovableButton> selectedButtons = new ArrayList<>();
    private ArrayList<MovableButton> unselectedButtons = new ArrayList<>();
    private int buttonHeightDip = 48;
    public static int buttonWidth = 0;
    public static int buttonHeight = 0;
    public static int Columns = 3;
    public static int vGapDip = 2;// x2
    public static int hGapDip = 1;// x2
    public static int vGap = 0;
    public static int hGap = 0;
    public static int buttonCellWidth = 0;
    public static int buttonCellHeight = 0;
    public static int animateVersion = 11;
    public static int minSelectedZoneHeight;
    private ShuffleCardSenator senator;
    private LinearLayout senatorLayout;
    private ShuffleCardCandidate candidate;
    private LinearLayout candidateLayout;
    public static int minButtons = 1;
    public static int maxButtons = 18;

    public ShuffleDesk(Context context) {
        this(context, null);
    }

    public ShuffleDesk(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_shuffle, this);

        candidateLayout = (LinearLayout) findViewById(R.id.CandidateLayout);
        senatorLayout = (LinearLayout) findViewById(R.id.SenatorLayout);

        candidate = (ShuffleCardCandidate) findViewById(R.id.candidate);
        senator = (ShuffleCardSenator) findViewById(R.id.senator);

        candidate.setDesk(this, candidateLayout);
        senator.setDesk(this, senatorLayout);
    }

    public void switch2Edit() {
        candidateLayout.setVisibility(View.GONE);
    }

    public void switch2Normal() {
        candidateLayout.setVisibility(View.VISIBLE);
    }

    public void InitDatas() {

        vGap = dip2px(vGapDip, getContext());
        hGap = dip2px(hGapDip, getContext());

        buttonCellWidth = DisplayUtil.getScreenWidth(getContext()) / Columns;
        buttonHeight = dip2px(buttonHeightDip, getContext());

        buttonWidth = buttonCellWidth - hGap * 2;
        buttonCellHeight = buttonHeight + vGap * 2;

        minSelectedZoneHeight = buttonCellHeight * 3;
        senator.setStandardMinHeight(minSelectedZoneHeight);

        senator.setList(selectedButtons);
        candidate.setList(unselectedButtons);
    }

    public void initView() {
        shuffleButtons();
    }

    private void shuffleButtons() {
        senator.shuffleButtons();
        candidate.shuffleButtons();
    }

    public ArrayList<MovableButton> getButtons() {
        ArrayList<MovableButton> buttons = new ArrayList<MovableButton>();
        buttons.addAll(senator.getSortedList());
        buttons.addAll(candidate.getSortedList());
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

    public ShuffleCardSenator getSenator() {
        return senator;
    }

    public ShuffleCardCandidate getCandidate() {
        return candidate;
    }

    public void setSelectedButtons(ArrayList<MovableButton> selectedButtons) {
        this.selectedButtons = selectedButtons;
    }

    public void setUnselectedButtons(ArrayList<MovableButton> unselectedButtons) {
        this.unselectedButtons = unselectedButtons;
    }
}

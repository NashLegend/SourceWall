package com.example.sourcewall.CommonView.shuffle;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.example.sourcewall.util.ToastUtil;

public class ShuffleCardCandidate extends ShuffleCard {

    public ShuffleCardCandidate(Context context) {
        super(context);
    }

    public ShuffleCardCandidate(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void shuffleButtons() {
        super.shuffleButtons();
        setClickListener();
        targetHeight = computeHeight();
    }

    @Override
    public void banishButton(MovableButton button) {
        super.banishButton(button);
        if (computeHeight() < targetHeight) {
            shrink();
        }
        removeView(button);
        setupAnimator(animateAfter(button.getPosition().y, button.getPosition().x, false));
        setFinalPosition();
    }

    @Override
    public void getResident(final MovableButton button) {
        // TODO 自动生成的方法存根
        setupAnimator(animateAfter(button.getPosition().y, button.getPosition().x, true));
        button.setSelected(false);
        addButtonAt(button, new Point(0, 0));
        if (computeHeight() > targetHeight) {
            expand();
        }
        setFinalPosition();
    }

    public void addButtonAt(MovableButton button, Point point) {
        list.add(button);

        if (computeHeight() > getHeight()) {
            expand();
        }

        point.x = 0;
        point.y = 0;
        button.setPosition(point);
        button.setTargetPosition(new Point(point.x, point.y));

        button.setXX(point.x * ShuffleDesk.buttonCellWidth + ShuffleDesk.hGap);
        button.setYY(point.y * ShuffleDesk.buttonCellHeight + ShuffleDesk.vGap);
        button.setOnClickListener(clickListener);

        this.addView(button);
    }

    public void setClickListener() {
        for (MovableButton movableButton : list) {
            movableButton.setOnClickListener(clickListener);
        }
    }

    private OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (desk.getSenator().getList().size() < ShuffleDesk.maxButtons) {
                banishButton((MovableButton) v);
                desk.getSenator().getResident(((MovableButton) v).clone());
            } else {
                ToastUtil.toastSingleton("订阅数不能超过" + ShuffleDesk.maxButtons + "个");
            }
        }
    };

}


package com.example.sourcewall.commonview.shuffle;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class ShuffleCard extends RelativeLayout {

    public ShuffleDesk desk;
    public LinearLayout parentLayout;
    private Object animator;
    public int targetHeight;
    private OnSizeChangingListener onSizeChangingListener;
    public ArrayList<MovableButton> list;

    public static final int MODE_SHRINK = 0;
    public static final int MODE_EXPAND = 1;

    public ShuffleCard(Context context) {
        super(context);
    }

    public ShuffleCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDesk(ShuffleDesk desk, LinearLayout layout) {
        this.desk = desk;
        parentLayout = layout;
    }

    public ShuffleDesk getDesk() {
        return this.desk;
    }

    public void setHeight(int height) {
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) getLayoutParams();
        params.height = height;
        setLayoutParams(params);
    }

    public int computeHeight() {
        return (int) Math.ceil((double) (list.size()) / ShuffleDesk.Columns)
                * ShuffleDesk.buttonCellHeight;
    }

    public void banishButton(MovableButton button) {
        list.remove(button);
    }

    public void getResident(MovableButton button) {
        list.add(button);
    }

    /**
     * MultiAnimator
     *
     * @param buttons
     */
    public void setupAnimator(ArrayList<MovableButton> buttons) {
        if (buttons != null && buttons.size() > 0) {
            Point point = new Point(0, 0);
            for (MovableButton movableButton : buttons) {
                movableButton.startAnimator(point);
            }
        }
    }

    public void shrink() {
        targetHeight -= ShuffleDesk.buttonCellHeight;
        changeSize(targetHeight);
        notifySizeChanging(targetHeight, MODE_SHRINK);
    }

    public void expand() {
        targetHeight += ShuffleDesk.buttonCellHeight;
        changeSize(targetHeight);
        notifySizeChanging(targetHeight, MODE_EXPAND);
    }

    public void shuffleButtons() {
        for (int i = 0; i < list.size(); i++) {
            MovableButton button = list.get(i);
            Point point = new Point();
            point.x = i % ShuffleDesk.Columns;
            point.y = i / ShuffleDesk.Columns;
            button.setPosition(point);
            button.setTargetPosition(new Point(point.x, point.y));

            LayoutParams params = new LayoutParams(ShuffleDesk.buttonWidth,
                    ShuffleDesk.buttonHeight);
            params.leftMargin = point.x * ShuffleDesk.buttonCellWidth + ShuffleDesk.hGap;
            params.topMargin = point.y * ShuffleDesk.buttonCellHeight + ShuffleDesk.vGap;
            button.setLayoutParams(params);
            this.addView(button);
        }
    }

    public void changeSize(int height) {
        if (Build.VERSION.SDK_INT < ShuffleDesk.animateVersion) {
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) getLayoutParams();
            params.height = height;
            setLayoutParams(params);
        } else {
            if (animator != null && ((ValueAnimator) animator).isRunning()) {
                ((ValueAnimator) animator).cancel();
            }
            animator = ObjectAnimator.ofInt(this, "height", getHeight(), height);
            ((ValueAnimator) animator).setDuration(100);
            ((ValueAnimator) animator).start();
        }
    }

    public void notifySizeChanging(int height, int mode) {
        if (onSizeChangingListener != null) {
            onSizeChangingListener.onSizeChanging(height, mode);
        }
    }

    /**
     * 向index减小方向后退
     *
     * @param buttons
     */
    public void moveBack(ArrayList<MovableButton> buttons) {
        for (Iterator<MovableButton> iterator = buttons.iterator(); iterator.hasNext(); ) {
            MovableButton movableButton = (MovableButton) iterator.next();
            movableButton.setTargetPositionIsPrev();
        }
        setupAnimator(buttons);
    }

    /**
     * 向index增大方向前进
     *
     * @param buttons
     */
    public void moveForward(ArrayList<MovableButton> buttons) {
        for (Iterator<MovableButton> iterator = buttons.iterator(); iterator.hasNext(); ) {
            MovableButton movableButton = (MovableButton) iterator.next();
            movableButton.setTargetPositionIsNext();
        }
        setupAnimator(buttons);
    }

    public ArrayList<MovableButton> animateButtonsBetween(int crtRow, int crtCol, int lastRow,
                                                          int lastCol) {
        boolean movingForward = crtRow * ShuffleDesk.Columns + crtCol - lastRow * ShuffleDesk.Columns
                - lastCol < 0;
        ArrayList<MovableButton> buttons = new ArrayList<MovableButton>();
        for (int i = 0; i < list.size(); i++) {
            MovableButton button = list.get(i);
            if (isBetweenPoint(button.getTargetPosition().y, button.getTargetPosition().x,
                    crtRow, crtCol, lastRow,
                    lastCol)) {
                buttons.add(button);
            }

        }
        if (movingForward) {
            moveForward(buttons);
        } else {
            moveBack(buttons);
        }
        return buttons;
    }

    public void setFinalPosition() {
        for (MovableButton movableButton : list) {
            movableButton.setPosition(new Point(movableButton.getTargetPosition().x,
                    movableButton.getTargetPosition().y));
        }
    }

    private boolean isBetweenPoint(int row, int col, int crtRow, int crtCol, int lastRow,
                                   int lastCol) {
        int tis = row * ShuffleDesk.Columns + col;
        int crt = crtRow * ShuffleDesk.Columns + crtCol;
        int lst = lastRow * ShuffleDesk.Columns + lastCol;
        if (lst < tis && tis <= crt) {
            return true;
        } else if (lst > tis && tis >= crt) {
            return true;
        }
        return false;
    }

    public ArrayList<MovableButton> animateAfter(int lastRow, int lastCol, boolean isTarget) {
        ArrayList<MovableButton> buttons = new ArrayList<MovableButton>();
        for (int i = 0; i < list.size(); i++) {
            MovableButton button = list.get(i);
            if (isAfterPoint(button.getTargetPosition().y, button.getTargetPosition().x, lastRow,
                    lastCol, isTarget)) {
                buttons.add(button);
            }
        }

        if (isTarget) {
            moveForward(buttons);
        } else {
            moveBack(buttons);
        }
        return buttons;
    }

    private boolean isAfterPoint(int row, int col, int crtRow, int crtCol, boolean isTarget) {
        int tis = row * ShuffleDesk.Columns + col;
        int crt = crtRow * ShuffleDesk.Columns + crtCol;
        if (isTarget && tis >= crt) {
            return true;
        }

        if (!isTarget && tis > crt) {
            return true;
        }

        return false;
    }

    public ArrayList<MovableButton> getBetweenButtons() {
        return null;
    }

    public void finalCheck() {
        ButtonComparator comparator = new ButtonComparator();
        Collections.sort(list, comparator);
        for (int i = 0; i < list.size(); i++) {
            MovableButton button = list.get(i);
            Point point = new Point();
            point.x = i % ShuffleDesk.Columns;
            point.y = i / ShuffleDesk.Columns;
            button.setPosition(point);
            button.setTargetPosition(new Point(point.x, point.y));
            button.setXX(point.x * ShuffleDesk.buttonCellWidth + ShuffleDesk.hGap);
            button.setYY(point.y * ShuffleDesk.buttonCellHeight + ShuffleDesk.vGap);
        }
    }

    public ArrayList<MovableButton> getSortedList() {
        ButtonComparator comparator = new ButtonComparator();
        Collections.sort(list, comparator);
        return list;
    }

    public class ButtonComparator implements Comparator<MovableButton> {

        @Override
        public int compare(MovableButton lhs, MovableButton rhs) {
            int com = lhs.getIndex() - rhs.getIndex();
            if (com > 0) {
                return 1;
            } else if (com == 0) {
                return 0;
            } else {
                return -1;
            }

        }
    }

    public interface OnSizeChangingListener {
        public abstract void onSizeChanging(int height, int mode);
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(int targetHeight) {
        this.targetHeight = targetHeight;
    }

    public OnSizeChangingListener getOnSizeChangingListener() {
        return onSizeChangingListener;
    }

    public void setOnSizeChangingListener(OnSizeChangingListener onSizeChangingListener) {
        this.onSizeChangingListener = onSizeChangingListener;
    }

    public ArrayList<MovableButton> getList() {
        return list;
    }

    public void setList(ArrayList<MovableButton> list) {
        this.list = list;
    }

}

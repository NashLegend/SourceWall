package com.example.sourcewall.CommonView.shuffle;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

@SuppressLint("ClickableViewAccessibility")
public class ShuffleCardSimple extends ShuffleCard {
    private int standardMinHeight = 0;
    private MovableButton currentButton;
    private int lastRow = 0;
    private int lastCol = 0;
    private float lastX = 0f;
    private float lastY = 0f;
    private float ddx = 0f;
    private float ddy = 0f;

    public ShuffleCardSimple(Context context) {
        super(context);
    }

    public ShuffleCardSimple(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void moveButton(float dx, float dy) {
        int[] location = {
                0, 0
        };
        this.getLocationOnScreen(location);
        currentButton.setXX(dx - location[0]);
        currentButton.setYY(dy - location[1]);
        getCurrentXone();
    }

    private void getCurrentXone() {
        PointF pointF = getCurrentButtonCenter();
        int crtRow = 0;
        int crtCol = 0;
        if (pointF == null) {
            return;
        }

        crtCol = (int) (pointF.x / ShuffleDesk.buttonCellWidth);
        crtRow = (int) (pointF.y / ShuffleDesk.buttonCellHeight);
        if (crtRow < 0) {
            crtRow = 0;
        }
        if (crtCol < 0) {
            crtCol = 0;
        }
        if (crtCol >= ShuffleDesk.Columns) {
            crtCol = ShuffleDesk.Columns - 1;
        }
        if (crtCol != lastCol || crtRow != lastRow) {
            if (isOnFixedPosition(crtRow, crtCol) && isOnFixedPosition(lastRow, lastCol)) {
                // do nothing
            } else if (isOnFixedPosition(crtRow, crtCol)) {
                animateAfter(lastRow, lastCol, false);
            } else if (isOnFixedPosition(lastRow, lastCol)) {
                animateAfter(crtRow, crtCol, true);
            } else {
                animateButtonsBetween(crtRow, crtCol, lastRow, lastCol);
            }
        }
        currentButton.setTargetPosition(new Point(crtCol, crtRow));
        lastRow = crtRow;
        lastCol = crtCol;
    }

    private boolean isOnFixedPosition(int row, int col) {
        return false;
        // return row == 0 && col <= 2;
    }

    private PointF getCurrentButtonCenter() {
        if (currentButton != null) {
            return new PointF(currentButton.getXX() + currentButton.getWidth() / 2,
                    currentButton.getYY()
                            + currentButton.getHeight() / 2);
        } else {
            return null;
        }
    }

    private void putButtonDown() {
        PointF pointF = getCurrentButtonCenter();
        int crtRow = 0;
        int crtCol = 0;
        ArrayList<MovableButton> buttons = new ArrayList<MovableButton>();
        if (pointF == null) {
            return;
        }
        crtCol = (int) (pointF.x / ShuffleDesk.buttonCellWidth);
        crtRow = (int) pointF.y / ShuffleDesk.buttonCellHeight;
        if (crtRow < 0) {
            crtRow = 0;
        }
        if (crtCol < 0) {
            crtCol = 0;
        }
        if (crtCol >= ShuffleDesk.Columns) {
            crtCol = ShuffleDesk.Columns - 1;
        }

        Point point = new Point(crtCol, crtRow);
        int ind = crtRow * ShuffleDesk.Columns + crtCol;
        if (isOnFixedPosition(crtRow, crtCol) || ind >= list.size() - 1) {
            point.x = (list.size() - 1) % ShuffleDesk.Columns;
            point.y = (list.size() - 1) / ShuffleDesk.Columns;
        }
        currentButton.setSelected(true);
        currentButton.setTargetPosition(point);
        buttons.add(currentButton);

        setupAnimator(buttons);
        setFinalPosition();

        lastRow = 0;
        lastCol = 0;
        currentButton = null;
    }

    @Override
    public void shuffleButtons() {
        super.shuffleButtons();
        setLongListener();
        setClickListener();
        ViewGroup.LayoutParams params = getLayoutParams();
        if (computeHeight() < standardMinHeight) {
            params.height = standardMinHeight;
            setLayoutParams(params);
        } else {
            params.height = computeHeight();
            setLayoutParams(params);
        }
        targetHeight = params.height;
    }

    public void startEditMode() {
        setTouchListener();
        fulfill();
    }

    public void fulfill() {
        //TODO
        targetHeight = deskSimple.getHeight() - parentLayout.getHeight() + getHeight();
        changeSize(targetHeight);
    }

    public void restore() {
        if (computeHeight() < standardMinHeight) {
            targetHeight = standardMinHeight;
        } else {
            targetHeight = computeHeight();
        }
        changeSize(targetHeight);
    }

    public void endEditMode() {
        setLongListener();
        setClickListener();
        restore();
    }

    public void setTouchListener() {
        for (MovableButton movableButton : list) {
            movableButton.setOnTouchListener(listener);
            movableButton.setOnLongClickListener(null);
            movableButton.setOnClickListener(null);
        }
    }

    public void setLongListener() {
        for (MovableButton movableButton : list) {
            movableButton.setOnLongClickListener(longClickListener);
            movableButton.setOnTouchListener(null);
        }
    }

    public void setClickListener() {
        for (MovableButton movableButton : list) {
            movableButton.setOnClickListener(clickListener);
            movableButton.setOnTouchListener(null);
        }
    }

    public void setStandardMinHeight(int standardMinHeight) {
        this.standardMinHeight = standardMinHeight;
    }

    private OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            //TODO
        }
    };

    private OnLongClickListener longClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            currentButton = (MovableButton) v;
            Vibrator vibrator = (Vibrator) getContext().getSystemService(
                    Service.VIBRATOR_SERVICE);
            vibrator.vibrate(20);
            currentButton = (MovableButton) v;
            startEditMode();
            int[] location = {
                    0, 0
            };
            v.getLocationOnScreen(location);
            lastX = location[0] + v.getWidth() / 2;
            lastY = location[1] + v.getHeight() / 2;
            ddx = v.getWidth() / 2;
            ddy = v.getHeight() / 2;
            lastRow = currentButton.getPosition().y;
            lastCol = currentButton.getPosition().x;
            currentButton.bringToFront();
            scrollView.requestDisallowInterceptTouchEvent(true);
            return false;
        }
    };

    private OnTouchListener listener = new OnTouchListener() {

        private long finalCheckInt = 400;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if ((currentButton == null && event.getAction() == MotionEvent.ACTION_DOWN)
                    || currentButton == v) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (currentButton == null) {
                            currentButton = (MovableButton) v;
                            lastX = event.getRawX();
                            lastY = event.getRawY();
                            lastRow = currentButton.getPosition().y;
                            lastCol = currentButton.getPosition().x;
                            ddx = event.getX();
                            ddy = event.getY();
                            currentButton.bringToFront();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (currentButton != null) {
                            float rawX = event.getRawX();
                            float rawY = event.getRawY();

                            int offset = 9;
                            int[] pos = {0, 0};
                            scrollView.getLocationOnScreen(pos);
                            int top = pos[1];
                            int bottom = top + scrollView.getHeight();
                            if (rawY - ShuffleDesk.buttonHeight / 2 < top) {
                                scrollView.scrollBy(0, -10);
                                offset = 0;
                            } else if (rawY + ShuffleDesk.buttonHeight / 2 > bottom) {
                                scrollView.scrollBy(0, 10);
                                offset = 0;
                            }

                            float dx = rawX - lastX;
                            float dy = rawY - lastY;
                            if (dx * dx + dy * dy > offset) {
                                lastX = rawX;
                                lastY = rawY;
                                moveButton(rawX - ddx, offset + rawY - ddy);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        if (currentButton != null) {
                            moveButton(event.getRawX() - ddx, event.getRawY() - ddy);
                            putButtonDown();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finalCheck();
                                    endEditMode();
                                }
                            }, finalCheckInt);
                        }
                        break;
                    default:
                        break;
                }
            }
            return false;
        }
    };
}

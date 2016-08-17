package net.nashlegend.sourcewall.view.common.shuffle;

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
import android.widget.LinearLayout;

import java.util.ArrayList;

@SuppressLint("ClickableViewAccessibility")
public class ShuffleCardSenator extends ShuffleCard {
    private int standardMinHeight = 0;
    private MovableButton currentButton;
    private int lastRow = 0;
    private int lastCol = 0;
    private float lastX = 0f;
    private float lastY = 0f;
    private float ddx = 0f;
    private float ddy = 0f;

    public ShuffleCardSenator(Context context) {
        super(context);
    }

    public ShuffleCardSenator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void banishButton(MovableButton button) {
        list.remove(button);
        if ((targetHeight > standardMinHeight) && (computeHeight() < targetHeight)) {
            shrink();
        }
        setupAnimator(animateAfter(button.getPosition().y, button.getPosition().x, false));
        removeView(button);
        setFinalPosition();
        button.setOnLongClickListener(null);
        button.setOnTouchListener(null);
        button.setOnClickListener(null);
    }

    @Override
    public void getResident(MovableButton button) {
        super.getResident(button);
        if (computeHeight() > targetHeight && computeHeight() > standardMinHeight) {
            expand();
        }
        int i = list.size() - 1;
        Point point = new Point();
        point.x = i % ShuffleDesk.Columns;
        point.y = i / ShuffleDesk.Columns;
        button.setPosition(point);
        button.setTargetPosition(new Point(point.x, point.y));

        LayoutParams params = (LayoutParams) button.getLayoutParams();

        params.leftMargin = point.x * ShuffleDesk.buttonCellWidth + ShuffleDesk.hGap;
        params.topMargin = point.y * ShuffleDesk.buttonCellHeight + ShuffleDesk.vGap;

        button.setSelected(true);
        button.setOnClickListener(clickListener);
        button.setOnLongClickListener(longClickListener);
        this.addView(button);
    }

    private void moveButton(float dx, float dy) {
        int[] location = {0, 0};
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
            return new PointF(currentButton.getXX() + currentButton.getWidth() / 2, currentButton.getYY() + currentButton.getHeight() / 2);
        } else {
            return null;
        }
    }

    private void putButtonDown() {
        PointF pointF = getCurrentButtonCenter();
        int crtRow = 0;
        int crtCol = 0;
        ArrayList<MovableButton> buttons = new ArrayList<>();
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
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
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
        desk.switch2Edit();
        fulfill();
    }

    public void fulfill() {
        targetHeight = desk.getHeight() - parentLayout.getHeight() + getHeight();
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
        desk.switch2Normal();
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

    public int getStandardMinHeight() {
        return standardMinHeight;
    }

    public void setStandardMinHeight(int standardMinHeight) {
        this.standardMinHeight = standardMinHeight;
    }

    private OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (list.size() > ShuffleDesk.minButtons) {
                MovableButton button = (MovableButton) v;
                banishButton(button);//
                desk.getCandidate().getResident(button.cloneButton());
            }
        }
    };

    private OnLongClickListener longClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            currentButton = (MovableButton) v;
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(20);
            currentButton = (MovableButton) v;
            startEditMode();
            int[] location = {0, 0};
            v.getLocationOnScreen(location);
            lastX = location[0] + v.getWidth() / 2;
            lastY = location[1] + v.getHeight() / 2;
            ddx = v.getWidth() / 2;
            ddy = v.getHeight() / 2;
            lastRow = currentButton.getPosition().y;
            lastCol = currentButton.getPosition().x;
            currentButton.bringToFront();
            return false;
        }
    };

    private OnTouchListener listener = new OnTouchListener() {

        private long finalCheckInt = 400;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if ((currentButton == null && event.getAction() == MotionEvent.ACTION_DOWN) || currentButton == v) {
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
                            float dx = event.getRawX() - lastX;
                            float dy = event.getRawY() - lastY;
                            if (dx * dx + dy * dy > 9) {
                                lastX = event.getRawX();
                                lastY = event.getRawY();
                                moveButton(event.getRawX() - ddx, event.getRawY() - ddy);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
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

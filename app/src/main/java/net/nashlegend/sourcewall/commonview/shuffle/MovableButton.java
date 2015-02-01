package net.nashlegend.sourcewall.commonview.shuffle;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.nashlegend.sourcewall.R;

/**
 * @author NashLegend
 */
public abstract class MovableButton<T> extends RelativeLayout {
    protected String title = "";
    protected int id = 1;
    protected Point position = new Point(0, 0);
    protected Point targetPosition = new Point(0, 0);
    protected Object animator;
    protected T section;
    protected boolean selected = false;
    protected Button button;
    protected ImageView imageView;

    public Point getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Point targetPosition) {
        this.targetPosition = targetPosition;
    }


    public MovableButton(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.button_movable, this);
        button = (Button) findViewById(R.id.button_mov);
        imageView = (ImageView) findViewById(R.id.section_new_hint);
    }

    public void setTargetPositionIsNext() {
        if (targetPosition.x < (ShuffleDesk.Columns - 1)) {
            targetPosition.x++;
        } else {
            targetPosition.x = 0;
            targetPosition.y++;
        }
    }

    public void setTargetPositionIsPrev() {
        if (targetPosition.x == 0 && targetPosition.y > 0) {
            targetPosition.x = ShuffleDesk.Columns - 1;
            targetPosition.y--;
        } else if (targetPosition.x > 0) {
            targetPosition.x--;
        }
    }

    public int getIndex() {
        return position.y * ShuffleDesk.Columns + position.x;
    }

    public void startAnimator(Point anchorPoint) {
        if (Build.VERSION.SDK_INT < ShuffleDesk.animateVersion) {
            LayoutParams params = (LayoutParams) getLayoutParams();
            params.leftMargin = ShuffleDesk.buttonCellWidth * targetPosition.x + ShuffleDesk.hGap;
            params.topMargin = ShuffleDesk.buttonCellHeight * targetPosition.y + anchorPoint.y
                    + ShuffleDesk.vGap;
            setLayoutParams(params);
        } else {
            if (animator != null && ((ValueAnimator) animator).isRunning()) {
                ((ValueAnimator) animator).cancel();
            }
            PropertyValuesHolder holderx = PropertyValuesHolder.ofFloat("x", getX(),
                    ShuffleDesk.buttonCellWidth
                            * targetPosition.x + ShuffleDesk.hGap);
            PropertyValuesHolder holdery = PropertyValuesHolder.ofFloat("y", getY(),
                    ShuffleDesk.buttonCellHeight
                            * targetPosition.y + anchorPoint.y + ShuffleDesk.vGap);
            animator = ObjectAnimator.ofPropertyValuesHolder(this, holderx, holdery);
            ((ObjectAnimator) animator).setDuration(300);
            ((ObjectAnimator) animator).start();
        }
    }

    public void setXX(float x) {
        if (Build.VERSION.SDK_INT < ShuffleDesk.animateVersion) {
            LayoutParams params = (LayoutParams) getLayoutParams();
            params.leftMargin = (int) x;
            setLayoutParams(params);
        } else {
            super.setX(x);
        }
    }

    public void setYY(float y) {
        if (Build.VERSION.SDK_INT < ShuffleDesk.animateVersion) {
            LayoutParams params = (LayoutParams) getLayoutParams();
            params.topMargin = (int) y;
            setLayoutParams(params);
        } else {
            super.setY(y);
        }
    }

    public float getXX() {
        if (Build.VERSION.SDK_INT < ShuffleDesk.animateVersion) {
            return this.getLeft();
        } else {
            return super.getX();
        }
    }

    public float getYY() {
        if (Build.VERSION.SDK_INT < ShuffleDesk.animateVersion) {
            return this.getTop();
        } else {
            return super.getY();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        button.setText(title);
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public abstract MovableButton clone();

    public abstract T getSection();

    public abstract void setSection(T section);

}

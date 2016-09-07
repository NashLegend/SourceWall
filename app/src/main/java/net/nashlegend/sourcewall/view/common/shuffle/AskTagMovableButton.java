package net.nashlegend.sourcewall.view.common.shuffle;

import android.content.Context;

import net.nashlegend.sourcewall.data.database.gen.AskTag;

/**
 * Created by NashLegend on 2015/1/20 0020
 */
public class AskTagMovableButton extends MovableButton<AskTag> {

    public AskTagMovableButton(Context context) {
        super(context);
    }

    public MovableButton cloneButton() {
        AskTagMovableButton button = new AskTagMovableButton(getContext());
        LayoutParams params = new LayoutParams(ShuffleDesk.buttonWidth, ShuffleDesk.buttonHeight);
        button.setLayoutParams(params);
        button.setSection(section);
        return button;
    }

    @Override
    public AskTag getSection() {
        section.setOrder(getIndex());
        section.setSelected(selected);
        return section;
    }

    @Override
    public void setSection(AskTag section) {
        this.section = section;
        setTitle(section.getName());
        setSelected(section.getSelected());
    }
}

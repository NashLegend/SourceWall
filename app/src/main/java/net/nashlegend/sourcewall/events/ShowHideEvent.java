package net.nashlegend.sourcewall.events;

import net.nashlegend.sourcewall.model.SubItem;

/**
 * Created by NashLegend on 16/9/20.
 */

public class ShowHideEvent {
    public int section = SubItem.Section_Article;
    public boolean show = false;

    public ShowHideEvent(int section, boolean show) {
        this.section = section;
        this.show = show;
    }
}

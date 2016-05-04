package net.nashlegend.sourcewall.events;

import net.nashlegend.sourcewall.model.SubItem;

/**
 * Created by NashLegend on 16/5/5.
 */
public class OpenContentFragmentEvent {
    public SubItem subItem;

    public OpenContentFragmentEvent(SubItem subItem) {
        this.subItem = subItem;
    }
}

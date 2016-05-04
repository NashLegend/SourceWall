package net.nashlegend.sourcewall.events;

import net.nashlegend.sourcewall.model.SubItem;

/**
 * Created by NashLegend on 16/5/5.
 */
public class PrepareOpenContentFragmentEvent {
    public SubItem subItem;

    public PrepareOpenContentFragmentEvent(SubItem subItem) {
        this.subItem = subItem;
    }
}

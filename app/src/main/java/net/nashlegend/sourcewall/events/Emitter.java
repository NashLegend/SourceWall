package net.nashlegend.sourcewall.events;

import de.greenrobot.event.EventBus;

/**
 * Created by NashLegend on 2016/9/28.
 */

public class Emitter {
    public static void register(Object object) {
        if (object == null) {
            return;
        }
        EventBus.getDefault().register(object);
    }

    public static void unregister(Object object) {
        if (object == null) {
            return;
        }
        EventBus.getDefault().unregister(object);
    }

    public static void emit(Object object) {
        if (object == null) {
            return;
        }
        EventBus.getDefault().post(object);
    }
}

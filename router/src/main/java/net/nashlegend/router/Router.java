package net.nashlegend.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by NashLegend on 16/4/20.
 */
public class Router {

    private static final List<ActionRoute> actionRoutes = new ArrayList<>();
    private static final List<ViewRoute> viewRoutes = new ArrayList<>();

    public static final String RAW_URI = "net.nashlegend.router.raw.uri";

    private static void initIfNeed() {
        if (!actionRoutes.isEmpty() || !viewRoutes.isEmpty()) {
            return;
        }
        try {
            Class<?> clazz = Class.forName("net.nashlegend.router.Tracks");
            clazz.getMethod("trackAllRoutine").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void ViewRoute(String format, Class<? extends Activity> activity, ExtraTypes extraTypes) {
        viewRoutes.add(new ViewRoute(format, extraTypes, activity));
    }

    static void ActionRoute(String format, Class<? extends Action> action, ExtraTypes extraTypes) {
        actionRoutes.add(new ActionRoute(format, extraTypes, action));
    }

    static void sort() {
        Collections.sort(viewRoutes, new Comparator<ViewRoute>() {
            @Override
            public int compare(ViewRoute lhs, ViewRoute rhs) {
                return lhs.getFormat().compareTo(rhs.getFormat()) * -1;
            }
        });

        Collections.sort(actionRoutes, new Comparator<ActionRoute>() {
            @Override
            public int compare(ActionRoute lhs, ActionRoute rhs) {
                return lhs.getFormat().compareTo(rhs.getFormat()) * -1;
            }
        });
    }

    public static boolean open(String uri, Context context) {
        return open(Uri.parse(uri), context);
    }

    public static boolean open(Uri uri, Context context) {
        initIfNeed();
        Path path = Path.create(uri);
        if (context != null) {
            for (ViewRoute rout : viewRoutes) {
                if (rout.match(path, uri)) {
                    Intent intent = new Intent(context, rout.getActivity());
                    intent.putExtras(rout.parseExtras(uri));
                    if (!(context instanceof Activity)) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    context.startActivity(intent);
                    return true;
                }
            }
        }
        for (ActionRoute rout : actionRoutes) {
            if (rout.match(path, uri)) {
                rout.execute(uri);
                return true;
            }
        }
        return false;
    }

    public static boolean action(String uri) {
        return action(Uri.parse(uri));
    }

    public static boolean action(Uri uri) {
        initIfNeed();
        Path path = Path.create(uri);
        for (ActionRoute rout : actionRoutes) {
            if (rout.match(path, uri)) {
                rout.execute(uri);
                return true;
            }
        }
        return false;
    }
}

package net.nashlegend.router;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by NashLegend on 16/4/20.
 */
public class ActionRoute extends BaseRoute {

    private Class<? extends Action> mAction;

    public ActionRoute(@NonNull String format, @NonNull ExtraTypes extras, @NonNull Class<? extends Action> action) {
        super(format, extras);
        this.mAction = action;
    }

    protected void execute(Uri uri) {
        try {
            Bundle bundle = parseExtras(uri);
            Action action = mAction.newInstance();
            action.execute(bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Action newActionInstance() throws Exception {
        return mAction.newInstance();
    }
}

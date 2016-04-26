package net.nashlegend.router;

import android.app.Activity;

/**
 * Created by NashLegend on 16/4/20.
 */
public class ViewRoute extends BaseRoute {

    private Class<? extends Activity> activity;

    public ViewRoute(String host, ExtraTypes extras, Class<? extends Activity> activity) {
        super(host, extras);
        this.activity = activity;
    }

    protected void execute() {
        //startActivity
    }

    public Class<? extends Activity> getActivity() {
        return activity;
    }
}

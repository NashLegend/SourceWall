package net.nashlegend.mvp.empty;

import net.nashlegend.mvp.presenter.ActivityPresenter;
import net.nashlegend.mvp.view.ActivityView;

/**
 * Created by NashLegend on 16/1/30.
 */
public class EmptyActivityPresenter extends ActivityPresenter<ActivityView>{
    public EmptyActivityPresenter(ActivityView view) {
        super(view);
    }
}

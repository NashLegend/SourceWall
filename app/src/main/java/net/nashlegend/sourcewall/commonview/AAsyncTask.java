package net.nashlegend.sourcewall.commonview;

import android.os.AsyncTask;

/**
 * Created by NashLegend on 2015/2/6 0006
 */
public abstract class AAsyncTask<Params, Progress, Result> extends AsyncTask {

    @Override
    protected void onPreExecute() {
        //addToStackedTasks here
    }

    @Override
    protected void onPostExecute(Object o) {
        //removeFromStackedTasks here
    }

    @Override
    protected void onCancelled() {
        //removeFromStackedTasks here
    }
}

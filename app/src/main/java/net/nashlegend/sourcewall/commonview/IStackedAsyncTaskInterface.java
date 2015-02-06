package net.nashlegend.sourcewall.commonview;

/**
 * Created by NashLegend on 2015/2/6 0006
 * 管理AsyncTask队列的接口，最好是使用一个自定义的AsyncTask，
 * 减少手动addToStackedTasks和removeFromStackedTasks的操作。
 * TODO
 */
public interface IStackedAsyncTaskInterface {
    /**
     * 将AsyncTask添加到队列，应该在AsyncTask.onPreExecute中执行
     *
     * @param task 要添加的AsyncTask
     */
    public void addToStackedTasks(AAsyncTask task);

    /**
     * 将AsyncTask从队列中删除，但是不负责取消执行。
     * 应该在AsyncTask.onCancelled和AsyncTask.onPostExecute中执行
     * 如果从AsyncTask.onCancelled方法里调用。
     * 由于stopAllTasks会调用AsyncTask.cancel，所以最后会多执行一次，我擦。
     * 可惜AsyncTask的finish方法是private的，要不写在此处就行了
     *
     * @param task 要清除的AsyncTask
     */
    public void removeFromStackedTasks(AAsyncTask task);

    /**
     * 清空所有队列中的Task，但是不负责取消执行
     */
    public void flushAllTasks();

    /**
     * 取消所有AsyncTask的执行
     */
    public void stopAllTasks();
}

package net.nashlegend.sourcewall.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.NoticeAdapter;
import net.nashlegend.sourcewall.commonview.AAsyncTask;
import net.nashlegend.sourcewall.commonview.IStackedAsyncTaskInterface;
import net.nashlegend.sourcewall.commonview.LListView;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.UrlCheckUtil;
import net.nashlegend.sourcewall.view.NoticeView;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2015/2/12 0012
 */
public class NoticesFragment extends ChannelsFragment implements LListView.OnRefreshListener, LoadingView.ReloadListener {

    private NoticeAdapter adapter;
    private LListView listView;
    private LoadingView loadingView;
    private LoaderTask task;
    private IgnoreTask ignoreTask;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice_list, container, false);
        loadingView = (LoadingView) view.findViewById(R.id.notice_loading_view);
        loadingView.setReloadListener(this);
        listView = (LListView) view.findViewById(R.id.notice_list);
        adapter = new NoticeAdapter(getActivity());
        listView.setCanPullToRefresh(false);
        listView.setCanPullToLoadMore(false);
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (view instanceof NoticeView) {
                    //这里要做两个请求，但是可以直接请求notice地址，让系统主动删除请求 TODO
                    MobclickAgent.onEvent(getActivity(), Mob.Event_Open_One_Notice);
                    Notice notice = ((NoticeView) view).getData();
                    UrlCheckUtil.redirectRequest(notice.getUrl(), notice.getId());
                }
            }
        });
        return view;
    }

    @Override
    public void onCreateViewAgain(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // do nothing
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        cancelPotentialTask();
        task = new LoaderTask(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void cancelPotentialTask() {
        if (task != null && task.getStatus() == AAsyncTask.Status.RUNNING) {
            task.cancel(true);
        }
        listView.doneOperation();
    }

    @Override
    public void setTitle() {
        loadData();
    }

    @Override
    public void onStartRefresh() {
        loadData();
    }

    @Override
    public void onStartLoadMore() {
        loadData();
    }

    @Override
    public void reload() {
        loadData();
    }

    @Override
    public int getFragmentMenu() {
        return R.menu.menu_notice_center;
    }

    @Override
    public boolean takeOverMenuInflate(MenuInflater inflater, Menu menu) {
        inflater.inflate(getFragmentMenu(), menu);
        return true;
    }

    @Override
    public boolean takeOverOptionsItemSelect(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_ignore_all:
                if (adapter.getCount() > 0) {
                    MobclickAgent.onEvent(getActivity(), Mob.Event_Ignore_All_Notice);
                    cancelPotentialTask();
                    if (ignoreTask != null && ignoreTask.getStatus() == AsyncTask.Status.RUNNING) {
                        ignoreTask.cancel(true);
                    }
                    ignoreTask = new IgnoreTask();
                    ignoreTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;
        }
        return true;
    }

    @Override
    public boolean takeOverBackPressed() {
        return false;
    }

    @Override
    public void resetData(SubItem subItem) {

    }

    @Override
    public void triggerRefresh() {

    }

    @Override
    public void prepareLoading(SubItem sub) {

    }

    class LoaderTask extends AAsyncTask<Integer, Integer, ResultObject> {

        LoaderTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected ResultObject doInBackground(Integer... params) {
            return UserAPI.getNoticeList();
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                loadingView.onLoadSuccess();
                ArrayList<Notice> ars = (ArrayList<Notice>) resultObject.result;
                if (ars.size() == 0) {
                    toast(R.string.no_notice);
                }
                adapter.setList(ars);
                adapter.notifyDataSetInvalidated();
            } else {
                loadingView.onLoadFailed();
                toast(R.string.load_failed);
            }
            listView.setCanPullToRefresh(true);
            listView.doneOperation();
        }
    }

    class IgnoreTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage(getString(R.string.message_replying));
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    IgnoreTask.this.cancel(true);
                }
            });
            progressDialog.show();
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            return UserAPI.ignoreAllNotice();
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }
            if (isActive()) {
                if (resultObject.ok) {
                    listView.setCanPullToRefresh(true);
                    listView.doneOperation();
                    adapter.clear();
                    adapter.notifyDataSetInvalidated();
                } else {
                    toast("忽略未遂");
                }
            }
        }
    }
}

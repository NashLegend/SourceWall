package net.nashlegend.sourcewall.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.NoticeAdapter;
import net.nashlegend.sourcewall.commonview.LListView;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.connection.ResultObject;
import net.nashlegend.sourcewall.connection.api.UserAPI;
import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.util.UrlCheckUtil;
import net.nashlegend.sourcewall.view.NoticeView;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2015/2/12 0012
 */
public class NoticesFragment extends BaseFragment implements LListView.OnRefreshListener, LoadingView.ReloadListener {

    private NoticeAdapter adapter;
    private LListView listView;
    private LoadingView loadingView;
    private LoaderTask task;

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
        task = new LoaderTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void cancelPotentialTask() {
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
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

    class LoaderTask extends AsyncTask<Integer, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(Integer... params) {
            return UserAPI.getNoticeList();
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                loadingView.onLoadSuccess();
                ArrayList<Notice> ars = (ArrayList<Notice>) resultObject.result;
                adapter.setList(ars);
                adapter.notifyDataSetInvalidated();
            } else {
                loadingView.onLoadFailed();
                ToastUtil.toast(R.string.load_failed);
            }
            listView.setCanPullToRefresh(true);
            listView.doneOperation();
        }
    }
}

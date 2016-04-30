package net.nashlegend.sourcewall.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import net.nashlegend.sourcewall.view.common.LListView;
import net.nashlegend.sourcewall.view.common.LoadingView;
import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.MessageAPI;
import net.nashlegend.sourcewall.util.CommonUtil;
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
                if (CommonUtil.shouldThrottle()) {
                    return;
                }
                if (view instanceof NoticeView) {
                    //这里要做两个请求，但是可以直接请求notice地址，让系统主动删除请求 TODO
                    MobclickAgent.onEvent(getActivity(), Mob.Event_Open_One_Notice);
                    Notice notice = ((NoticeView) view).getData();
                    if (!UrlCheckUtil.redirectRequest(notice.getUrl(), notice.getId())) {
                        MessageAPI.ignoreOneNotice(notice.getId());
                    }
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

    RequestObject requestObject;

    private void loadData() {
        cancelPotentialTask();
        requestObject = MessageAPI.getNoticeList(new RequestObject.CallBack<ArrayList<Notice>>() {
            @Override
            public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<ArrayList<Notice>> result) {
                loadingView.onLoadFailed();
                toast(R.string.load_failed);
                listView.setCanPullToRefresh(true);
                listView.doneOperation();
            }

            @Override
            public void onSuccess(@NonNull ArrayList<Notice> result, @NonNull ResponseObject<ArrayList<Notice>> detailed) {
                loadingView.onLoadSuccess();
                if (result.size() == 0) {
                    toast(R.string.no_notice);
                }
                adapter.setList(result);
                adapter.notifyDataSetInvalidated();
                listView.setCanPullToRefresh(true);
                listView.doneOperation();
            }
        });
    }

    private void cancelPotentialTask() {
        if (requestObject != null) {
            requestObject.softCancel();
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
                    ignoreAll();
                }
                break;
        }
        return true;
    }

    private void ignoreAll() {
        final RequestObject requestObject = MessageAPI.ignoreAllNotice(new RequestObject.CallBack<Boolean>() {
            @Override
            public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<Boolean> result) {
                CommonUtil.dismissDialog(progressDialog);
                toast("忽略未遂");
            }

            @Override
            public void onSuccess(@NonNull Boolean result, @NonNull ResponseObject<Boolean> detailed) {
                CommonUtil.dismissDialog(progressDialog);
                listView.setCanPullToRefresh(true);
                listView.doneOperation();
                adapter.clear();
                adapter.notifyDataSetInvalidated();
            }
        });

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.message_wait_a_minute));
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                requestObject.softCancel();
            }
        });
        progressDialog.show();
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

    @Override
    public void scrollToHead() {
        listView.setSelection(0);
    }
}

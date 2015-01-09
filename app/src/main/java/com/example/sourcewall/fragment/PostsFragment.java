package com.example.sourcewall.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;

import com.example.sourcewall.PostActivity;
import com.example.sourcewall.PublishPostActivity;
import com.example.sourcewall.R;
import com.example.sourcewall.adapters.PostAdapter;
import com.example.sourcewall.commonview.LListView;
import com.example.sourcewall.commonview.LoadingView;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.PostAPI;
import com.example.sourcewall.model.Post;
import com.example.sourcewall.model.SubItem;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.ToastUtil;
import com.example.sourcewall.view.PostListItemView;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/18 0018
 * 这几个Fragment结构几乎一模一样，简直浪费
 * 对于更新很快的地方，下拉加载更多的时候列表不应该只是简单的叠加在一起，
 * 因为这样会导致一个列表里面出现多个相同的帖子，只能一屏展示一页才对。
 * 因此要改为按页加载，还要提供加载上一页的功能，按时间倒序排列的都有这问题……
 * 我擦……
 */
public class PostsFragment extends ChannelsFragment implements LListView.OnRefreshListener {
    private LListView listView;
    private PostAdapter adapter;
    private LoaderTask task;
    private SubItem subItem;
    private LoadingView loadingView;
    private int currentPage = -1;//page从0开始，-1表示还没有数据
    private View headerView;
    private final int Code_Publish_post = 1044;

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        subItem = (SubItem) getArguments().getSerializable(Consts.Extra_SubItem);
        headerView = inflater.inflate(R.layout.layout_header_load_pre_page, null, false);
        loadingView = (LoadingView) view.findViewById(R.id.post_progress_loading);
        listView = (LListView) view.findViewById(R.id.list_posts);
        adapter = new PostAdapter(getActivity());
        listView.setCanPullToRefresh(false);
        listView.setCanPullToLoadMore(false);
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), PostActivity.class);
                intent.putExtra(Consts.Extra_Post, ((PostListItemView) view).getPost());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
            }
        });

        listView.addHeaderView(headerView);
        headerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (headerView.getLayoutParams() != null) {
                    headerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    headerView.getLayoutParams().height = 1;
                    headerView.setVisibility(View.GONE);
                }
            }
        });
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPrePage();
            }
        });
        //防止滑动headerView的时候下拉上拉
        headerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        listView.requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        listView.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
        setTitle();
        loadOver();
        return view;
    }

    @Override
    public void onCreateViewAgain(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SubItem mSubItem = (SubItem) getArguments().getSerializable(Consts.Extra_SubItem);
        resetData(mSubItem);
    }

    @Override
    public void setTitle() {
        if (subItem.getType() == SubItem.Type_Collections) {
            getActivity().setTitle("小组热贴");
        } else {
            getActivity().setTitle(this.subItem.getName() + " -- 小组");
        }
    }

    private void loadOver() {
        loadingView.setVisibility(View.VISIBLE);
        loadData(0);
    }

    private void loadData(int offset) {
        if (offset < 0) {
            offset = 0;
        }
        cancelPotentialTask();
        task = new LoaderTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, offset);
    }

    private void loadPrePage() {
        listView.setCanPullToLoadMore(false);
        listView.setCanPullToRefresh(false);
        headerView.findViewById(R.id.text_header_load_hint).setVisibility(View.INVISIBLE);
        headerView.findViewById(R.id.progress_header_loading).setVisibility(View.VISIBLE);
        loadData(currentPage - 1);
    }

    private void writePost() {
        Intent intent = new Intent(getActivity(), PublishPostActivity.class);
        intent.putExtra(Consts.Extra_SubItem, subItem);
        startActivityForResult(intent, Code_Publish_post);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Code_Publish_post && resultCode == Activity.RESULT_OK) {
            //Publish OK
        }
    }

    @Override
    public void onStartRefresh() {
        //TODO
        headerView.getLayoutParams().height = 1;
        headerView.setVisibility(View.GONE);
        loadData(0);
    }

    @Override
    public void onStartLoadMore() {
        //TODO
        loadData(currentPage + 1);
    }

    @Override
    public int getFragmentMenu() {
        return R.menu.menu_fragment_post;
    }

    @Override
    public void takeOverMenuInflate(MenuInflater inflater, Menu menu) {
        inflater.inflate(getFragmentMenu(), menu);
    }

    @Override
    public boolean takeOverOptionsItemSelect(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_write_post:
                writePost();
                break;
        }
        return true;
    }

    @Override
    public void resetData(SubItem subItem) {
        if (subItem.equals(this.subItem)) {
            triggerRefresh();
        } else {
            currentPage = -1;
            this.subItem = subItem;
            adapter.clear();
            adapter.notifyDataSetInvalidated();
            listView.setCanPullToRefresh(false);
            listView.setCanPullToLoadMore(false);
            headerView.getLayoutParams().height = 1;
            headerView.setVisibility(View.GONE);
            loadOver();
        }
        setTitle();
    }

    @Override
    public void triggerRefresh() {
        //TODO
        listView.startRefreshing();
    }

    private void cancelPotentialTask() {
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
            listView.doneOperation();
        }
    }

    /**
     * 这几个Task都长得很像，可以封装起来
     */
    class LoaderTask extends AsyncTask<Integer, Integer, ResultObject> {

        int loadedPage;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ResultObject doInBackground(Integer... datas) {
            loadedPage = datas[0];
            //解析html的page是从1开始的，所以offset要+1
            if (subItem.getType() == SubItem.Type_Collections) {
                return PostAPI.getGroupHotPostListFromMobileUrl(loadedPage + 1);
            } else if (subItem.getType() == SubItem.Type_Private_Channel) {
                return PostAPI.getMyGroupRecentRepliesPosts(loadedPage + 1);
            } else {
                //如果是最后一页，低于20条，那么就会有问题——也就是请求不到数据
                return PostAPI.getGroupPostListByJsonUrl(subItem.getValue(), loadedPage * 20);
            }
        }

        @Override
        protected void onPostExecute(ResultObject o) {
            loadingView.setVisibility(View.GONE);
            if (!isCancelled()) {
                if (o.ok) {
                    ArrayList<Post> ars = (ArrayList<Post>) o.result;
                    if (ars.size() > 0) {
                        currentPage = loadedPage;
                        adapter.setList(ars);
                        adapter.notifyDataSetInvalidated();
                        listView.smoothScrollToPosition(0);
                        if (currentPage > 0) {
                            headerView.setVisibility(View.VISIBLE);
                            headerView.getLayoutParams().height = 0;
                        } else {
                            headerView.getLayoutParams().height = 1;
                            headerView.setVisibility(View.GONE);
                        }
                    } else {
                        //没有数据，页码不变
                        ToastUtil.toast("No Data Loaded");
                    }
                } else {
                    ToastUtil.toast("Load Error");
                }
                if (adapter.getCount() > 0) {
                    listView.setCanPullToLoadMore(true);
                    listView.setCanPullToRefresh(true);
                } else {
                    listView.setCanPullToLoadMore(false);
                    listView.setCanPullToRefresh(true);
                }
                headerView.findViewById(R.id.text_header_load_hint).setVisibility(View.VISIBLE);
                headerView.findViewById(R.id.progress_header_loading).setVisibility(View.GONE);
                listView.doneOperation();
            }
        }
    }
}

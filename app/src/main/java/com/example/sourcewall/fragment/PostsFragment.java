package com.example.sourcewall.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.sourcewall.PostActivity;
import com.example.sourcewall.R;
import com.example.sourcewall.adapters.PostAdapter;
import com.example.sourcewall.commonview.LListView;
import com.example.sourcewall.commonview.LoadingView;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.PostAPI;
import com.example.sourcewall.model.Post;
import com.example.sourcewall.model.SubItem;
import com.example.sourcewall.util.Consts;
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
    private int currentPage = 1;//page从1开始而不是0

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        subItem = (SubItem) getArguments().getSerializable(Consts.Extra_SubItem);
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
        setTitle();
        loadOver();
        return view;
    }

    private void setTitle() {
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
        cancelPotentialTask();
        task = new LoaderTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, offset);
    }

    @Override
    public void onStartRefresh() {
        //TODO
        loadData(0);
    }

    @Override
    public void onStartLoadMore() {
        //TODO
        loadData(adapter.getCount());
    }

    @Override
    public void resetData(SubItem subItem) {
        if (subItem.equals(this.subItem)) {
            triggerRefresh();
        } else {
            this.subItem = subItem;
            setTitle();
            adapter.clear();
            adapter.notifyDataSetInvalidated();
            listView.setCanPullToRefresh(false);
            listView.setCanPullToLoadMore(false);
            loadOver();
        }
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

        int offset;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(Integer... datas) {
            offset = datas[0];
            ArrayList<Post> posts = new ArrayList<Post>();
            ResultObject resultObject = new ResultObject();
            try {
                //在前两种情况下offset好像不对……
                if (subItem.getType() == SubItem.Type_Collections) {
                    int tmp = (int) Math.ceil(offset / 20 + 0.0001);
                    posts = PostAPI.getGroupHotPostListFromMobileUrl(tmp);
                } else if (subItem.getType() == SubItem.Type_Private_Channel) {
                    int tmp = (int) Math.ceil(offset / 20 + 0.0001);
                    posts = PostAPI.getMyGroupRecentRepliesPosts(tmp);
                } else {
                    posts = PostAPI.getGroupPostListByJsonUrl(subItem.getValue(), offset);
                }
                resultObject.result = posts;
                if (posts != null) {
                    resultObject.ok = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResultObject o) {
            loadingView.setVisibility(View.GONE);
            if (!isCancelled()) {
                if (o.ok) {
                    ArrayList<Post> ars = (ArrayList<Post>) o.result;
                    if (offset > 0) {
                        //Load More
                        if (ars.size() > 0) {
                            adapter.addAll(ars);
                            adapter.notifyDataSetChanged();
                        } else {
                            //no data loaded
                        }
                    } else {
                        //Refresh
                        if (ars.size() > 0) {
                            adapter.setList(ars);
                            adapter.notifyDataSetInvalidated();
                        } else {
                            //no data loaded,不要清除了，保留旧数据得了
                        }
                    }
                } else {
                    //load error
                }
                if (adapter.getCount() > 0) {
                    listView.setCanPullToLoadMore(true);
                    listView.setCanPullToRefresh(true);
                } else {
                    listView.setCanPullToLoadMore(false);
                    listView.setCanPullToRefresh(true);
                }
                listView.doneOperation();
            }
        }
    }
}

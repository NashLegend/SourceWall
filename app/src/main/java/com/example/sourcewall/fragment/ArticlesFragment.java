package com.example.sourcewall.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.sourcewall.ArticleActivity;
import com.example.sourcewall.CommonView.LListView;
import com.example.sourcewall.CommonView.LoadingView;
import com.example.sourcewall.R;
import com.example.sourcewall.adapters.ArticleAdapter;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.ArticleAPI;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.model.SubItem;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.ToastUtil;
import com.example.sourcewall.view.ArticleListItemView;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class ArticlesFragment extends ChannelsFragment implements LListView.OnRefreshListener {

    private LListView listView;
    private ArticleAdapter adapter;
    private LoaderTask task;
    private SubItem subItem;
    private LoadingView loadingView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_articles, container, false);
        loadingView = (LoadingView) view.findViewById(R.id.article_progress_loading);
        subItem = (SubItem) getArguments().getSerializable(Consts.Extra_SubItem);
        listView = (LListView) view.findViewById(R.id.list_articles);
        adapter = new ArticleAdapter(getActivity());
        listView.setCanPullToRefresh(false);
        listView.setCanPullToLoadMore(false);
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), ArticleActivity.class);
                intent.putExtra(Consts.Extra_Article, ((ArticleListItemView) view).getArticle());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
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
            getActivity().setTitle("科学人");
        } else {
            getActivity().setTitle(this.subItem.getName() + " -- 科学人");
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStartRefresh() {
        loadData(0);
    }

    @Override
    public void onStartLoadMore() {
        loadData(adapter.getCount());
    }

    @Override
    public int getFragmentMenu() {
        return R.menu.menu_fragment_article;
    }

    @Override
    public void takeOverMenuInflate(MenuInflater inflater, Menu menu) {
        inflater.inflate(getFragmentMenu(), menu);
    }

    @Override
    public boolean takeOverOptionsItemSelect(MenuItem item) {
        int id = item.getItemId();
        return true;
    }

    @Override
    public void resetData(SubItem subItem) {
        if (subItem.equals(this.subItem)) {
            triggerRefresh();
        } else {
            this.subItem = subItem;
            adapter.clear();
            adapter.notifyDataSetInvalidated();
            listView.setCanPullToRefresh(false);
            listView.setCanPullToLoadMore(false);
            loadOver();
        }
        setTitle();
    }

    @Override
    public void triggerRefresh() {
        listView.startRefreshing();
    }

    private void cancelPotentialTask() {
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
            listView.doneOperation();
        }
    }

    class LoaderTask extends AsyncTask<Integer, Integer, ResultObject> {

        int offset;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(Integer... datas) {
            offset = datas[0];
            if (subItem.getType() == SubItem.Type_Collections) {
                return ArticleAPI.getArticleListIndexPage(offset);
            } else {
                return ArticleAPI.getArticleListByChannel(subItem.getValue(), offset);
            }
        }

        @Override
        protected void onPostExecute(ResultObject o) {
            loadingView.setVisibility(View.GONE);
            if (!isCancelled()) {
                if (o.ok) {
                    ArrayList<Article> ars = (ArrayList<Article>) o.result;
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
                    ToastUtil.toast("Load Error");
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

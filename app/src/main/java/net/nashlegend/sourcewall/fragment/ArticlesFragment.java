package net.nashlegend.sourcewall.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.ArticleActivity;
import net.nashlegend.sourcewall.adapters.ArticleAdapter;
import net.nashlegend.sourcewall.data.Consts.Extras;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.ArticleAPI;
import net.nashlegend.sourcewall.view.ArticleListItemView;
import net.nashlegend.sourcewall.view.common.LoadingView;
import net.nashlegend.sourcewall.view.common.LoadingView.ReloadListener;
import net.nashlegend.sourcewall.view.common.listview.LListView;
import net.nashlegend.sourcewall.view.common.listview.LListView.OnRefreshListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ArticlesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArticlesFragment extends BaseFragment implements ReloadListener, OnRefreshListener, OnItemClickListener {
    View layoutView;
    @BindView(R.id.list_articles)
    LListView listView;
    @BindView(R.id.articles_loading)
    ProgressBar progressBar;
    @BindView(R.id.article_progress_loading)
    LoadingView loadingView;

    private ArticleAdapter adapter;
    private SubItem subItem;

    public ArticlesFragment() {
        // Required empty public constructor
    }

    public static ArticlesFragment newInstance(SubItem subItem) {
        ArticlesFragment fragment = new ArticlesFragment();
        Bundle args = new Bundle();
        args.putParcelable(Extras.Extra_SubItem, subItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subItem = getArguments().getParcelable(Extras.Extra_SubItem);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.fragment_articles, container, false);
        ButterKnife.bind(this, layoutView);
        loadingView.setReloadListener(this);
        subItem = getArguments().getParcelable(Extras.Extra_SubItem);
        adapter = new ArticleAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(this);
        loadOver();
        return layoutView;
    }

    private void loadOver() {
        loadData(0);
        loadingView.startLoading();
    }

    private void loadData(int offset) {
        loadArticles(offset);
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

    public void scrollToHead() {
        listView.setSelection(0);
    }

    @Override
    public void reload() {
        loadData(0);
    }

    private void loadArticles(final int offset) {
        Observable<ResponseObject<ArrayList<Article>>> observable =
                ArticleAPI.getArticleList(subItem.getType(), subItem.getValue(), offset, offset == 0 && adapter.getList().size() == 0);
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        loadingView.onLoadSuccess();
                        if (adapter.getCount() > 0) {
                            listView.setCanPullToLoadMore(true);
                            listView.setCanPullToRefresh(true);
                        } else {
                            listView.setCanPullToLoadMore(false);
                            listView.setCanPullToRefresh(true);
                        }
                        listView.doneOperation();
                    }
                })
                .subscribe(new Observer<ResponseObject<ArrayList<Article>>>() {
                    @Override
                    public void onCompleted() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(ResponseObject<ArrayList<Article>> result) {
                        if (result.isCached && offset == 0) {
                            if (result.ok) {
                                ArrayList<Article> ars = result.result;
                                if (ars.size() > 0) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    loadingView.onLoadSuccess();
                                    adapter.addAll(ars);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            listView.doneOperation();
                            progressBar.setVisibility(View.GONE);
                            if (result.ok) {
                                loadingView.onLoadSuccess();
                                ArrayList<Article> ars = result.result;
                                if (offset > 0) {
                                    if (ars.size() > 0) {
                                        adapter.addAll(ars);
                                        adapter.notifyDataSetChanged();
                                    }
                                } else {
                                    if (ars.size() > 0) {
                                        adapter.setList(ars);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            } else {
                                toastSingleton(R.string.load_failed);
                                loadingView.onLoadFailed();
                            }
                            if (adapter.getCount() > 0) {
                                listView.setCanPullToLoadMore(true);
                                listView.setCanPullToRefresh(true);
                            } else {
                                listView.setCanPullToLoadMore(false);
                                listView.setCanPullToRefresh(true);
                            }
                        }
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view instanceof ArticleListItemView) {
            Intent intent = new Intent();
            intent.setClass(getContext(), ArticleActivity.class);
            intent.putExtra(Extras.Extra_Article, ((ArticleListItemView) view).getData());
            startOneActivity(intent);
        }
    }

    @Override
    public boolean reTap() {
        if (listView == null) {
            return false;
        }
        if (listView.getFirstVisiblePosition() <= 1 && listView.getChildAt(0).getY() == 0) {
            listView.startRefreshing();
        } else {
            listView.smoothScrollByOffset(-Integer.MAX_VALUE);
        }
        return true;
    }
}

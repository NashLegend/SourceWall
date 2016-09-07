package net.nashlegend.sourcewall.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.FavorAdapter;
import net.nashlegend.sourcewall.model.Favor;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.FavorAPI;
import net.nashlegend.sourcewall.data.Consts.Extras;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.util.UrlCheckUtil;
import net.nashlegend.sourcewall.view.FavorListItemView;
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
 * Created by NashLegend on 2014/9/18 0018
 */
public class FavorsFragment extends BaseFragment implements OnRefreshListener, ReloadListener, OnItemClickListener {

    View layoutView;
    @BindView(R.id.list_favors)
    LListView listView;
    @BindView(R.id.favors_loading)
    ProgressBar progressBar;
    @BindView(R.id.favor_progress_loading)
    LoadingView loadingView;

    private FavorAdapter adapter;
    private SubItem subItem;

    public static FavorsFragment newInstance(SubItem subItem) {
        FavorsFragment fragment = new FavorsFragment();
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
        if (layoutView == null) {
            layoutView = inflater.inflate(R.layout.fragment_favors, container, false);
            ButterKnife.bind(this, layoutView);
            loadingView.setReloadListener(this);
            subItem = getArguments().getParcelable(Extras.Extra_SubItem);
            adapter = new FavorAdapter(getActivity());
            listView.setAdapter(adapter);
            listView.setOnRefreshListener(this);
            listView.setOnItemClickListener(this);
            loadOver();
        } else {
            if (layoutView.getParent() != null) {
                ((ViewGroup) layoutView.getParent()).removeView(layoutView);
            }
            SubItem mSubItem = getArguments().getParcelable(Extras.Extra_SubItem);
            resetData(mSubItem);
        }
        return layoutView;
    }

    private void loadOver() {
        loadData(0);
        loadingView.startLoading();
    }

    private void loadData(int offset) {
        loadFavors(offset);
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

    public void resetData(SubItem subItem) {
        if (subItem.equals(this.subItem)) {
            loadingView.onLoadSuccess();
            if (adapter == null || adapter.getCount() == 0) {
                triggerRefresh();
            }
        } else {
            this.subItem = subItem;
            adapter.clear();
            adapter.notifyDataSetInvalidated();
            listView.setCanPullToRefresh(false);
            listView.setCanPullToLoadMore(false);
            loadOver();
        }
    }

    public void triggerRefresh() {
        listView.startRefreshing();
    }

    @Override
    public void reload() {
        loadData(0);
    }

    private void loadFavors(final int offset) {
        Observable<ResponseObject<ArrayList<Favor>>> observable =
                FavorAPI.getFavorList(subItem.getValue(), offset, offset == 0 && adapter.getList().size() == 0);
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
                .subscribe(new Observer<ResponseObject<ArrayList<Favor>>>() {
                    @Override
                    public void onCompleted() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseObject<ArrayList<Favor>> result) {
                        if (result.isCached && offset == 0) {
                            if (result.ok) {
                                ArrayList<Favor> ars = result.result;
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
                                ArrayList<Favor> ars = result.result;
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
                                toast(R.string.load_failed);
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
        if (UiUtil.shouldThrottle()) {
            return;
        }
        if (view instanceof FavorListItemView) {
            UrlCheckUtil.redirectRequest(((FavorListItemView) view).getData().getUrl());
        }
    }
}

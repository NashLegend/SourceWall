package net.nashlegend.sourcewall.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.QuestionActivity;
import net.nashlegend.sourcewall.adapters.QuestionAdapter;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.QuestionAPI;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.view.QuestionListItemView;
import net.nashlegend.sourcewall.view.common.LListView;
import net.nashlegend.sourcewall.view.common.LoadingView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Questions2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Questions2Fragment extends BaseFragment implements LoadingView.ReloadListener, LListView.OnRefreshListener, AdapterView.OnItemClickListener {
    View layoutView;
    private final String HOTTEST = "hottest";
    private final String HIGHLIGHT = "highlight";

    @BindView(R.id.list_questions)
    LListView listView;
    @BindView(R.id.questions_loading)
    ProgressBar progressBar;
    @BindView(R.id.question_progress_loading)
    LoadingView loadingView;

    private QuestionAdapter adapter;
    private SubItem subItem;
    private int currentPage = -1;//page从0开始，-1表示还没有数据
    private View headerView;

    public Questions2Fragment() {
        // Required empty public constructor
    }

    public static Questions2Fragment newInstance(SubItem subItem) {
        Questions2Fragment fragment = new Questions2Fragment();
        Bundle args = new Bundle();
        args.putParcelable(Consts.Extra_SubItem, subItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subItem = getArguments().getParcelable(Consts.Extra_SubItem);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.fragment_questions2, container, false);
        ButterKnife.bind(this, layoutView);
        loadingView.setReloadListener(this);
        subItem = getArguments().getParcelable(Consts.Extra_SubItem);
        headerView = inflater.inflate(R.layout.layout_header_load_pre_page, null, false);
        adapter = new QuestionAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(this);
        listView.addHeaderView(headerView);
        headerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (headerView.getLayoutParams() != null) {
                    headerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    hideHeader();
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

        loadOver();

        return layoutView;
    }

    private void loadOver() {
        loadData(0);
        loadingView.startLoading();
    }

    private void loadData(int offset) {
        if (offset < 0) {
            offset = 0;
        }
        try {
            loadQuestions(offset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void loadPrePage() {
        listView.setCanPullToLoadMore(false);
        listView.setCanPullToRefresh(false);
        headerView.findViewById(R.id.text_header_load_hint).setVisibility(View.INVISIBLE);
        headerView.findViewById(R.id.progress_header_loading).setVisibility(View.VISIBLE);
        loadData(currentPage - 1);
    }

    /**
     * 有可能LoaderTask已经结束但是Header仍然没有layoutParams，下同
     * 不过不用担心，只会出现在刚刚进入的时候，这时不设置Height,getViewTreeObserver也会很快将其隐藏的
     */
    private void hideHeader() {
        if (headerView.getLayoutParams() != null) {
            headerView.getLayoutParams().height = 1;
        }
        headerView.setVisibility(View.GONE);
    }

    private void showHeader() {
        if (headerView.getLayoutParams() != null) {
            headerView.getLayoutParams().height = 0;
        }
        headerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStartRefresh() {
        hideHeader();
        loadData(0);
    }

    @Override
    public void onStartLoadMore() {
        loadData(currentPage + 1);
    }

    public void scrollToHead() {
        listView.setSelection(0);
    }

    @Override
    public void reload() {
        loadData(0);
    }

    private void loadQuestions(final int loadedPage) throws UnsupportedEncodingException {
        boolean useCache = loadedPage == 0 && adapter.getList().size() == 0;
        Observable<ResponseObject<ArrayList<Question>>> observable;
        if (subItem.getType() == SubItem.Type_Collections) {
            if (HOTTEST.equals(subItem.getValue())) {
                observable = QuestionAPI.getHotQuestions(loadedPage * 20, useCache);
            } else {
                observable = QuestionAPI.getHighlightQuestions(loadedPage + 1, useCache);
            }
        } else {
            observable = QuestionAPI.getQuestionsByTag(subItem.getValue(), loadedPage * 20, useCache);
        }
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        listView.doneOperation();
                        loadingView.onLoadSuccess();
                        if (currentPage > 0) {
                            showHeader();
                        } else {
                            hideHeader();
                        }
                        if (adapter.getCount() > 0) {
                            listView.setCanPullToLoadMore(true);
                            listView.setCanPullToRefresh(true);
                        } else {
                            listView.setCanPullToLoadMore(false);
                            listView.setCanPullToRefresh(true);
                        }
                    }
                })
                .subscribe(new Observer<ResponseObject<ArrayList<Question>>>() {
                    @Override
                    public void onCompleted() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseObject<ArrayList<Question>> result) {
                        if (result.isCached && loadedPage == 0) {
                            if (result.ok) {
                                ArrayList<Question> ars = result.result;
                                if (ars.size() > 0) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    loadingView.onLoadSuccess();
                                    adapter.setList(ars);
                                    adapter.notifyDataSetInvalidated();
                                }
                            }
                        } else {
                            listView.doneOperation();
                            progressBar.setVisibility(View.GONE);
                            if (result.ok) {
                                loadingView.onLoadSuccess();
                                ArrayList<Question> ars = result.result;
                                if (ars.size() > 0) {
                                    currentPage = loadedPage;
                                    adapter.setList(ars);
                                    adapter.notifyDataSetChanged();
                                    listView.setSelection(0);
                                } else {
                                    toast("没有加载到数据");
                                }
                            } else {
                                toast(R.string.load_failed);
                                loadingView.onLoadFailed();
                            }
                            if (currentPage > 0) {
                                showHeader();
                            } else {
                                hideHeader();
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
                        }
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (UiUtil.shouldThrottle()) {
            return;
        }
        if (view instanceof QuestionListItemView) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), QuestionActivity.class);
            intent.putExtra(Consts.Extra_Question, ((QuestionListItemView) view).getData());
            startActivity(intent);
        }
    }

}

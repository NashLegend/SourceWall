package net.nashlegend.sourcewall.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.AceAdapter;
import net.nashlegend.sourcewall.model.Answer;
import net.nashlegend.sourcewall.model.SearchItem;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.RequestObject.SimpleCallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.SearchAPI;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.util.UrlCheckUtil;
import net.nashlegend.sourcewall.view.AceView;
import net.nashlegend.sourcewall.view.AnswerListItemView;
import net.nashlegend.sourcewall.view.common.LoadingView;
import net.nashlegend.sourcewall.view.common.listview.LListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends BaseActivity {

    @BindView(R.id.btn_search_all)
    TextView btnSearchAll;
    @BindView(R.id.btn_search_article)
    TextView btnSearchArticle;
    @BindView(R.id.btn_search_post)
    TextView btnSearchPost;
    @BindView(R.id.btn_search_question)
    TextView btnSearchQuestion;
    @BindView(R.id.btn_search_blog)
    TextView btnSearchBlog;
    @BindView(R.id.text_search)
    EditText searchText;
    @BindView(R.id.list_search)
    LListView listSearch;
    @BindView(R.id.view_loading)
    LoadingView viewLoading;
    @BindView(R.id.activity_search)
    LinearLayout activitySearch;

    SearchAdapter adapter;
    String crtType = SearchAPI.TYPE_ALL;
    int crtPage = 0;
    NetworkTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        adapter = new SearchAdapter(this);
        listSearch.setAdapter(adapter);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    search(crtType, 1);
                    UiUtil.hideIME(SearchActivity.this);
                    return true;
                }
                return false;
            }
        });
        viewLoading.setReloadListener(new LoadingView.ReloadListener() {
            @Override
            public void reload() {
                search(crtType, 1);
            }
        });
        listSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (view instanceof SearchItemView) {
                    SearchItem item = ((SearchItemView) view).getData();
                    UrlCheckUtil.redirectRequest(item.getUrl());
                }
            }
        });
        listSearch.setOnRefreshListener(new LListView.OnRefreshListener() {
            @Override
            public void onStartRefresh() {
                search(crtType, 1);
            }

            @Override
            public void onStartLoadMore() {
                search(crtType, crtPage + 1);
            }
        });
        btnSearchAll.performClick();
    }

    @OnClick({R.id.btn_search_all, R.id.btn_search_article, R.id.btn_search_post, R.id.btn_search_question, R.id.btn_search_blog})
    public void onClick(View view) {
        if (view.isSelected()) {
            return;
        }
        crtPage = 0;
        switch (view.getId()) {
            case R.id.btn_search_all:
                crtType = SearchAPI.TYPE_ALL;
                break;
            case R.id.btn_search_article:
                crtType = SearchAPI.TYPE_ARTICLE;
                break;
            case R.id.btn_search_post:
                crtType = SearchAPI.TYPE_POST;
                break;
            case R.id.btn_search_question:
                crtType = SearchAPI.TYPE_QUESTION;
                break;
            case R.id.btn_search_blog:
                crtType = SearchAPI.TYPE_BLOG;
                break;
        }
        search(crtType, 1);
        btnSearchAll.setSelected(false);
        btnSearchArticle.setSelected(false);
        btnSearchPost.setSelected(false);
        btnSearchQuestion.setSelected(false);
        btnSearchBlog.setSelected(false);
        view.setSelected(true);
    }

    private void search(String type, final int page) {
        if (searchText.getText().toString().trim().length() == 0) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        if (page == 1) {
            viewLoading.startLoading();
            adapter.clear();
            adapter.notifyDataSetChanged();
        }
        task = SearchAPI.getSearchedItems(type, page, searchText.getText().toString().trim(), new SimpleCallBack<ArrayList<SearchItem>>() {
            @Override
            public void onFailure() {
                if (page == 1) {
                    viewLoading.onLoadFailed();
                }
                toast("加载失败");
                listSearch.doneOperation();
            }

            @Override
            public void onSuccess(@NonNull ArrayList<SearchItem> result) {
                if (isFinishing()) {
                    return;
                }
                if (page == 1) {
                    viewLoading.onLoadSuccess();
                    if (result.size() == 0) {
                        toast("没有搜索到结果");
                    }
                } else {
                    if (result.size() == 0) {
                        toast("下面没有了");
                    }
                }
                listSearch.doneOperation();
                listSearch.setCanPullToLoadMore(true);
                if (page == 1) {
                    adapter.setList(result);
                } else {
                    adapter.addAll(result);
                }
                adapter.notifyDataSetChanged();
                if (result.size() > 0) {
                    crtPage = page;
                }
            }
        });
    }

    class SearchAdapter extends AceAdapter<SearchItem> {

        public SearchAdapter(Context context) {
            super(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new SearchItemView(getContext());
            }
            ((SearchItemView) convertView).setData(list.get(position));
            return convertView;
        }
    }

    class SearchItemView extends AceView<SearchItem> {

        TextView title;
        TextView summary;
        TextView from;
        TextView date;

        SearchItem searchItem;

        public SearchItemView(Context context) {
            super(context);
            initView();
        }

        public SearchItemView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView();
        }

        public SearchItemView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initView();
        }

        private void initView() {
            View.inflate(getContext(), R.layout.layout_search_item, this);
            title = (TextView) findViewById(R.id.text_title);
            summary = (TextView) findViewById(R.id.text_content);
            from = (TextView) findViewById(R.id.text_from);
            date = (TextView) findViewById(R.id.text_date);
        }

        @Override
        public void setData(SearchItem data) {
            searchItem = data;
            title.setText(searchItem.getTitle());
            if (TextUtils.isEmpty(searchItem.getSummary())) {
                summary.setVisibility(GONE);
            } else {
                summary.setVisibility(VISIBLE);
            }
            summary.setText(searchItem.getSummary());
            from.setText(searchItem.getFrom());
            date.setText(searchItem.getDatetime());
        }

        @Override
        public SearchItem getData() {
            return searchItem;
        }
    }
}

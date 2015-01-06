package com.example.sourcewall.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;

import com.example.sourcewall.QuestionActivity;
import com.example.sourcewall.R;
import com.example.sourcewall.adapters.QuestionAdapter;
import com.example.sourcewall.commonview.LListView;
import com.example.sourcewall.commonview.LoadingView;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.QuestionAPI;
import com.example.sourcewall.model.Question;
import com.example.sourcewall.model.SubItem;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.ToastUtil;
import com.example.sourcewall.view.QuestionFeaturedListItemView;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class QuestionsFragment extends ChannelsFragment implements LListView.OnRefreshListener {
    private final String HOTTEST = "hottest";
    private final String HIGHLIGHT = "highlight";
    private LListView listView;
    private QuestionAdapter adapter;
    private LoaderTask task;
    private SubItem subItem;
    private LoadingView loadingView;
    private int currentPage = -1;//page从0开始，-1表示还没有数据
    private View headerView;

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questions, container, false);
        loadingView = (LoadingView) view.findViewById(R.id.question_progress_loading);
        subItem = (SubItem) getArguments().getSerializable(Consts.Extra_SubItem);
        headerView = inflater.inflate(R.layout.layout_header_load_pre_page, null, false);
        listView = (LListView) view.findViewById(R.id.list_questions);
        adapter = new QuestionAdapter(getActivity());
        listView.setCanPullToRefresh(false);
        listView.setCanPullToLoadMore(false);
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), QuestionActivity.class);
                intent.putExtra(Consts.Extra_Question, ((QuestionFeaturedListItemView) view).getData());
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
        getActivity().setTitle(this.subItem.getName() + " -- 问答");
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

    private void loadPrePage() {
        listView.setCanPullToLoadMore(false);
        listView.setCanPullToRefresh(false);
        headerView.findViewById(R.id.text_header_load_hint).setVisibility(View.INVISIBLE);
        headerView.findViewById(R.id.progress_header_loading).setVisibility(View.VISIBLE);
        loadData(currentPage - 1);
    }

    @Override
    public void resetData(SubItem subItem) {
        if (subItem.equals(this.subItem)) {
            triggerRefresh();
        } else {
            currentPage = -1;
            this.subItem = subItem;
            setTitle();
            adapter.clear();
            adapter.notifyDataSetInvalidated();
            listView.setCanPullToRefresh(false);
            listView.setCanPullToLoadMore(false);
            headerView.getLayoutParams().height = 1;
            headerView.setVisibility(View.GONE);
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
        }
    }

    class LoaderTask extends AsyncTask<Integer, Integer, ResultObject> {

        int loadedPage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(Integer... datas) {
            loadedPage = datas[0];
            ArrayList<Question> questions = new ArrayList<Question>();
            ResultObject resultObject = new ResultObject();
            try {
                if (subItem.getType() == SubItem.Type_Collections) {
                    if (HOTTEST.equals(subItem.getValue())) {
                        questions = QuestionAPI.getHotQuestions(loadedPage + 1);
                    } else {
                        questions = QuestionAPI.getHighlightQuestions(loadedPage + 1);
                    }
                } else {
                    questions = QuestionAPI.getQuestionsByTagFromJsonUrl(URLEncoder.encode(subItem.getValue(), "UTF-8"), loadedPage * 20);
                }
                resultObject.result = questions;
                if (questions != null) {
                    resultObject.ok = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResultObject o) {
            loadingView.setVisibility(View.GONE);
            if (!isCancelled()) {
                if (o.ok) {
                    ArrayList<Question> ars = (ArrayList<Question>) o.result;
                    if (ars.size() > 0) {
                        currentPage = loadedPage;
                        adapter.setList(ars);
                        adapter.notifyDataSetInvalidated();
                        // listView.smoothScrollToPosition(0);
                        listView.scrollTo(0, 0);
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

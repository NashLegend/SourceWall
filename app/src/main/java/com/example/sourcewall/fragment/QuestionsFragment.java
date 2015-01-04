package com.example.sourcewall.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questions, container, false);
        loadingView = (LoadingView) view.findViewById(R.id.question_progress_loading);
        subItem = (SubItem) getArguments().getSerializable(Consts.Extra_SubItem);
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
            ArrayList<Question> questions = new ArrayList<Question>();
            ResultObject resultObject = new ResultObject();
            try {
                if (subItem.getType() == SubItem.Type_Collections) {
                    int tmp = (int) Math.ceil(offset / 20 + 0.0001);
                    if (HOTTEST.equals(subItem.getValue())) {
                        questions = QuestionAPI.getHotQuestions(tmp);
                    } else {
                        questions = QuestionAPI.getHighlightQuestions(tmp);
                    }
                } else {
                    questions = QuestionAPI.getQuestionsByTagFromJsonUrl(URLEncoder.encode(subItem.getValue(), "UTF-8"), offset);
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
                    // load error
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

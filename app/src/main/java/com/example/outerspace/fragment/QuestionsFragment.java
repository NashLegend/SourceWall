package com.example.outerspace.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.outerspace.PostActivity;
import com.example.outerspace.R;
import com.example.outerspace.adapters.QuestionAdapter;
import com.example.outerspace.commonview.LListView;
import com.example.outerspace.connection.ResultObject;
import com.example.outerspace.connection.api.QuestionAPI;
import com.example.outerspace.model.Question;
import com.example.outerspace.util.Consts;
import com.example.outerspace.view.QuestionFeaturedListItemView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class QuestionsFragment extends BaseFragment implements LListView.OnRefreshListener {
    boolean isChannel = false;
    private final String HOTTEST = "hottest";
    private final String HIGHLIGHT = "highlight";
    LListView listView;
    QuestionAdapter adapter;
    ChannelBoardFragment channelBoard;
    LoaderTask task;

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questions, container, false);
        listView = (LListView) view.findViewById(R.id.list_questions);
        adapter = new QuestionAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), PostActivity.class);
                intent.putExtra(Consts.Extra_Post, ((QuestionFeaturedListItemView) view).getQuestion());
                startActivity(intent);
            }
        });
        task = new LoaderTask();
        RequestData requestData = new RequestData();
        requestData.isByTag = false;
        requestData.isLoadMore = false;
        requestData.offset = 1;
        requestData.tag = HOTTEST;
        task.execute(requestData);
        return view;
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {

    }

    class LoaderTask extends AsyncTask<RequestData, Integer, ResultObject> {

        RequestData data;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(RequestData... datas) {
            data = datas[0];
            ArrayList<Question> questions = new ArrayList<Question>();
            ResultObject resultObject = new ResultObject();
            try {
                if (data.isByTag) {
                    questions = QuestionAPI.getQuestionsByTagFromJsonUrl(data.tag, data.offset);
                } else {
                    if (HOTTEST.equals(data.tag)) {
                        questions = QuestionAPI.getHotQuestions(data.offset);
                    } else {
                        questions = QuestionAPI.getHighlightQuestions(data.offset);
                    }
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
            if (o.ok) {
                ArrayList<Question> ars = (ArrayList<Question>) o.result;
                if (data.isLoadMore) {
                    if (ars.size() > 0) {
                        adapter.addAll(ars);
                    } else {

                    }
                } else {
                    if (ars.size() > 0) {
                        adapter.setList(ars);
                    } else {

                    }
                }
                adapter.notifyDataSetChanged();
            } else {

            }
        }
    }

    class RequestData {
        boolean isLoadMore = false;
        boolean isByTag = false;
        String tag = "";
        int offset = 0;
    }
}

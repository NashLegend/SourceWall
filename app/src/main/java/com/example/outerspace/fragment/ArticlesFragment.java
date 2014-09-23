package com.example.outerspace.fragment;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.outerspace.R;
import com.example.outerspace.adapters.ArticleAdapter;
import com.example.outerspace.connection.ResultObject;
import com.example.outerspace.connection.api.ArticleAPI;
import com.example.outerspace.model.Article;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/18 0018.
 */
public class ArticlesFragment extends BaseFragment {

    String defaultChannel = "hottest";
    boolean isChannel = true;
    ListView listView;
    ArticleAdapter adapter;
    ChannelBoardFragment channelBoard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_articles, container, false);
        listView = (ListView) view.findViewById(R.id.list_articles);
        adapter = new ArticleAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        return view;
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
            ArrayList<Article> articles = new ArrayList<Article>();
            ResultObject resultObject = new ResultObject();
            try {
                if (data.isChannel) {
                    articles = ArticleAPI.getArticleListByChannel(data.key, data.offset);
                } else {
                    articles = ArticleAPI.getArticleListBySubject(data.key, data.offset);
                }
                resultObject.result = articles;
                if (articles != null) {
                    resultObject.ok = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResultObject o) {
            if (o.ok) {
                ArrayList<Article> ars = (ArrayList<Article>) o.result;
                if (data.isLoadMore) {
                    if (ars.size() > 0) {
                        adapter.setList(ars);
                    } else {

                    }
                } else {
                    if (ars.size() > 0) {
                        adapter.addAll(ars);
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
        boolean isChannel = true;
        String key = "";
        int offset = 0;
    }


}

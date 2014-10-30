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
import com.example.outerspace.adapters.PostAdapter;
import com.example.outerspace.commonview.LListView;
import com.example.outerspace.connection.ResultObject;
import com.example.outerspace.connection.api.PostAPI;
import com.example.outerspace.model.Post;
import com.example.outerspace.util.Consts;
import com.example.outerspace.view.PostListItemView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class PostsFragment extends BaseFragment implements LListView.OnRefreshListener {
    boolean isChannel = false;
    LListView listView;
    PostAdapter adapter;
    ChannelBoardFragment channelBoard;
    LoaderTask task;

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        listView = (LListView) view.findViewById(R.id.list_posts);
        adapter = new PostAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), PostActivity.class);
                intent.putExtra(Consts.Extra_Post, ((PostListItemView) view).getPost());
                startActivity(intent);
            }
        });
        task = new LoaderTask();
        RequestData requestData = new RequestData();
        requestData.isChannel = false;
        requestData.isLoadMore = false;
        requestData.offset = 1;
        task.execute(requestData);
        return view;
    }

    @Override
    public void onRefresh() {
        //TODO
    }

    @Override
    public void onLoadMore() {
        //TODO
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
            ArrayList<Post> posts = new ArrayList<Post>();
            ResultObject resultObject = new ResultObject();
            try {
                if (data.isChannel) {
                    posts = PostAPI.getGroupPostListByJsonUrl(data.key, data.offset);
                } else {
                    posts = PostAPI.getGroupHotPostListFromMobileUrl(data.offset);
                }
                resultObject.result = posts;
                if (posts != null) {
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
                ArrayList<Post> ars = (ArrayList<Post>) o.result;
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
        boolean isChannel = true;
        String key = "";
        int offset = 0;
    }
}

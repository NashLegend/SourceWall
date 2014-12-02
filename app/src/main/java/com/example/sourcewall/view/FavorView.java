package com.example.sourcewall.view;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.example.sourcewall.R;
import com.example.sourcewall.adapters.FavorAdapter;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.UserAPI;
import com.example.sourcewall.model.Basket;
import com.example.sourcewall.util.ToastUtil;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class FavorView extends FrameLayout implements View.OnClickListener {

    ListView listView;
    final FavorAdapter adapter;
    Button btn_create;
    Button btn_ok;
    ViewGroup listLayout;
    ViewGroup editLayout;

    public FavorView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dialog_favor, this);
        listLayout = (ViewGroup) findViewById(R.id.layout_favor_dialog_list);
        editLayout = (ViewGroup) findViewById(R.id.layout_favor_dialog_edit);
        btn_create = (Button) findViewById(R.id.button_favor_dialog_create);
        btn_ok = (Button) findViewById(R.id.button_favor_dialog_ok);
        listView = (ListView) findViewById(R.id.list_favor_dialog);
        adapter = new FavorAdapter(context);
        listView.setAdapter(adapter);
        btn_create.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        loadBasket();
    }

    private void loadBasket() {
        LoadBasketTask task = new LoadBasketTask();
        task.execute();
    }

    private void createBasket(String title, String introduction, String category_id) {
        CreateBasketTask task = new CreateBasketTask();
        task.execute(title, introduction, category_id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_favor_dialog_create:
                break;
            case R.id.button_favor_dialog_ok:
                break;
            case R.id.button_cancel_create_basket:
                break;
            case R.id.button_create_basket:

                break;
        }
    }

    class LoadBasketTask extends AsyncTask<Void, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(Void... params) {
            return UserAPI.getBaskets();
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                ArrayList<Basket> baskets = (ArrayList<Basket>) resultObject.result;
                adapter.clear();
                if (baskets != null && baskets.size() > 0) {
                    adapter.setList(baskets);
                }
                adapter.notifyDataSetChanged();
            } else {
                //TODO fetch failed
                ToastUtil.toast("Load Basket Failed");
            }
        }
    }

    class CreateBasketTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(String... params) {
            String title = params[0];
            String introduction = params[1];
            String categoryID = params[2];
            return UserAPI.createBasket(title, introduction, categoryID);
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                Basket basket = (Basket) resultObject.result;
                if (basket != null) {
                    adapter.add(basket);
                    adapter.notifyDataSetChanged();
                }
            } else {
                ToastUtil.toast("Create Failed");
                //TODO create failed
            }
        }
    }
}

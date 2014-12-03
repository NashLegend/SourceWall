package com.example.sourcewall.view;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.adapters.FavorAdapter;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.UserAPI;
import com.example.sourcewall.model.AceModel;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.model.Basket;
import com.example.sourcewall.model.Category;
import com.example.sourcewall.model.Post;
import com.example.sourcewall.util.ToastUtil;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class FavorView extends FrameLayout implements View.OnClickListener {

    ListView listView;
    FavorAdapter adapter;
    Button btn_invoke_create;
    Button btn_ok;
    Button btn_create_basket;
    Button btn_cancel_create_basket;
    Spinner spinner;
    ViewGroup listLayout;
    ViewGroup editLayout;
    TextView nameText;
    TextView introText;

    public FavorView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dialog_favor, this);
        nameText = (TextView) findViewById(R.id.text_create_basket_name);
        introText = (TextView) findViewById(R.id.text_create_basket_introduction);
        listLayout = (ViewGroup) findViewById(R.id.layout_favor_dialog_list);
        editLayout = (ViewGroup) findViewById(R.id.layout_favor_dialog_edit);
        btn_invoke_create = (Button) findViewById(R.id.button_favor_dialog_invoke_create);
        btn_ok = (Button) findViewById(R.id.button_favor_dialog_ok);
        btn_cancel_create_basket = (Button) findViewById(R.id.button_cancel_create_basket);
        btn_create_basket = (Button) findViewById(R.id.button_create_basket);
        spinner = (Spinner) findViewById(R.id.spinner_categories);
        listView = (ListView) findViewById(R.id.list_favor_dialog);
    }

    public void setData(AceModel model) {
        String link = "";
        String title = "";
        if (model instanceof Article) {
            link = "http://www.guokr.com/article/" + ((Article) model).getId() + "/";
            title = ((Article) model).getTitle();
        } else if (model instanceof Post) {
            link = "http://www.guokr.com/post/" + ((Post) model).getId() + "/";
            title = ((Post) model).getTitle();
        } else {
            return;
        }
        adapter = new FavorAdapter(getContext(), link, title);
        listView.setAdapter(adapter);
        btn_invoke_create.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        btn_cancel_create_basket.setOnClickListener(this);
        btn_create_basket.setOnClickListener(this);
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

    private void loadCategories() {
        LoadCategoryTask task = new LoadCategoryTask();
        task.execute();
    }

    private void openCreateBasketView() {
        //TODO show view
        listLayout.setVisibility(GONE);
        editLayout.setVisibility(VISIBLE);
        loadCategories();
    }

    private void openBasketListView() {
        listLayout.setVisibility(VISIBLE);
        editLayout.setVisibility(GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_favor_dialog_invoke_create:
                openCreateBasketView();
                break;
            case R.id.button_favor_dialog_ok:
                break;
            case R.id.button_cancel_create_basket:
                openBasketListView();
                break;
            case R.id.button_create_basket:
                createBasket(nameText.getText().toString(), introText.getText().toString(), "-1");
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

    class LoadCategoryTask extends AsyncTask<Void, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(Void... params) {
            return UserAPI.getCategoryList();
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                ToastUtil.toast("Load Category OK");
                ArrayList<Category> categories = (ArrayList<Category>) resultObject.result;
                //TODO
            } else {
                //TODO fetch failed
                ToastUtil.toast("Load Category Failed");
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
                ToastUtil.toast("Create OK");
                Basket basket = (Basket) resultObject.result;
                adapter.add(basket);
                adapter.notifyDataSetChanged();
                openBasketListView();
            } else {
                ToastUtil.toast("Create Failed");
                //TODO create failed
            }
        }
    }
}

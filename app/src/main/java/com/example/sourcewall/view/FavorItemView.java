package com.example.sourcewall.view;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.UserAPI;
import com.example.sourcewall.model.Basket;
import com.example.sourcewall.util.ToastUtil;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class FavorItemView extends AceView<Basket> implements View.OnClickListener {
    TextView textView;
    View button;
    Basket basket;
    String link = "";
    String title = "";

    public FavorItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_favor_item_view, this);
        textView = (TextView) findViewById(R.id.text_basket_name);
        button = findViewById(R.id.button_add_2_favor);
        button.setOnClickListener(this);
    }

    public FavorItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FavorItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setData(Basket basket, String link, String title) {
        this.link = link;
        this.title = title;
        setData(basket);
    }

    @Override
    public void setData(Basket basket) {
        this.basket = basket;
        textView.setText(basket.getName());
        if (basket.isHasFavored()) {
            button.setEnabled(false);
            button.setVisibility(View.INVISIBLE);
        } else {
            if (basket.isFavoring()) {
                button.setEnabled(false);
            } else {
                button.setEnabled(true);
                button.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        FavorTask task = new FavorTask();
        task.execute(link, title, basket.getId());
    }

    class FavorTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            basket.setFavoring(true);
            button.setEnabled(false);
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            String link = params[0];
            String title = params[1];
            String basketID = params[2];
            return UserAPI.favorLink(link, title, basketID);
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            basket.setFavoring(false);
            if (resultObject.ok) {
                ToastUtil.toast("Favor OK");
                basket.setHasFavored(true);
                button.setVisibility(View.INVISIBLE);
            } else {
                ToastUtil.toast("Favor Failed");
                button.setEnabled(false);
            }
        }
    }
}

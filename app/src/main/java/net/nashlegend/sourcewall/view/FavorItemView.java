package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.connection.ResultObject;
import net.nashlegend.sourcewall.connection.api.UserAPI;
import net.nashlegend.sourcewall.model.Basket;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class FavorItemView extends AceView<Basket> implements View.OnClickListener {
    TextView textView;
    ImageButton button;
    Basket basket;
    String link = "";
    String title = "";

    public FavorItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_favor_item_view, this);
        textView = (TextView) findViewById(R.id.text_basket_name);
        button = (ImageButton) findViewById(R.id.button_add_2_favor);
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
            button.setImageResource(R.drawable.check_36dp);
        } else {
            if (basket.isFavoring()) {
                button.setEnabled(false);
            } else {
                button.setEnabled(true);
                button.setImageResource(R.drawable.plus);
            }
        }
    }

    @Override
    public Basket getData() {
        return basket;
    }

    @Override
    public void onClick(View v) {
        FavorTask task = new FavorTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, link, title, basket.getId());
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
                basket.setHasFavored(true);
                button.setImageResource(R.drawable.check_36dp);
            } else {
                button.setEnabled(true);
            }
        }
    }
}

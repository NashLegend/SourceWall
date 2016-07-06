package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.FavorAPI;

import de.greenrobot.event.EventBus;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class BasketItemView extends AceView<Basket> implements View.OnClickListener {
    TextView textView;
    ImageButton button;
    ProgressBar progressBar;
    Basket basket;
    String link = "";
    String title = "";
    NetworkTask<Boolean> networkTask;

    public BasketItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_basket_item_view, this);
        textView = (TextView) findViewById(R.id.text_basket_name);
        button = (ImageButton) findViewById(R.id.button_add_2_favor);
        progressBar = (ProgressBar) findViewById(R.id.progress_adding_favor);
        button.setOnClickListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(Basket basket) {
        if (equalBasket(basket)) {
            setData(basket);
        }
    }

    public boolean equalBasket(Basket bas) {
        return bas != null && this.basket != null
                && this.basket.getId().equals(bas.getId());
    }

    public BasketItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BasketItemView(Context context, AttributeSet attrs, int defStyle) {
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
            button.setImageResource(R.drawable.check_24dp);
            button.setEnabled(false);
            button.setVisibility(VISIBLE);
            progressBar.setVisibility(GONE);
        } else {
            if (basket.isFavoring()) {
                progressBar.setVisibility(VISIBLE);
                button.setEnabled(false);
                button.setVisibility(GONE);
            } else {
                button.setEnabled(true);
                button.setVisibility(VISIBLE);
                progressBar.setVisibility(GONE);
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
        favor(basket);
    }

    private void favor(final Basket bas) {
        bas.setFavoring(true);
        button.setEnabled(false);
        button.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
        networkTask = FavorAPI.favorLink(link, title, bas, new RequestObject.CallBack<Boolean>() {
            @Override
            public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<Boolean> result) {
                bas.setFavoring(false);
                postBasketChanged(bas);
            }

            @Override
            public void onSuccess(@NonNull Boolean result, @NonNull ResponseObject<Boolean> detailed) {
                bas.setFavoring(false);
                bas.setHasFavored(true);
                postBasketChanged(bas);
            }
        });
    }

    public void postBasketChanged(Basket basket) {
        EventBus.getDefault().post(basket);
    }
}

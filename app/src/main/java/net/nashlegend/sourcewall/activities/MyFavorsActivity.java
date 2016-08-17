package net.nashlegend.sourcewall.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.ChannelsAdapter;
import net.nashlegend.sourcewall.db.BasketHelper;
import net.nashlegend.sourcewall.db.gen.MyBasket;
import net.nashlegend.sourcewall.fragment.ArticlePagerFragment;
import net.nashlegend.sourcewall.fragment.ArticlesFragment;
import net.nashlegend.sourcewall.fragment.FavorsFragment;
import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.NetworkTask;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.RequestObject.SimpleCallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.FavorAPI;
import net.nashlegend.sourcewall.util.ChannelHelper;
import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.view.common.LoadingView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static net.nashlegend.sourcewall.model.SubItem.Section_Favor;
import static net.nashlegend.sourcewall.model.SubItem.Type_Single_Channel;

public class MyFavorsActivity extends AppCompatActivity {

    ArrayList<SubItem> subItems = ChannelHelper.getBaskets();

    @BindView(R.id.favor_tabs)
    TabLayout tabLayout;
    @BindView(R.id.favor_pager)
    ViewPager viewPager;
    @BindView(R.id.favor_progress_loading)
    LoadingView loadingView;
    @BindView(R.id.action_bar)
    Toolbar toolbar;

    FavorPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_favors);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        adapter = new FavorPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        loadingView.setReloadListener(new LoadingView.ReloadListener() {
            @Override
            public void reload() {
                loadBaskets();
            }
        });
        loadingView.startLoading();
        loadBaskets();
    }

    private void loadBaskets() {
        FavorAPI.getBaskets(new SimpleCallBack<ArrayList<Basket>>() {
            @Override
            public void onFailure() {
                List<SubItem> subItems = BasketHelper.getAllMyBasketsSubItems();
                if (subItems.size() > 0) {
                    onGetBaskets(subItems);
                } else {
                    loadingView.onLoadFailed();
                }
            }

            @Override
            public void onSuccess(@NonNull ArrayList<Basket> result) {
                BasketHelper.putAllBaskets(result);
                ArrayList<SubItem> subItems = new ArrayList<>();
                for (int i = 0; i < result.size(); i++) {
                    Basket basket = result.get(i);
                    SubItem subItem = new SubItem(Section_Favor, Type_Single_Channel, basket.getName(), basket.getId());
                    subItems.add(subItem);
                }
                onGetBaskets(subItems);
                loadingView.onLoadSuccess();
            }
        });
    }

    private void onGetBaskets(List<SubItem> subItems) {
        if (isFinishing()) {
            return;
        }
        if (subItems == null || subItems.size() == 0) {
            ToastUtil.toastBigSingleton("您尚未添加收藏!");
            finish();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    class FavorPagerAdapter extends FragmentStatePagerAdapter {

        public FavorPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return subItems.get(position).getName();
        }

        @Override
        public Fragment getItem(int position) {
            return FavorsFragment.newInstance(subItems.get(position));
        }

        @Override
        public int getCount() {
            return subItems.size();
        }
    }
}

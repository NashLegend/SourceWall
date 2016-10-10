package net.nashlegend.sourcewall.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.data.Mob;
import net.nashlegend.sourcewall.fragment.FavorsFragment;
import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.RequestObject.SimpleCallBack;
import net.nashlegend.sourcewall.request.api.FavorAPI;
import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.view.common.LoadingView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static net.nashlegend.sourcewall.model.SubItem.Section_Favor;
import static net.nashlegend.sourcewall.model.SubItem.Type_Single_Channel;

public class MyFavorsActivity extends BaseActivity {

    final List<SubItem> subItems = new ArrayList<>();

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
        Mob.onEvent(Mob.Event_Open_My_Favors);
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
        loadBaskets();
    }

    private void loadBaskets() {
        loadingView.onLoading();
        FavorAPI.getBaskets(new SimpleCallBack<ArrayList<Basket>>() {
            @Override
            public void onFailure() {
                loadingView.onFailed();
            }

            @Override
            public void onSuccess(@NonNull ArrayList<Basket> result) {
                ArrayList<SubItem> fetchedSubItems = new ArrayList<>();
                for (int i = 0; i < result.size(); i++) {
                    Basket basket = result.get(i);
                    SubItem subItem = new SubItem(Section_Favor, Type_Single_Channel, basket.getName(), basket.getId());
                    fetchedSubItems.add(subItem);
                }
                onGetBaskets(fetchedSubItems);
            }
        });
    }

    private void onGetBaskets(List<SubItem> fetchedSubItems) {
        if (isFinishing()) {
            return;
        }
        loadingView.onSuccess();
        if (fetchedSubItems == null || fetchedSubItems.size() == 0) {
            ToastUtil.toastBigSingleton("还没有创建果篮 0.0 ");
            finish();
        } else {
            subItems.clear();
            subItems.addAll(fetchedSubItems);
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

package net.nashlegend.sourcewall.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.SearchActivity;
import net.nashlegend.sourcewall.adapters.FakeFragmentStatePagerAdapter;
import net.nashlegend.sourcewall.data.ChannelHelper;
import net.nashlegend.sourcewall.events.Emitter;
import net.nashlegend.sourcewall.events.ShowHideEvent;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.util.UiUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ArticlePagerFragment extends BaseFragment {
    View layoutView;
    ArrayList<SubItem> subItems = ChannelHelper.getArticles();
    @BindView(R.id.article_tabs)
    TabLayout tabLayout;
    @BindView(R.id.article_pager)
    ViewPager viewPager;
    @BindView(R.id.button_search)
    View searchButton;

    ArticlePagerAdapter adapter;

    public ArticlePagerFragment() {
        // Required empty public constructor
    }

    public static ArticlePagerFragment newInstance() {
        ArticlePagerFragment fragment = new ArticlePagerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Emitter.register(this);
        if (layoutView == null) {
            layoutView = inflater.inflate(R.layout.fragment_article_pager, container, false);
            ButterKnife.bind(this, layoutView);
            adapter = new ArticlePagerAdapter(getFragmentManager());
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);
        } else {
            if (layoutView.getParent() != null) {
                ((ViewGroup) layoutView.getParent()).removeView(layoutView);
            }
        }
        return layoutView;
    }

    @Override
    public void onDestroyView() {
        Emitter.unregister(this);
        super.onDestroyView();
    }

    @OnClick({R.id.button_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_search:
                startActivity(SearchActivity.class);
                break;
        }
    }

    @Override
    public boolean reTap() {
        if (adapter == null) {
            return false;
        }
        Fragment fragment = adapter.getFragmentAt(viewPager.getCurrentItem());
        return fragment instanceof ArticlesFragment && ((ArticlesFragment) fragment).reTap();
    }

    public void onEventMainThread(ShowHideEvent event) {
        if (event.section == SubItem.Section_Article) {
            if (event.show) {
                showSearch();
            } else {
                hideSearch();
            }
        }
    }

    ObjectAnimator hideAnimator;
    ObjectAnimator showAnimator;

    private void hideSearch() {
        UiUtil.dismissAnimator(showAnimator);
        if (hideAnimator != null && hideAnimator.isRunning()) {
            return;
        }
        hideAnimator = ObjectAnimator.ofFloat(searchButton, "translationY", searchButton.getTranslationY(), searchButton.getHeight());
        hideAnimator.setDuration(300);
        hideAnimator.start();
    }

    private void showSearch() {
        UiUtil.dismissAnimator(hideAnimator);
        if (showAnimator != null && showAnimator.isRunning()) {
            return;
        }
        showAnimator = ObjectAnimator.ofFloat(searchButton, "translationY", searchButton.getTranslationY(), 0);
        showAnimator.setDuration(300);
        showAnimator.start();
    }

    class ArticlePagerAdapter extends FakeFragmentStatePagerAdapter {

        ArticlePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return subItems.get(position).getName();
        }

        @Override
        public Fragment getItem(int position) {
            return ArticlesFragment.newInstance(subItems.get(position));
        }

        @Override
        public int getCount() {
            return subItems.size();
        }
    }

}

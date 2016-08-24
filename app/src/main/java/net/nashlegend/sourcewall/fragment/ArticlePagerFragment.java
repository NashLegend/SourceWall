package net.nashlegend.sourcewall.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.BaseActivity;
import net.nashlegend.sourcewall.activities.PublishPostActivity;
import net.nashlegend.sourcewall.activities.SearchActivity;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.ChannelHelper;
import net.nashlegend.sourcewall.util.Consts;

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

    @OnClick({R.id.button_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_search:
                startActivity(SearchActivity.class);
                break;
        }
    }

    class ArticlePagerAdapter extends FragmentStatePagerAdapter {

        public ArticlePagerAdapter(FragmentManager fm) {
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

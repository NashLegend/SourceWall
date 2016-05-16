package net.nashlegend.sourcewall.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.BuildConfig;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.events.OpenContentFragmentEvent;
import net.nashlegend.sourcewall.events.PrepareOpenContentFragmentEvent;
import net.nashlegend.sourcewall.fragment.ArticlesFragment;
import net.nashlegend.sourcewall.fragment.ChannelsFragment;
import net.nashlegend.sourcewall.fragment.FavorsFragment;
import net.nashlegend.sourcewall.fragment.NavigationDrawerFragment;
import net.nashlegend.sourcewall.fragment.PostsFragment;
import net.nashlegend.sourcewall.fragment.QuestionsFragment;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.cache.RequestCache;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity {
    @BindView(R.id.action_bar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.navigation_drawer)
    View container;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ArticlesFragment articlesFragment;
    private PostsFragment postsFragment;
    private QuestionsFragment questionsFragment;
    private FavorsFragment favorsFragment;
    public ChannelsFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSwipeEnabled(false);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        setSupportActionBar(toolbar);

        toolbar.setOnClickListener(new View.OnClickListener() {
            boolean preparingToScrollToHead = false;

            @Override
            public void onClick(View v) {
                // STOPSHIP: 16/5/15
                Main2Activity.open();
                if (!mNavigationDrawerFragment.isDrawerOpen()) {
                    if (preparingToScrollToHead) {
                        if (currentFragment != null) {
                            currentFragment.scrollToHead();
                        }
                    } else {
                        preparingToScrollToHead = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                preparingToScrollToHead = false;
                            }
                        }, 200);
                    }
                }
            }
        });
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(container, drawerLayout, toolbar);
    }

    @Override
    protected void onPause() {
        RequestCache.getInstance().flushCache();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        RequestCache.getInstance().closeCache();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getTitle());
    }

    boolean preparingToExit = false;

    @Override
    public void onBackPressed() {
        if (currentFragment != null && currentFragment.takeOverBackPressed()) {
            return;
        }

        if (isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
            return;
        }

        if (preparingToExit) {
            super.onBackPressed();
        } else {
            preparingToExit = true;
            toastSingleton(R.string.click_again_to_exit);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    preparingToExit = false;
                }
            }, Config.ExitTapsGap);
        }
    }

    public boolean isDrawerOpen() {
        return mNavigationDrawerFragment != null && mNavigationDrawerFragment.isDrawerOpen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            if (currentFragment == null || !currentFragment.takeOverMenuInflate(getMenuInflater(), menu)) {
                getMenuInflater().inflate(R.menu.main, menu);
            }
            restoreActionBar();
            initSearchView(menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void initSearchView(final Menu menu) {
        if (menu == null || menu.findItem(R.id.search) == null) {
            return;
        }
        final MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(true);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                setItemsVisibility(menu, searchItem, false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                setItemsVisibility(menu, null, true);
                invalidateOptionsMenu();
                return true;
            }
        });
    }

    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return currentFragment != null
                && currentFragment.takeOverOptionsItemSelect(item)
                || super.onOptionsItemSelected(item);
    }

    public void replaceFragment(ChannelsFragment fragment, SubItem subItem) {
        if (currentFragment == fragment) {
            fragment.resetData(subItem);
            invalidateOptionsMenu();
        } else {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Consts.Extra_SubItem, subItem);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commitAllowingStateLoss();
            currentFragment = fragment;
        }
    }

    public void prepareFragment(SubItem subItem) {
        if (currentFragment != null) {
            currentFragment.prepareLoading(subItem);
        }
    }

    public void onEventMainThread(OpenContentFragmentEvent event) {
        SubItem subItem = event.subItem;
        if (subItem == null) {
            subItem = new SubItem(SubItem.Section_Article, SubItem.Type_Collections, "科学人", "");
        }
        switch (subItem.getSection()) {
            case SubItem.Section_Article:
                if (articlesFragment == null) {
                    articlesFragment = new ArticlesFragment();
                }
                replaceFragment(articlesFragment, subItem);
                break;
            case SubItem.Section_Post:
                if (postsFragment == null) {
                    postsFragment = new PostsFragment();
                }
                replaceFragment(postsFragment, subItem);
                break;
            case SubItem.Section_Question:
                if (questionsFragment == null) {
                    questionsFragment = new QuestionsFragment();
                }
                replaceFragment(questionsFragment, subItem);
                break;
            case SubItem.Section_Favor:
                if (favorsFragment == null) {
                    favorsFragment = new FavorsFragment();
                }
                replaceFragment(favorsFragment, subItem);
                break;
        }
    }

    public void onEventMainThread(PrepareOpenContentFragmentEvent event) {
        SubItem subItem = event.subItem;
        if (subItem == null) {
            subItem = new SubItem(SubItem.Section_Article, SubItem.Type_Collections, "科学人", "");
        }
        prepareFragment(subItem);
    }

}

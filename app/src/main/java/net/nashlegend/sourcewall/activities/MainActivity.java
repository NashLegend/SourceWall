package net.nashlegend.sourcewall.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import net.nashlegend.sourcewall.fragment.ArticlesFragment;
import net.nashlegend.sourcewall.fragment.ChannelsFragment;
import net.nashlegend.sourcewall.fragment.NavigationDrawerFragment;
import net.nashlegend.sourcewall.fragment.PostsFragment;
import net.nashlegend.sourcewall.fragment.QuestionsFragment;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.cache.RequestCache;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;

public class MainActivity extends BaseActivity {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private Receiver receiver;
    private ArticlesFragment articlesFragment;
    private PostsFragment postsFragment;
    private QuestionsFragment questionsFragment;
    public ChannelsFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        AnalyticsConfig.enableEncrypt(true);
        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Consts.Action_Open_Content_Fragment);
        filter.addAction(Consts.Action_Prepare_Open_Content_Fragment);
        registerReceiver(receiver, filter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            boolean preparingToScrollToHead = false;

            @Override
            public void onClick(View v) {
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
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
    }

    @Override
    protected void onPause() {
        RequestCache.getInstance().flushCache();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        RequestCache.getInstance().closeCache();
        unregisterReceiver(receiver);
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

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            SubItem subItem = intent.getParcelableExtra(Consts.Extra_SubItem);
            if (subItem == null) {
                subItem = new SubItem(SubItem.Section_Article, SubItem.Type_Collections, "科学人", "");
            }
            if (Consts.Action_Open_Content_Fragment.equals(intent.getAction())) {
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
                }
            } else if (Consts.Action_Prepare_Open_Content_Fragment.equals(intent.getAction())) {
                prepareFragment(subItem);
            }
        }
    }
}

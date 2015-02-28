package net.nashlegend.sourcewall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.nashlegend.sourcewall.fragment.ArticlesFragment;
import net.nashlegend.sourcewall.fragment.ChannelsFragment;
import net.nashlegend.sourcewall.fragment.NavigationDrawerFragment;
import net.nashlegend.sourcewall.fragment.PostsFragment;
import net.nashlegend.sourcewall.fragment.QuestionsFragment;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedUtil;
import net.nashlegend.sourcewall.util.ToastUtil;


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

    @Override
    public void setTheme(int resId) {
        if (SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            resId = net.nashlegend.sourcewall.R.style.BottomThemeNight;
        } else {
            resId = net.nashlegend.sourcewall.R.style.BottomTheme;
        }
        super.setTheme(resId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(net.nashlegend.sourcewall.R.layout.activity_main);
        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Consts.Action_Open_Content_Fragment);
        registerReceiver(receiver, filter);
        Toolbar toolbar = (Toolbar) findViewById(net.nashlegend.sourcewall.R.id.action_bar);
        setSupportActionBar(toolbar);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(net.nashlegend.sourcewall.R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                net.nashlegend.sourcewall.R.id.navigation_drawer,
                (DrawerLayout) findViewById(net.nashlegend.sourcewall.R.id.drawer_layout), toolbar);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getTitle());
    }

    boolean preparingToExit = false;

    @Override
    public void onBackPressed() {
        if (currentFragment != null && currentFragment.takeOverBackPressed()) {
            return;
        }
        if (preparingToExit) {
            super.onBackPressed();
        } else {
            preparingToExit = true;
            ToastUtil.toastSingleton(getString(R.string.click_again_to_exit));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    preparingToExit = false;
                }
            }, Config.ExitTapsGap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            if (currentFragment != null) {
                currentFragment.takeOverMenuInflate(getMenuInflater(), menu);
            } else {
                getMenuInflater().inflate(net.nashlegend.sourcewall.R.menu.main, menu);
            }
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (currentFragment != null && currentFragment.takeOverOptionsItemSelect(item)) {
            return true;
        }
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private ChannelsFragment currentFragment;

    public void replaceFragment(ChannelsFragment fragment, SubItem subItem) {
        if (currentFragment == fragment) {
            fragment.resetData(subItem);
        } else {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Consts.Extra_SubItem, subItem);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(net.nashlegend.sourcewall.R.id.container, fragment).commitAllowingStateLoss();
            currentFragment = fragment;
        }
    }

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            SubItem subItem = (SubItem) intent.getSerializableExtra(Consts.Extra_SubItem);
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
        }
    }
}

package com.example.sourcewall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.sourcewall.fragment.ArticlesFragment;
import com.example.sourcewall.fragment.ChannelsFragment;
import com.example.sourcewall.fragment.NavigationDrawerFragment;
import com.example.sourcewall.fragment.PostsFragment;
import com.example.sourcewall.fragment.QuestionsFragment;
import com.example.sourcewall.model.SubItem;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.SharedUtil;


public class MainActivity extends BaseActivity {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    Receiver receiver;
    ArticlesFragment articlesFragment;
    PostsFragment postsFragment;
    QuestionsFragment questionsFragment;
    Toolbar toolbar;

    @Override
    public void setTheme(int resId) {
        if (SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            resId = R.style.BottomThemeNight;
        } else {
            resId = R.style.BottomTheme;
        }
        super.setTheme(resId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Consts.Action_Open_Content_Fragment);
        registerReceiver(receiver, filter);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            if (currentFragment != null) {
                currentFragment.takeOverMenuInflate(getMenuInflater(), menu);
            } else {
                getMenuInflater().inflate(R.menu.main, menu);
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
            currentFragment = fragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }
    }

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            SubItem subItem = (SubItem) intent.getSerializableExtra(Consts.Extra_SubItem);
            boolean shouldInvalidateMenu = intent.getBooleanExtra(Consts.Extra_Should_Invalidate_Menu, false);
            if (shouldInvalidateMenu){
                MainActivity.this.invalidateOptionsMenu();
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
            }
        }
    }
}

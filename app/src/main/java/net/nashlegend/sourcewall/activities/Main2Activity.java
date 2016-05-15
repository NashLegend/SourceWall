package net.nashlegend.sourcewall.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.fragment.ArticlePagerFragment;
import net.nashlegend.sourcewall.fragment.PostPagerFragment;
import net.nashlegend.sourcewall.fragment.ProfileFragment;
import net.nashlegend.sourcewall.fragment.QuestionPagerFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.majiajie.pagerbottomtabstrip.Controller;
import me.majiajie.pagerbottomtabstrip.PagerBottomTabLayout;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectListener;

public class Main2Activity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.main2_content)
    FrameLayout main2Content;
    @BindView(R.id.bottom_bar)
    PagerBottomTabLayout bottomBar;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    Fragment crtFragment;
    ArticlePagerFragment articlePagerFragment;
    PostPagerFragment postPagerFragment;
    QuestionPagerFragment questionPagerFragment;
    ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);
        setSwipeEnabled(false);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        initPages();
    }

    private void initPages() {
        Controller controller = bottomBar.builder()
                .addTabItem(android.R.drawable.ic_menu_gallery, "科学人")
                .addTabItem(android.R.drawable.ic_menu_manage, "小组")
                .addTabItem(android.R.drawable.ic_search_category_default, "小组")
                .addTabItem(android.R.drawable.ic_menu_always_landscape_portrait, "我")
                .build();
        controller.addTabItemClickListener(new OnTabItemSelectListener() {
            @Override
            public void onSelected(int index, Object tag) {
                crtFragment = getFragmentByPosition(index);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main2_content, crtFragment).commitAllowingStateLoss();
            }

            @Override
            public void onRepeatClick(int index, Object tag) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private Fragment getFragmentByPosition(int idx) {
        Fragment fragment = null;
        switch (idx) {
            case 0:
                if (articlePagerFragment == null) {
                    articlePagerFragment = ArticlePagerFragment.newInstance();
                }
                fragment = articlePagerFragment;
                break;
            case 1:
                if (postPagerFragment == null) {
                    postPagerFragment = PostPagerFragment.newInstance();
                }
                fragment = postPagerFragment;
                break;
            case 2:
                if (questionPagerFragment == null) {
                    questionPagerFragment = QuestionPagerFragment.newInstance();
                }
                fragment = questionPagerFragment;
                break;
            case 3:
                if (profileFragment == null) {
                    profileFragment = ProfileFragment.newInstance();
                }
                fragment = profileFragment;
                break;
        }
        return fragment;
    }

    public static void open(){
        Intent intent = new Intent(App.getApp(),Main2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getApp().startActivity(intent);
    }
}

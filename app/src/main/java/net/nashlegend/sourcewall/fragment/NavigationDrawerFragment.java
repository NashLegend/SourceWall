package net.nashlegend.sourcewall.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.LoginActivity;
import net.nashlegend.sourcewall.activities.MessageCenterActivity;
import net.nashlegend.sourcewall.activities.SettingActivity;
import net.nashlegend.sourcewall.adapters.ChannelsAdapter;
import net.nashlegend.sourcewall.events.LoginStateChangedEvent;
import net.nashlegend.sourcewall.events.OpenContentFragmentEvent;
import net.nashlegend.sourcewall.events.PrepareOpenContentFragmentEvent;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.model.UserInfo;
import net.nashlegend.sourcewall.request.RequestObject.CallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.MessageAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.ImageUtils;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.view.SubItemView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class NavigationDrawerFragment extends BaseFragment implements View.OnClickListener {

    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    @BindView(R.id.image_avatar)
    ImageView avatarView;
    @BindView(R.id.text_name)
    TextView userName;
    @BindView(R.id.image_notice)
    ImageView noticeView;
    @BindView(R.id.layout_user)
    LinearLayout userView;
    @BindView(R.id.list_channel)
    ExpandableListView listView;
    @BindView(R.id.view_switch_to_day)
    LinearLayout dayView;
    @BindView(R.id.view_switch_to_night)
    LinearLayout nightView;
    @BindView(R.id.view_setting)
    LinearLayout settingView;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private boolean mFromSavedInstanceState;
    private SubItem currentSubItem = new SubItem(SubItem.Section_Article, SubItem.Type_Collections, "科学人", "");
    private boolean mUserLearnedDrawer;
    private ChannelsAdapter adapter;
    private boolean currentLoginState = false;
    private String currentUkey = "";
    private SubItem lazyItem;

    public NavigationDrawerFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            int section = savedInstanceState.getInt("section", SubItem.Section_Article);
            int type = savedInstanceState.getInt("type", SubItem.Type_Collections);
            String name = savedInstanceState.getString("name", "科学人");
            String value = savedInstanceState.getString("value", "");
            currentSubItem = new SubItem(section, type, name, value);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        View layoutView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        ButterKnife.bind(this, layoutView);
        if (SharedPreferencesUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            dayView.setVisibility(View.VISIBLE);
            nightView.setVisibility(View.GONE);
        } else {
            dayView.setVisibility(View.GONE);
            nightView.setVisibility(View.VISIBLE);
        }

        listView = (ExpandableListView) layoutView.findViewById(R.id.list_channel);
        listView.setGroupIndicator(null);
        adapter = new ChannelsAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (v instanceof SubItemView) {
                    if (mDrawerLayout != null) {
                        mDrawerLayout.closeDrawer(mFragmentContainerView);
                    }

                    SubItem subItem = ((SubItemView) v).getSubItem();
                    currentSubItem = subItem;
                    lazyItem = subItem;

                    EventBus.getDefault().post(new PrepareOpenContentFragmentEvent(subItem));
                }
                return false;
            }
        });

        currentLoginState = UserAPI.isLoggedIn();
        currentUkey = UserAPI.getUkey();
        if (currentLoginState) {
            setupLocalUserView();
        }
        return layoutView;
    }

    @Override
    public void onResume() {
        super.onResume();
        recheckData();
    }

    private void recheckData() {
        adapter.setDefaultChannels();
        if (currentLoginState != UserAPI.isLoggedIn()) {
            if (UserAPI.isLoggedIn()) {
                loadUserInfo();
            } else {
                back2UnLogged();
            }
        } else {
            if (UserAPI.isLoggedIn()) {
                if (currentUkey != null && currentUkey.equals(UserAPI.getUkey())) {
                    setupLocalUserView();
                } else {
                    //切换了用户的话
                    loadUserInfo();
                }
            }
        }
        currentLoginState = UserAPI.isLoggedIn();
        currentUkey = UserAPI.getUkey();
        if (currentLoginState) {
            loadMessages();
        } else {
            noticeView.setVisibility(View.GONE);
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    public void setUp(View container, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = container;
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                if (lazyItem != null) {
                    EventBus.getDefault().post(new OpenContentFragmentEvent(lazyItem));
                    lazyItem = null;
                } else {
                    getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (currentSubItem != null) {
            EventBus.getDefault().post(new OpenContentFragmentEvent(currentSubItem));
        }

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentSubItem != null) {
            outState.putInt("section", currentSubItem.getSection());
            outState.putInt("type", currentSubItem.getType());
            outState.putString("value", currentSubItem.getValue());
            outState.putString("name", currentSubItem.getName());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isAdded() && mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void onUserViewClicked() {
        if (!UserAPI.isLoggedIn()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivityForResult(intent, Consts.Code_Login);
        } else {
            if (TextUtils.isEmpty(UserAPI.getName())) {
                loadUserInfo();
            } else {
                Intent intent = new Intent(getActivity(), MessageCenterActivity.class);
                startActivityForResult(intent, Consts.Code_Message_Center);
            }
        }
    }

    private void revertMode() {
        SharedPreferencesUtil.saveBoolean(Consts.Key_Is_Night_Mode, !SharedPreferencesUtil.readBoolean(Consts.Key_Is_Night_Mode, false));
        MobclickAgent.onEvent(getActivity(), Mob.Event_Switch_Day_Night_Mode);
        getActivity().recreate();
    }

    @OnClick({R.id.layout_user, R.id.view_setting, R.id.view_switch_to_day, R.id.view_switch_to_night})
    public void onClick(View v) {
        if (UiUtil.shouldThrottle()) {
            return;
        }
        switch (v.getId()) {
            case R.id.layout_user:
                onUserViewClicked();
                break;
            case R.id.view_setting:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            case R.id.view_switch_to_day:
            case R.id.view_switch_to_night:
                revertMode();
                break;
        }
    }

    public void onEventMainThread(LoginStateChangedEvent e) {
        recheckData();
    }

    /**
     * 清除头像与名字，回到未登录状态
     */
    private void back2UnLogged() {
        avatarView.setImageResource(R.drawable.default_avatar);
        userName.setText(R.string.click_to_login);
    }

    private void loadUserInfo() {
        if (UserAPI.isLoggedIn()) {
            String nameString = UserAPI.getName();
            if (TextUtils.isEmpty(nameString)) {
                userName.setText(R.string.loading);
            }
            UserAPI.getUserInfoByUkey(UserAPI.getUkey(), new CallBack<UserInfo>() {
                @Override
                public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<UserInfo> result) {
                    String nameString = UserAPI.getName();
                    if (TextUtils.isEmpty(nameString)) {
                        userName.setText(R.string.click_to_reload);
                    }
                }

                @Override
                public void onSuccess(@NonNull UserInfo result, @NonNull ResponseObject<UserInfo> detailed) {
                    setupUserInfo(result);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Consts.Code_Login && resultCode == Activity.RESULT_OK) {
            /**
             * TODO
             * 以后可能会在其他地方添加登录入口，比如回复时提示登录，这个地方会加入登录按钮，
             * 这时候此处需要主动登录，而不是等待一个onActivityResult了
             */
            loadUserInfo();
        }
    }

    private void loadMessages() {
        MessageAPI.getReminderAndNoticeNum(new CallBack<ReminderNoticeNum>() {
            @Override
            public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<ReminderNoticeNum> result) {
                noticeView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(@NonNull ReminderNoticeNum result, @NonNull ResponseObject<ReminderNoticeNum> detailed) {
                if (result.getNotice_num() > 0) {
                    noticeView.setVisibility(View.VISIBLE);
                } else {
                    noticeView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupLocalUserView() {
        String nameString = UserAPI.getName();
        if (!TextUtils.isEmpty(nameString)) {
            userName.setText(nameString);
        }
        String avatarString = UserAPI.getUserAvatar();
        if (!TextUtils.isEmpty(avatarString)) {
            if (Config.shouldLoadImage()) {
                ImageLoader.getInstance().displayImage(avatarString, avatarView, ImageUtils.avatarOptions);
            } else {
                avatarView.setImageResource(R.drawable.default_avatar);
            }
        }
    }

    private void setupUserInfo(UserInfo info) {
        SharedPreferencesUtil.saveString(Consts.Key_User_Name, info.getNickname());
        SharedPreferencesUtil.saveString(Consts.Key_User_ID, info.getId());
        SharedPreferencesUtil.saveString(Consts.Key_User_Avatar, info.getAvatar());
        if (Config.shouldLoadImage()) {
            ImageLoader.getInstance().displayImage(info.getAvatar(), avatarView, ImageUtils.avatarOptions);
        } else {
            avatarView.setImageResource(R.drawable.default_avatar);
        }
        userName.setText(info.getNickname());
    }

}

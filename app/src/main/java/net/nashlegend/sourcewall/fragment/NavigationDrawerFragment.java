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
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.LoginActivity;
import net.nashlegend.sourcewall.activities.MessageCenterActivity;
import net.nashlegend.sourcewall.activities.SettingActivity;
import net.nashlegend.sourcewall.adapters.ChannelsAdapter;
import net.nashlegend.sourcewall.db.AskTagHelper;
import net.nashlegend.sourcewall.db.GroupHelper;
import net.nashlegend.sourcewall.events.LoginStateChangedEvent;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.model.UserInfo;
import net.nashlegend.sourcewall.request.api.MessageAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.swrequest.RequestObject;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.util.ChannelHelper;
import net.nashlegend.sourcewall.util.CommonUtil;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.ImageUtils;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;
import net.nashlegend.sourcewall.view.SubItemView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class NavigationDrawerFragment extends BaseFragment implements View.OnClickListener {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private View layoutView;
    private View mFragmentContainerView;

    private boolean mFromSavedInstanceState;
    private SubItem currentSubItem = new SubItem(SubItem.Section_Article, SubItem.Type_Collections, "科学人", "");
    private boolean mUserLearnedDrawer;
    private ExpandableListView listView;
    private ChannelsAdapter adapter;
    private View settingView;
    private View userView;
    private View dayView;
    private View nightView;
    private ImageView avatarView;
    private TextView userName;
    private boolean loginState = false;
    private String userKey = "";
    private boolean isFirstLoad = true;
    private ImageView noticeView;
    private Intent lazyIntent;

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
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        settingView = layoutView.findViewById(R.id.view_setting);
        userView = layoutView.findViewById(R.id.layout_user);
        dayView = layoutView.findViewById(R.id.view_switch_to_day);
        nightView = layoutView.findViewById(R.id.view_switch_to_night);

        if (SharedPreferencesUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            dayView.setVisibility(View.VISIBLE);
            nightView.setVisibility(View.GONE);
        } else {
            dayView.setVisibility(View.GONE);
            nightView.setVisibility(View.VISIBLE);
        }

        avatarView = (ImageView) layoutView.findViewById(R.id.image_avatar);
        userName = (TextView) layoutView.findViewById(R.id.text_name);
        noticeView = (ImageView) layoutView.findViewById(R.id.image_notice);
        settingView.setOnClickListener(this);
        userView.setOnClickListener(this);
        dayView.setOnClickListener(this);
        nightView.setOnClickListener(this);

        listView = (ExpandableListView) layoutView.findViewById(R.id.list_channel);
        listView.setGroupIndicator(null);
        adapter = new ChannelsAdapter(getActivity());
        adapter.createDefaultChannels();
        checkChannelList();
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
                    lazyIntent = new Intent();
                    lazyIntent.setAction(Consts.Action_Open_Content_Fragment);
                    lazyIntent.putExtra(Consts.Extra_SubItem, subItem);

                    Intent prepareIntent = new Intent();
                    prepareIntent.setAction(Consts.Action_Prepare_Open_Content_Fragment);
                    prepareIntent.putExtra(Consts.Extra_SubItem, subItem);
                    getActivity().sendBroadcast(prepareIntent);
                }
                return false;
            }
        });
        return layoutView;
    }

    @Override
    public void onCreateViewAgain(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    }

    @Override
    public void setTitle() {

    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                if (lazyIntent != null) {
                    getActivity().sendBroadcast(lazyIntent);
                    lazyIntent = null;
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
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
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

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (currentSubItem != null) {
            lazyIntent = new Intent();
            lazyIntent.setAction(Consts.Action_Open_Content_Fragment);
            lazyIntent.putExtra(Consts.Extra_SubItem, currentSubItem);
            getActivity().sendBroadcast(lazyIntent);
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

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
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
            getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
        } else {
            if (TextUtils.isEmpty(UserAPI.getName())) {
                loadUserInfo();
            } else {
                Intent intent = new Intent(getActivity(), MessageCenterActivity.class);
                startActivityForResult(intent, Consts.Code_Message_Center);
                getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
            }
        }
    }

    private void revertMode() {
        SharedPreferencesUtil.saveBoolean(Consts.Key_Is_Night_Mode, !SharedPreferencesUtil.readBoolean(Consts.Key_Is_Night_Mode, false));
        MobclickAgent.onEvent(getActivity(), Mob.Event_Switch_Day_Night_Mode);
        getActivity().recreate();
    }

    @Override
    public void onClick(View v) {
        if (CommonUtil.shouldThrottle()) {
            return;
        }
        switch (v.getId()) {
            case R.id.layout_user:
                onUserViewClicked();
                break;
            case R.id.view_setting:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
                break;
            case R.id.view_switch_to_day:
            case R.id.view_switch_to_night:
                revertMode();
                break;
        }
    }

    long currentGroupDBVersion = -1;
    long currentTagDBVersion = -1;

    public void onEventMainThread(LoginStateChangedEvent e) {
        recheckData();
    }

    @Override
    public void onResume() {
        super.onResume();
        recheckData();
    }

    private void recheckData() {
        if (isFirstLoad) {
            if (UserAPI.isLoggedIn()) {
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
            isFirstLoad = false;
        } else {
            if (loginState != UserAPI.isLoggedIn()) {
                checkChannelList();
                if (UserAPI.isLoggedIn()) {
                    loadUserInfo();
                } else {
                    back2UnLogged();
                }
            } else {
                if (UserAPI.isLoggedIn()) {
                    if (userKey != null && userKey.equals(UserAPI.getUkey())) {
                        String avatarString = UserAPI.getUserAvatar();
                        if (!TextUtils.isEmpty(avatarString)) {
                            if (Config.shouldLoadImage()) {
                                ImageLoader.getInstance().displayImage(avatarString, avatarView, ImageUtils.avatarOptions);
                            } else {
                                avatarView.setImageResource(R.drawable.default_avatar);
                            }
                        }
                        //重新加载小组数据库
                        long lastGroupDBVersion = SharedPreferencesUtil.readLong(Consts.Key_Last_Post_Groups_Version, 0);
                        if (currentGroupDBVersion != lastGroupDBVersion) {
                            ArrayList<SubItem> groupSubItems = adapter.getSubLists().get(1);
                            groupSubItems.clear();
                            groupSubItems.add(new SubItem(SubItem.Section_Post, SubItem.Type_Private_Channel, "我的小组", "user_group"));
                            if (GroupHelper.getMyGroupsNumber() > 0) {
                                //如果已经加载了栏目
                                groupSubItems.add(new SubItem(SubItem.Section_Post, SubItem.Type_Collections, "小组热贴", "hot_posts"));
                                groupSubItems.addAll(GroupHelper.getSelectedGroupSubItems());
                            } else {
                                groupSubItems.addAll(ChannelHelper.getPosts());
                            }
                        }
                        currentGroupDBVersion = lastGroupDBVersion;

                        //重新加载标签数据库
                        long lastTagDBVersion = SharedPreferencesUtil.readLong(Consts.Key_Last_Ask_Tags_Version, 0);
                        if (currentTagDBVersion != lastTagDBVersion) {
                            ArrayList<SubItem> questionSubItems = adapter.getSubLists().get(2);
                            if (AskTagHelper.getAskTagsNumber() > 0) {
                                //如果已经加载了栏目
                                questionSubItems.clear();
                                questionSubItems.add(new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "热门问答", "hottest"));
                                questionSubItems.add(new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "精彩回答", "highlight"));
                                questionSubItems.addAll(AskTagHelper.getSelectedQuestionSubItems());
                            } else {
                                questionSubItems.clear();
                                questionSubItems.addAll(ChannelHelper.getQuestions());
                            }
                        }
                        currentTagDBVersion = lastTagDBVersion;
                        adapter.notifyDataSetChanged();
                    } else {
                        //切换了用户的话
                        checkChannelList();
                        loadUserInfo();
                    }
                }
            }
        }
        loginState = UserAPI.isLoggedIn();
        userKey = UserAPI.getUkey();
        if (loginState) {
            loadMessages();
        } else {
            noticeView.setVisibility(View.GONE);
        }
    }

    /**
     * 清除头像与名字，回到未登录状态
     */
    private void back2UnLogged() {
        avatarView.setImageResource(R.drawable.default_avatar);
        userName.setText(R.string.click_to_login);
    }

    /**
     * 重新验证当前ChannelList是否是对的
     */
    private void checkChannelList() {
        ArrayList<SubItem> groupSubItems = adapter.getSubLists().get(1);
        groupSubItems.clear();
        if (UserAPI.isLoggedIn()) {
            groupSubItems.add(new SubItem(SubItem.Section_Post, SubItem.Type_Private_Channel, "我的小组", "user_group"));
        }
        if (GroupHelper.getMyGroupsNumber() > 0) {
            //如果已经加载了栏目
            groupSubItems.add(new SubItem(SubItem.Section_Post, SubItem.Type_Collections, "小组热贴", "hot_posts"));
            groupSubItems.addAll(GroupHelper.getSelectedGroupSubItems());
        } else {
            groupSubItems.addAll(ChannelHelper.getPosts());
        }

        ArrayList<SubItem> questionSubItems = adapter.getSubLists().get(2);
        questionSubItems.clear();
        if (AskTagHelper.getAskTagsNumber() > 0) {
            //如果已经加载了栏目
            questionSubItems.add(new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "热门问答", "hottest"));
            questionSubItems.add(new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "精彩回答", "highlight"));
            questionSubItems.addAll(AskTagHelper.getSelectedQuestionSubItems());
        } else {
            questionSubItems.addAll(ChannelHelper.getQuestions());
        }
        adapter.notifyDataSetInvalidated();
    }

    private void loadUserInfo() {
        if (UserAPI.isLoggedIn()) {
            String nameString = UserAPI.getName();
            if (TextUtils.isEmpty(nameString)) {
                userName.setText(R.string.loading);
            }
            UserAPI.getUserInfoByUkey(UserAPI.getUkey(), new RequestObject.CallBack<UserInfo>() {
                @Override
                public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<UserInfo> result) {
                    String nameString = UserAPI.getName();
                    if (TextUtils.isEmpty(nameString)) {
                        userName.setText(R.string.click_to_reload);
                    }
                }

                @Override
                public void onSuccess(@NonNull ResponseObject<UserInfo> result) {
                    setupUserInfo(result.result);
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
        MessageAPI.getReminderAndNoticeNum(new RequestObject.CallBack<ReminderNoticeNum>() {
            @Override
            public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<ReminderNoticeNum> result) {
                noticeView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(@NonNull ResponseObject<ReminderNoticeNum> result) {
                ReminderNoticeNum num = result.result;
                if (num.getNotice_num() > 0) {
                    noticeView.setVisibility(View.VISIBLE);
                } else {
                    noticeView.setVisibility(View.GONE);
                }
            }
        });
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

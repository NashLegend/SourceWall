package net.nashlegend.sourcewall.fragment;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
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

import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.LoginActivity;
import net.nashlegend.sourcewall.MessageCenterActivity;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.SettingActivity;
import net.nashlegend.sourcewall.adapters.ChannelsAdapter;
import net.nashlegend.sourcewall.db.AskTagHelper;
import net.nashlegend.sourcewall.db.GroupHelper;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.model.UserInfo;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.ChannelHelper;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;
import net.nashlegend.sourcewall.view.SubItemView;

import java.util.ArrayList;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 * Created by NashLegend on 2014/9/15 0015.
 */
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

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
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
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutView = inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

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
                }
//                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
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
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
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

        SubItem subItem = (SubItem) adapter.getChild(0, 0);
        Intent intent = new Intent();
        intent.setAction(Consts.Action_Open_Content_Fragment);
        intent.putExtra(Consts.Extra_SubItem, subItem);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout != null && isDrawerOpen()) {
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
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    private void onUserViewClicked() {
        if (!UserAPI.isLoggedIn()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivityForResult(intent, Consts.Code_Login);
            getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
        } else {
            String nameString = SharedPreferencesUtil.readString(Consts.Key_User_Name, "");
            if (TextUtils.isEmpty(nameString)) {
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

    @Override
    public void onResume() {
        super.onResume();
        recheckData();
    }

    private void recheckData() {
        if (isFirstLoad) {
            if (UserAPI.isLoggedIn()) {
                String nameString = SharedPreferencesUtil.readString(Consts.Key_User_Name, "");
                if (!TextUtils.isEmpty(nameString)) {
                    userName.setText(nameString);
                }
                String avatarString = SharedPreferencesUtil.readString(Consts.Key_User_Avatar, "");
                if (!TextUtils.isEmpty(avatarString)) {
                    if (Config.shouldLoadImage()) {
                        Picasso.with(getActivity()).load(avatarString)
                                .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen).placeholder(R.drawable.default_avatar)
                                .into(avatarView);
                    } else {
                        avatarView.setImageResource(R.drawable.default_avatar);
                    }
                }
                testLogin();
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
                    if (userKey != null && userKey.equals(SharedPreferencesUtil.readString(Consts.Key_Ukey, ""))) {
                        String avatarString = SharedPreferencesUtil.readString(Consts.Key_User_Avatar, "");
                        if (!TextUtils.isEmpty(avatarString)) {
                            if (Config.shouldLoadImage()) {
                                Picasso.with(getActivity()).load(avatarString)
                                        .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen)
                                        .into(avatarView);
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
                        checkChannelList();
                        if (UserAPI.isLoggedIn()) {
                            loadUserInfo();
                        } else {
                            back2UnLogged();
                        }
                    }
                }
            }
        }
        loginState = UserAPI.isLoggedIn();
        userKey = SharedPreferencesUtil.readString(Consts.Key_Ukey, "");
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
        avatarView.setImageBitmap(null);
        avatarView.setImageResource(R.drawable.default_avatar);
        userName.setText(R.string.click_to_login);
    }

    /**
     * 重新验证当前ChannelList是否是对的
     */
    private void checkChannelList() {
        ArrayList<SubItem> groupSubItems = adapter.getSubLists().get(1);
        if (UserAPI.isLoggedIn()) {
            groupSubItems.clear();
            groupSubItems.add(new SubItem(SubItem.Section_Post, SubItem.Type_Private_Channel, "我的小组", "user_group"));
            if (GroupHelper.getMyGroupsNumber() > 0) {
                //如果已经加载了栏目
                groupSubItems.add(new SubItem(SubItem.Section_Post, SubItem.Type_Collections, "小组热贴", "hot_posts"));
                groupSubItems.addAll(GroupHelper.getSelectedGroupSubItems());
            } else {
                groupSubItems.addAll(ChannelHelper.getPosts());
            }
        } else {
            groupSubItems.clear();
            groupSubItems.addAll(ChannelHelper.getPosts());
        }

        ArrayList<SubItem> questionSubItems = adapter.getSubLists().get(2);
        if (UserAPI.isLoggedIn() && AskTagHelper.getAskTagsNumber() > 0) {
            //如果已经加载了栏目
            questionSubItems.clear();
            questionSubItems.add(new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "热门问答", "hottest"));
            questionSubItems.add(new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "精彩回答", "highlight"));
            questionSubItems.addAll(AskTagHelper.getSelectedQuestionSubItems());
        } else {
            questionSubItems.clear();
            questionSubItems.addAll(ChannelHelper.getQuestions());
        }
        adapter.notifyDataSetInvalidated();
    }

    private void testLogin() {
        TestLoginTask testLoginTask = new TestLoginTask();
        testLoginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadUserInfo() {
        if (UserAPI.isLoggedIn()) {
            UserInfoTask task = new UserInfoTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Consts.Code_Login && resultCode == Activity.RESULT_OK) {
            loadUserInfo();
        }
    }

    MessageTask messageTask;

    private void loadMessages() {
        if (messageTask != null && messageTask.getStatus() == AsyncTask.Status.RUNNING) {
            messageTask.cancel(false);
        }
        messageTask = new MessageTask();
        messageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class MessageTask extends AsyncTask<Void, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(Void... params) {
            return UserAPI.getReminderAndNoticeNum();
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                ReminderNoticeNum num = (ReminderNoticeNum) resultObject.result;
                if (num.getNotice_num() > 0) {
                    noticeView.setVisibility(View.VISIBLE);
                } else {
                    noticeView.setVisibility(View.GONE);
                }
            } else {
                noticeView.setVisibility(View.GONE);
            }
        }
    }

    class TestLoginTask extends AsyncTask<Void, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(Void... params) {
            return UserAPI.testLogin();
        }

        /**
         * 目前尚未添加详细的Error Code
         * 这么写其实不合理，因为有可能TestLogin失败只是因为网络错误或者服务器错误。
         * 只能有两种情况是失败的，一是token过期，另一种是没有token
         * 仅仅以是否有token作为登录状态判断。
         * 应该在某处添加方法，在发现Token过期后将清空token，
         * 应该统一在HttpFetcher里面添加状态操作才对。TODO
         *
         * @param resultObject 返回结果
         */
        @Override
        protected void onPostExecute(ResultObject resultObject) {
            boolean shouldMarkAsFailed = true;//是否视为登录失败
            if (resultObject.ok) {
                shouldMarkAsFailed = false;
            } else {
                int errorID = 0;
                switch (resultObject.code) {
                    case CODE_LOGIN_FAILED:
                        errorID = R.string.login_failed_for_other_reason;
                        break;
                    case CODE_NETWORK_ERROR:
                        errorID = R.string.network_error;
                        shouldMarkAsFailed = false;
                        break;
                    case CODE_JSON_ERROR:
                        errorID = R.string.json_error;
                        shouldMarkAsFailed = false;
                        break;
                    case CODE_UNKNOWN:
                        errorID = R.string.unknown_error;
                        break;
                    case CODE_TOKEN_INVALID:
                        errorID = R.string.token_invalid;
                        break;
                    case CODE_NO_TOKEN:
                        errorID = R.string.have_not_login;
                        break;
                }
                toast(errorID);
            }

            if (!shouldMarkAsFailed) {
                checkChannelList();
                loadUserInfo();
                loginState = true;
            } else {
                loginState = false;
            }
            if (!loginState) {
                back2UnLogged();
            }
        }
    }

    class UserInfoTask extends AsyncTask<String, Intent, ResultObject> {

        @Override
        protected void onPreExecute() {
            String nameString = SharedPreferencesUtil.readString(Consts.Key_User_Name, "");
            if (TextUtils.isEmpty(nameString)) {
                userName.setText(R.string.loading);
            }
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            return UserAPI.getUserInfoByUkey(UserAPI.getUkey());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                setupUserInfo((UserInfo) resultObject.result);
            } else {
                String nameString = SharedPreferencesUtil.readString(Consts.Key_User_Name, "");
                if (TextUtils.isEmpty(nameString)) {
                    userName.setText(R.string.click_to_reload);
                }
            }
        }
    }

    private void setupUserInfo(UserInfo info) {
        SharedPreferencesUtil.saveString(Consts.Key_User_Name, info.getNickname());
        SharedPreferencesUtil.saveString(Consts.Key_User_ID, info.getId());
        SharedPreferencesUtil.saveString(Consts.Key_User_Avatar, info.getAvatar());
        if (Config.shouldLoadImage()) {
            Picasso.with(getActivity()).load(info.getAvatar())
                    .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen)
                    .into(avatarView);
        } else {
            avatarView.setImageResource(R.drawable.default_avatar);
        }
        userName.setText(info.getNickname());
    }
}

package net.nashlegend.sourcewall.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.LoginActivity;
import net.nashlegend.sourcewall.activities.MessageCenterActivity;
import net.nashlegend.sourcewall.activities.MyAnswersActivity;
import net.nashlegend.sourcewall.activities.MyFavorsActivity;
import net.nashlegend.sourcewall.activities.MyPostsActivity;
import net.nashlegend.sourcewall.activities.MyQuestionsActivity;
import net.nashlegend.sourcewall.activities.SettingActivity;
import net.nashlegend.sourcewall.events.LoginStateChangedEvent;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.model.UserInfo;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.MessageAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.data.Config;
import net.nashlegend.sourcewall.data.Consts.Keys;
import net.nashlegend.sourcewall.data.Consts.RequestCode;
import net.nashlegend.sourcewall.util.ImageUtils;
import net.nashlegend.sourcewall.data.Mob;
import net.nashlegend.sourcewall.util.PrefsUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class ProfileFragment extends BaseFragment {


    @BindView(R.id.image_avatar)
    ImageView imageAvatar;
    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.profile_header)
    LinearLayout profileHeader;
    @BindView(R.id.layout_msg_center)
    LinearLayout layoutMsgCenter;
    @BindView(R.id.layout_my_favors)
    LinearLayout layoutMyFavors;
    @BindView(R.id.layout_my_posts)
    LinearLayout layoutMyPosts;
    @BindView(R.id.layout_my_questions)
    LinearLayout layoutMyQuestions;
    @BindView(R.id.view_switch)
    LinearLayout viewSwitchDayNight;
    @BindView(R.id.layout_setting)
    LinearLayout layoutSetting;
    @BindView(R.id.layout_logged_in)
    LinearLayout loginLayout;
    @BindView(R.id.img_msg)
    ImageView imgMsg;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        initView();
        return view;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    private void initView() {
        if (UserAPI.isLoggedIn()) {
            ImageLoader.getInstance().displayImage(UserAPI.getUserAvatar(), imageAvatar, ImageUtils.bigAvatarOptions);
            userName.setText(UserAPI.getName());
            loginLayout.setVisibility(View.VISIBLE);
            loadUserInfo();
        } else {
            imageAvatar.setImageResource(R.drawable.ic_default_avatar_96dp);
            userName.setText(R.string.click_to_login);
            loginLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (UserAPI.isLoggedIn()) {
            checkUnread();
        }
    }

    @OnClick({R.id.layout_msg_center, R.id.layout_my_favors, R.id.layout_my_posts,
            R.id.layout_my_questions, R.id.layout_my_answers,
            R.id.view_switch, R.id.layout_setting, R.id.profile_header})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_header:
                if (!UserAPI.isLoggedIn()) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(intent, RequestCode.Code_Login);
                }
                break;
            case R.id.layout_msg_center:
                startActivity(MessageCenterActivity.class);
                break;
            case R.id.layout_my_favors:
                startActivity(MyFavorsActivity.class);
                break;
            case R.id.layout_my_posts:
                startActivity(MyPostsActivity.class);
                break;
            case R.id.layout_my_questions:
                startActivity(MyQuestionsActivity.class);
                break;
            case R.id.layout_my_answers:
                startActivity(MyAnswersActivity.class);
                break;
            case R.id.view_switch:
                revertMode();
                break;
            case R.id.layout_setting:
                startActivity(SettingActivity.class);
                break;
        }
    }

    private void revertMode() {
        if (App.isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            PrefsUtil.saveBoolean(Keys.Key_Is_Night_Mode, false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            PrefsUtil.saveBoolean(Keys.Key_Is_Night_Mode, true);
        }
        MobclickAgent.onEvent(getActivity(), Mob.Event_Switch_Day_Night_Mode);
        getActivity().recreate();
    }

    private void checkUnread() {
        MessageAPI.getReminderAndNoticeNum(new RequestObject.CallBack<ReminderNoticeNum>() {
            @Override
            public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<ReminderNoticeNum> result) {
                imgMsg.setImageResource(R.drawable.ic_notifications_none_24dp);
            }

            @Override
            public void onSuccess(@NonNull ReminderNoticeNum result, @NonNull ResponseObject<ReminderNoticeNum> detailed) {
                if (result.getNotice_num() > 0) {
                    imgMsg.setImageResource(R.drawable.ic_notifications_active_24dp);
                } else {
                    imgMsg.setImageResource(R.drawable.ic_notifications_none_24dp);
                }
            }
        });
    }

    private void onLogin() {
        ImageLoader.getInstance().displayImage(UserAPI.getUserAvatar(), imageAvatar, ImageUtils.bigAvatarOptions);
        userName.setText(UserAPI.getName());
        loginLayout.setVisibility(View.VISIBLE);
        loadUserInfo();
    }

    private void onLogOut() {
        imageAvatar.setImageResource(R.drawable.ic_default_avatar_96dp);
        userName.setText(R.string.click_to_login);
        loginLayout.setVisibility(View.GONE);
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
                public void onSuccess(@NonNull UserInfo info, @NonNull ResponseObject<UserInfo> detailed) {
                    PrefsUtil.saveString(Keys.Key_User_Name, info.getNickname());
                    PrefsUtil.saveString(Keys.Key_User_ID, info.getId());
                    PrefsUtil.saveString(Keys.Key_User_Avatar, info.getAvatar());
                    if (Config.shouldLoadImage()) {
                        ImageLoader.getInstance().displayImage(info.getAvatar(), imageAvatar, ImageUtils.avatarOptions);
                    } else {
                        imageAvatar.setImageResource(R.drawable.ic_default_avatar_96dp);
                    }
                    userName.setText(info.getNickname());
                }
            });
        }
    }

    public void onEventMainThread(LoginStateChangedEvent e) {
        System.out.println(UserAPI.isLoggedIn());
        if (UserAPI.isLoggedIn()) {
            onLogin();
        } else {
            onLogOut();
        }
    }

    @Override
    public boolean reTap() {
        return false;
    }
}

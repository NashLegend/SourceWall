package net.nashlegend.sourcewall.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.LoginActivity;
import net.nashlegend.sourcewall.activities.SettingActivity;
import net.nashlegend.sourcewall.events.LoginEvent;
import net.nashlegend.sourcewall.events.LogoutEvent;
import net.nashlegend.sourcewall.model.ReminderNoticeNum;
import net.nashlegend.sourcewall.model.UserInfo;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.MessageAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.ImageUtils;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;

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
    @BindView(R.id.layout_my_replies)
    LinearLayout layoutMyReplies;
    @BindView(R.id.layout_my_questions)
    LinearLayout layoutMyQuestions;
    @BindView(R.id.view_switch_to_day)
    LinearLayout viewSwitchToDay;
    @BindView(R.id.view_switch_to_night)
    LinearLayout viewSwitchToNight;
    @BindView(R.id.layout_setting)
    LinearLayout layoutSetting;
    @BindView(R.id.layout_logged_in)
    LinearLayout loginLayout;
    @BindView(R.id.img_msg)
    ImageView imgMsg;

    private boolean currentLoginState = false;
    private String currentUkey = "";

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        currentLoginState = UserAPI.isLoggedIn();
        currentUkey = UserAPI.getUkey();
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
        if (SharedPreferencesUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            viewSwitchToDay.setVisibility(View.VISIBLE);
            viewSwitchToNight.setVisibility(View.GONE);
        } else {
            viewSwitchToDay.setVisibility(View.GONE);
            viewSwitchToNight.setVisibility(View.VISIBLE);
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
            R.id.layout_my_replies, R.id.layout_my_questions, R.id.view_switch_to_day,
            R.id.view_switch_to_night, R.id.layout_setting, R.id.profile_header})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_header:
                if (!UserAPI.isLoggedIn()) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(intent, Consts.Code_Login);
                }
                break;
            case R.id.layout_msg_center:
                break;
            case R.id.layout_my_favors:
                break;
            case R.id.layout_my_posts:
                break;
            case R.id.layout_my_replies:
                break;
            case R.id.layout_my_questions:
                break;
            case R.id.view_switch_to_day:
            case R.id.view_switch_to_night:
                revertMode();
                break;
            case R.id.layout_setting:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
        }
    }

    private void revertMode() {
        SharedPreferencesUtil.saveBoolean(Consts.Key_Is_Night_Mode, !SharedPreferencesUtil.readBoolean(Consts.Key_Is_Night_Mode, false));
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

    private void onLoadUserInfo() {
        // TODO: 16/7/27
        ImageLoader.getInstance().displayImage(UserAPI.getUserAvatar(), imageAvatar, ImageUtils.bigAvatarOptions);
        userName.setText(UserAPI.getName());
        loginLayout.setVisibility(View.VISIBLE);
    }

    private void onLogin() {
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
                    SharedPreferencesUtil.saveString(Consts.Key_User_Name, info.getNickname());
                    SharedPreferencesUtil.saveString(Consts.Key_User_ID, info.getId());
                    SharedPreferencesUtil.saveString(Consts.Key_User_Avatar, info.getAvatar());
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

    public void onEventMainThread(LoginEvent e) {
        onLogin();
    }

    public void onEventMainThread(LogoutEvent e) {
        onLogOut();
    }

}

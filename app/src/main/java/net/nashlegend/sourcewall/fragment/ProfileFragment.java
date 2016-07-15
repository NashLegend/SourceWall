package net.nashlegend.sourcewall.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.LoginActivity;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.ImageUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileFragment extends BaseFragment {


    @BindView(R.id.image_avatar)
    ImageView imageAvatar;
    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.profile_header)
    LinearLayout profileHeader;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        if (UserAPI.isLoggedIn()) {
            ImageLoader.getInstance().displayImage(UserAPI.getUserAvatar(), imageAvatar, ImageUtils.bigAvatarOptions);
            userName.setText(UserAPI.getName());
        } else {
            imageAvatar.setImageResource(R.drawable.ic_default_avatar_96dp);
            userName.setText(R.string.click_to_login);
        }
    }

    @OnClick(R.id.profile_header)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_header:
                if (!UserAPI.isLoggedIn()) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(intent, Consts.Code_Login);
                }
                break;
        }
    }
}

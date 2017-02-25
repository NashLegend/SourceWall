package net.nashlegend.sourcewall.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.data.Config;
import net.nashlegend.sourcewall.data.Consts;
import net.nashlegend.sourcewall.model.UserInfo;
import net.nashlegend.sourcewall.request.SimpleCallBack;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.ImageUtils;
import net.nashlegend.sourcewall.view.common.LoadingView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserActivity extends BaseActivity {
    String userId = null;
    UserInfo user = null;
    @BindView(R.id.action_bar)
    Toolbar toolbar;
    @BindView(R.id.image_avatar)
    ImageView imageAvatar;
    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.question_progress_loading)
    LoadingView mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        userId = getIntent().getStringExtra(Consts.Extras.Extra_User_ID);
        user = getIntent().getParcelableExtra(Consts.Extras.Extra_User);
        if (TextUtils.isEmpty(userId) && user == null) {
            finish();
        } else {
            if (user != null) {
                userId = user.getId();
            }
            mLoadingView.onLoading();
            mLoadingView.setReloadListener(new LoadingView.ReloadListener() {
                @Override
                public void reload() {
                    loadUser();
                }
            });
            loadUser();
        }
    }

    private void loadUser() {
        UserAPI.getUserInfoByID(userId, new SimpleCallBack<UserInfo>() {
            @Override
            public void onFailure() {
                mLoadingView.onFailed();
            }

            @Override
            public void onSuccess(@NonNull UserInfo info) {
                mLoadingView.onSuccess();
                if (Config.shouldLoadImage()) {
                    ImageLoader.getInstance().displayImage(info.getAvatar(), imageAvatar,
                            ImageUtils.avatarOptions);
                } else {
                    imageAvatar.setImageResource(R.drawable.ic_default_avatar_96dp);
                }
                userName.setText(info.getNickname());
            }
        });
    }

    @OnClick({R.id.layout_my_favors, R.id.layout_my_posts, R.id.layout_my_questions, R.id.layout_my_answers})
    public void onClick(View view) {
        // TODO: 2017/2/25  不是自己
        switch (view.getId()) {
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
        }
    }
}

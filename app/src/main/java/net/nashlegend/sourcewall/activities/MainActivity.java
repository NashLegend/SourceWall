package net.nashlegend.sourcewall.activities;

import android.content.Intent;
import android.os.Bundle;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.fragment.ArticlePagerFragment;
import net.nashlegend.sourcewall.fragment.BaseFragment;
import net.nashlegend.sourcewall.fragment.PostPagerFragment;
import net.nashlegend.sourcewall.fragment.ProfileFragment;
import net.nashlegend.sourcewall.fragment.QuestionPagerFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.majiajie.pagerbottomtabstrip.Controller;
import me.majiajie.pagerbottomtabstrip.PagerBottomTabLayout;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectListener;

public class MainActivity extends BaseActivity {

    @BindView(R.id.bottom_bar)
    PagerBottomTabLayout bottomBar;

    BaseFragment crtFragment;
    ArticlePagerFragment articlePagerFragment;
    PostPagerFragment postPagerFragment;
    QuestionPagerFragment questionPagerFragment;
    ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSwipeEnabled(false);
        initPages();
    }

    private void initPages() {
        Controller controller = bottomBar.builder()
                .addTabItem(android.R.drawable.ic_menu_gallery, "科学人")
                .addTabItem(android.R.drawable.ic_menu_manage, "小组")
                .addTabItem(android.R.drawable.ic_search_category_default, "问答")
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

    private BaseFragment getFragmentByPosition(int idx) {
        BaseFragment fragment = null;
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

    @Override
    public void onBackPressed() {
        if (wasTakenOver()) {
            return;
        }
        super.onBackPressed();
    }

    private boolean wasTakenOver() {
        return crtFragment != null && crtFragment.takeOverBackPress();
    }

    public static void open() {
        Intent intent = new Intent(App.getApp(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getApp().startActivity(intent);
    }

    @Override
    public void finish() {
        finish(0, 0);
    }
}

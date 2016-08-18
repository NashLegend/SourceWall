package net.nashlegend.sourcewall.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.fragment.ArticlePagerFragment;
import net.nashlegend.sourcewall.fragment.BaseFragment;
import net.nashlegend.sourcewall.fragment.PostPagerFragment;
import net.nashlegend.sourcewall.fragment.ProfileFragment;
import net.nashlegend.sourcewall.fragment.QuestionPagerFragment;
import net.nashlegend.sourcewall.util.Config;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    BaseFragment crtFragment;
    ArticlePagerFragment articlePagerFragment;
    PostPagerFragment postPagerFragment;
    QuestionPagerFragment questionPagerFragment;
    ProfileFragment profileFragment;
    @BindView(R.id.layout_science)
    LinearLayout layoutScience;
    @BindView(R.id.layout_group)
    LinearLayout layoutGroup;
    @BindView(R.id.layout_questions)
    LinearLayout layoutQuestions;
    @BindView(R.id.layout_me)
    LinearLayout layoutMe;
    ArrayList<View> bars = new ArrayList<>();

    int crtIndex = 0;
    String crtIndexKay = "crtIndex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bars.add(layoutScience);
        bars.add(layoutGroup);
        bars.add(layoutQuestions);
        bars.add(layoutMe);
        setSwipeEnabled(false);

        if (savedInstanceState != null) {
            crtIndex = savedInstanceState.getInt(crtIndexKay, 0);
        } else {
            crtIndex = 0;
        }
        if (crtIndex < 0 && crtIndex > 3) {
            crtIndex = 0;
        }
        onBarClick(bars.get(crtIndex).getId());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(crtIndexKay, crtIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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

    boolean preparingToExit = false;

    @Override
    public void onBackPressed() {
        if (crtFragment != null && crtFragment.takeOverBackPress()) {
            return;
        }

        if (preparingToExit) {
            super.onBackPressed();
        } else {
            preparingToExit = true;
            toastSingleton(R.string.click_again_to_exit);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    preparingToExit = false;
                }
            }, Config.ExitTapsGap);
        }
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

    @OnClick({R.id.layout_science, R.id.layout_group, R.id.layout_questions, R.id.layout_me})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_science:
            case R.id.layout_group:
            case R.id.layout_questions:
            case R.id.layout_me:
                onBarClick(view.getId());
                break;
        }
    }

    private void onBarClick(int id) {
        BaseFragment fragment = null;
        int index = 0;
        switch (id) {
            case R.id.layout_science:
                index = 0;
                break;
            case R.id.layout_group:
                index = 1;
                break;
            case R.id.layout_questions:
                index = 2;
                break;
            case R.id.layout_me:
                index = 3;
                break;
        }
        fragment = getFragmentByPosition(index);
        if (fragment == null || fragment == crtFragment) {
            return;
        } else {
            crtIndex = index;
            crtFragment = fragment;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content, crtFragment).commitAllowingStateLoss();
        }
        for (View bar : bars) {
            if (id == bar.getId()) {
                bar.setSelected(true);
            } else {
                bar.setSelected(false);
            }
        }
    }
}

package net.nashlegend.sourcewall.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.PublishPostActivity;
import net.nashlegend.sourcewall.db.GroupHelper;
import net.nashlegend.sourcewall.db.gen.MyGroup;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.api.PostAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.ChannelHelper;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;
import net.nashlegend.sourcewall.util.SimpleAnimationListener;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.view.common.shuffle.GroupMovableButton;
import net.nashlegend.sourcewall.view.common.shuffle.MovableButton;
import net.nashlegend.sourcewall.view.common.shuffle.ShuffleDeskSimple;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PostPagerFragment extends BaseFragment {
    View layoutView;
    ArrayList<SubItem> subItems = ChannelHelper.getGroupSectionsByUserState();
    @BindView(R.id.post_tabs)
    TabLayout tabLayout;
    @BindView(R.id.post_pager)
    ViewPager viewPager;
    @BindView(R.id.show_more)
    ImageView showMore;
    @BindView(R.id.button_write)
    View btnWrite;

    @BindView(R.id.plastic_scroller)
    ScrollView scrollView;
    @BindView(R.id.layout_more_sections)
    FrameLayout moreSectionsLayout;

    private ShuffleDeskSimple deskSimple;
    private Button manageButton;
    private long currentDBVersion = -1;
    private boolean isMoreSectionsButtonShowing;

    PostPagerAdapter adapter;

    public PostPagerFragment() {
        // Required empty public constructor
    }

    public static PostPagerFragment newInstance() {
        PostPagerFragment fragment = new PostPagerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (layoutView == null) {
            layoutView = inflater.inflate(R.layout.fragment_post_pager, container, false);
            ButterKnife.bind(this, layoutView);
            adapter = new PostPagerAdapter(getFragmentManager());
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

            deskSimple = new ShuffleDeskSimple(getActivity(), scrollView);
            scrollView.addView(deskSimple);
            deskSimple.setOnButtonClickListener(new ShuffleDeskSimple.OnButtonClickListener() {
                @Override
                public void onClick(MovableButton btn) {
                    onSectionButtonClicked((GroupMovableButton) btn);
                }
            });
            ((TextView) deskSimple.findViewById(R.id.tip_of_more_sections)).setText(R.string.tip_of_more_groups);
            manageButton = (Button) deskSimple.findViewById(R.id.button_manage_my_sections);
            manageButton.setText(R.string.reload_all_groups);
            manageButton.setVisibility(View.INVISIBLE);
            manageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reloadFromNet();
                }
            });
            moreSectionsLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (moreSectionsLayout.getHeight() > 0) {
                        moreSectionsLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        moreSectionsLayout.setTranslationY(-moreSectionsLayout.getHeight());
                        moreSectionsLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            if (layoutView.getParent() != null) {
                ((ViewGroup) layoutView.getParent()).removeView(layoutView);
            }
        }
        return layoutView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (UserAPI.isLoggedIn()) {
            showMore.setVisibility(View.VISIBLE);
            btnWrite.setVisibility(View.VISIBLE);
        } else {
            showMore.setVisibility(View.GONE);
            btnWrite.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.show_more, R.id.button_write})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_more:
                toggleShowMore();
                break;
            case R.id.button_write:
                startActivity(PublishPostActivity.class);
                break;
        }
    }

    private void toggleShowMore() {
        if (isMoreSectionsButtonShowing) {
            hideMoreSections();
        } else {
            showMoreSections();
        }
    }

    @Override
    public boolean takeOverBackPress() {
        if (!isVisible()) {
            return false;
        }
        if (isMoreSectionsButtonShowing) {
            hideMoreSections();
            return true;
        }
        return false;
    }

    private void onSectionButtonClicked(GroupMovableButton button) {
        MyGroup myGroup = button.getSection();
        SubItem subItem = new SubItem(myGroup.getSection(), myGroup.getType(), myGroup.getName(), myGroup.getValue());
        hideMoreSections();
        for (int i = 0; i < subItems.size(); i++) {
            if (subItems.get(i).getValue().equals(subItem.getValue())) {
                viewPager.setCurrentItem(i, false);
                tabLayout.getTabAt(i).select();
                break;
            }
        }
    }

    private void initView() {
        deskSimple.InitDatas();
        deskSimple.initView();
    }

    private void resetButtons(List<MyGroup> allSelections) {
        ArrayList<MovableButton> allGroupButtons = new ArrayList<>();
        for (int i = 0; i < allSelections.size(); i++) {
            MyGroup section = allSelections.get(i);
            GroupMovableButton button = new GroupMovableButton(getActivity());
            button.setSection(section);
            allGroupButtons.add(button);
        }
        deskSimple.setButtons(allGroupButtons);
        deskSimple.InitDatas();
        deskSimple.initView();
    }

    private void showMoreSections() {
        if (!isAdded()) {
            return;
        }
        isMoreSectionsButtonShowing = true;
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.cancel();
        }
        animatorSet = new AnimatorSet();
        ObjectAnimator layoutAnimator = ObjectAnimator.ofFloat(moreSectionsLayout, "translationY", moreSectionsLayout.getTranslationY(), 0);
        layoutAnimator.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(moreSectionsLayout, "alpha", 0.0f, 1);
        alphaAnimator.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(showMore, "rotation", showMore.getRotation(), 180);
        imageAnimator.setInterpolator(new DecelerateInterpolator());

        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(layoutAnimator);
        animators.add(imageAnimator);
        animators.add(alphaAnimator);

        animatorSet.addListener(new SimpleAnimationListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isAdded()) {
                    return;
                }
                if (GroupHelper.getMyGroupsNumber() > 0) {
                    long lastDBVersion = SharedPreferencesUtil.readLong(Consts.Key_Last_Post_Groups_Version, 0);
                    if (currentDBVersion != lastDBVersion) {
                        resetButtons(GroupHelper.getAllMyGroups());
                        initView();
                        currentDBVersion = SharedPreferencesUtil.readLong(Consts.Key_Last_Post_Groups_Version, 0);
                    }
                    manageButton.setVisibility(View.VISIBLE);
                } else {
                    manageButton.setVisibility(View.INVISIBLE);
                    popUnloaded();
                }
            }
        });

        animatorSet.playTogether(animators);
        animatorSet.setDuration(400);
        animatorSet.start();
    }

    private void popUnloaded() {
        new AlertDialog
                .Builder(getActivity())
                .setTitle(R.string.hint)
                .setMessage(R.string.ok_to_load_groups)
                .setPositiveButton(R.string.confirm_to_load_my_groups, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reloadFromNet();
                    }
                })
                .setNegativeButton(R.string.use_default_groups, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hideMoreSections();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        hideMoreSections();
                    }
                })
                .create()
                .show();
    }

    private AnimatorSet animatorSet;

    private void hideMoreSections() {
        if (!isAdded()) {
            return;
        }
        isMoreSectionsButtonShowing = false;
        moreSectionsLayout.setVisibility(View.VISIBLE);
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.cancel();
        }
        animatorSet = new AnimatorSet();
        ObjectAnimator layoutAnimator = ObjectAnimator.ofFloat(moreSectionsLayout, "translationY",
                moreSectionsLayout.getTranslationY(), -moreSectionsLayout.getHeight());
        layoutAnimator.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(moreSectionsLayout, "alpha", 1, 0.0f);
        alphaAnimator.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(showMore, "rotation", showMore.getRotation(), 360);
        imageAnimator.setInterpolator(new DecelerateInterpolator());

        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(layoutAnimator);
        animators.add(imageAnimator);
        animators.add(alphaAnimator);
        if (deskSimple.getButtons() != null && deskSimple.getButtons().size() > 0) {
            commitChange(deskSimple.getSortedButtons());
        }
        animatorSet.playTogether(animators);
        animatorSet.setDuration(300);
        animatorSet.start();
    }

    private void commitChange(ArrayList<MovableButton> buttons) {
        boolean changed = false;
        if (subItems.size() == buttons.size() + 2) {
            for (int i = 2; i < subItems.size(); i++) {
                MyGroup myGroup = (MyGroup) buttons.get(i - 2).getSection();
                if (!subItems.get(i).getValue().equals(myGroup.getValue())) {
                    changed = true;
                    break;
                }
            }
            if (!changed) {
                return;
            }
        }

        List<MyGroup> sections = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i++) {
            MyGroup myGroup = (MyGroup) buttons.get(i).getSection();
            myGroup.setOrder(i);
            sections.add(myGroup);
        }
        GroupHelper.putAllMyGroups(sections);
        update();
    }

    private void update() {
        subItems = ChannelHelper.getGroupSectionsByUserState();
        adapter.notifyDataSetChanged();
    }

    ProgressDialog progressDialog;

    private void reloadFromNet() {
        final Subscription subscription =
                PostAPI
                        .getAllMyGroupsAndMerge()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<ArrayList<MyGroup>>() {
                            @Override
                            public void onCompleted() {
                                UiUtil.cancelDialog(progressDialog);
                            }

                            @Override
                            public void onError(Throwable e) {
                                UiUtil.cancelDialog(progressDialog);
                            }

                            @Override
                            public void onNext(ArrayList<MyGroup> myGroups) {
                                UiUtil.cancelDialog(progressDialog);
                                resetButtons(myGroups);
                                initView();
                            }
                        });
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.message_loading_my_groups));
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (!subscription.isUnsubscribed()) {
                    subscription.unsubscribe();
                }
            }
        });
        progressDialog.show();
    }


    class PostPagerAdapter extends FragmentStatePagerAdapter {

        public PostPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return subItems.get(position).getName();
        }

        @Override
        public Fragment getItem(int position) {
            return Posts2Fragment.newInstance(subItems.get(position));
        }

        @Override
        public int getCount() {
            return subItems.size();
        }
    }
}

package net.nashlegend.sourcewall.fragment;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.ShuffleGroupActivity;
import net.nashlegend.sourcewall.db.GroupHelper;
import net.nashlegend.sourcewall.db.gen.MyGroup;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.util.ChannelHelper;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;
import net.nashlegend.sourcewall.view.common.shuffle.GroupMovableButton;
import net.nashlegend.sourcewall.view.common.shuffle.MovableButton;
import net.nashlegend.sourcewall.view.common.shuffle.ShuffleDeskSimple;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostPagerFragment extends BaseFragment {
    View layoutView;
    ArrayList<SubItem> subItems = ChannelHelper.getGroupSectionsByUserState();
    @BindView(R.id.post_tabs)
    TabLayout tabLayout;
    @BindView(R.id.post_pager)
    ViewPager viewPager;
    @BindView(R.id.show_more)
    ImageView showMore;

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
                    if (btn instanceof GroupMovableButton) {
                        onSectionButtonClicked((GroupMovableButton) btn);
                    }
                }
            });
            ((TextView) deskSimple.findViewById(R.id.tip_of_more_sections)).setText(R.string.tip_of_more_groups);
            manageButton = (Button) deskSimple.findViewById(R.id.button_manage_my_sections);
            manageButton.setText(R.string.manage_all_groups);
            manageButton.setVisibility(View.INVISIBLE);
            manageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideMoreSections();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideMoreSections();
                        }
                    }, 320);
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

    @OnClick(R.id.show_more)
    public void toggleShowMore() {
        if (isMoreSectionsButtonShowing) {
            hideMoreSections();
        } else {
            showMoreSections();
        }
    }

    public boolean takeOverBackPressed() {
        // TODO: 16/7/14
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

    private void getButtons() {
        List<MyGroup> allSelections = GroupHelper.getAllMyGroups();
        ArrayList<MovableButton> allGroupButtons = new ArrayList<>();
        for (int i = 0; i < allSelections.size(); i++) {
            MyGroup section = allSelections.get(i);
            GroupMovableButton button = new GroupMovableButton(getActivity());
            button.setSection(section);
            allGroupButtons.add(button);
        }
        deskSimple.setButtons(allGroupButtons);
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
        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(showMore, "rotation", showMore.getRotation(), 180);
        imageAnimator.setInterpolator(new DecelerateInterpolator());

        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(layoutAnimator);
        animators.add(imageAnimator);

        animatorSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isAdded()) {
                    if (GroupHelper.getMyGroupsNumber() > 0) {
                        long lastDBVersion = SharedPreferencesUtil.readLong(Consts.Key_Last_Post_Groups_Version, 0);
                        if (currentDBVersion != lastDBVersion) {
                            getButtons();
                            initView();
                            currentDBVersion = SharedPreferencesUtil.readLong(Consts.Key_Last_Post_Groups_Version, 0);
                        }
                        manageButton.setVisibility(View.VISIBLE);
                    } else {
                        manageButton.setVisibility(View.INVISIBLE);
                        new AlertDialog
                                .Builder(getActivity())
                                .setTitle(R.string.hint)
                                .setMessage(R.string.ok_to_load_groups)
                                .setPositiveButton(R.string.confirm_to_load_my_groups, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        hideMoreSections();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(getActivity(), ShuffleGroupActivity.class);
                                                intent.putExtra(Consts.Extra_Should_Load_Before_Shuffle, true);
                                                startActivityForResult(intent, Consts.Code_Start_Shuffle_Groups);
                                                getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
                                            }
                                        }, 320);
                                    }
                                }).setNegativeButton(R.string.use_default_groups, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                hideMoreSections();
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                hideMoreSections();
                            }
                        })
                                .create()
                                .show();
                    }
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

        });

        animatorSet.playTogether(animators);
        animatorSet.setDuration(400);
        animatorSet.start();
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
        ObjectAnimator layoutAnimator = ObjectAnimator
                .ofFloat(moreSectionsLayout, "translationY", moreSectionsLayout.getTranslationY(), -moreSectionsLayout.getHeight());
        layoutAnimator.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(showMore, "rotation", showMore.getRotation(), 360);
        imageAnimator.setInterpolator(new DecelerateInterpolator());

        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(layoutAnimator);
        animators.add(imageAnimator);
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
                MyGroup myGroup = (MyGroup) buttons.get(i-2).getSection();
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

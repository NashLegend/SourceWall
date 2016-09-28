package net.nashlegend.sourcewall.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
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

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.BaseActivity;
import net.nashlegend.sourcewall.activities.PublishPostActivity;
import net.nashlegend.sourcewall.activities.SearchActivity;
import net.nashlegend.sourcewall.adapters.FakeFragmentStatePagerAdapter;
import net.nashlegend.sourcewall.data.ChannelHelper;
import net.nashlegend.sourcewall.data.Consts.Extras;
import net.nashlegend.sourcewall.data.Consts.Keys;
import net.nashlegend.sourcewall.data.Consts.RequestCode;
import net.nashlegend.sourcewall.data.database.GroupHelper;
import net.nashlegend.sourcewall.data.database.gen.MyGroup;
import net.nashlegend.sourcewall.events.Emitter;
import net.nashlegend.sourcewall.events.GroupFetchedEvent;
import net.nashlegend.sourcewall.events.LoginStateChangedEvent;
import net.nashlegend.sourcewall.events.ShowHideEvent;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.api.PostAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.simple.SimpleAnimationListener;
import net.nashlegend.sourcewall.util.PrefsUtil;
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
    @BindView(R.id.layout_operation)
    FloatingActionsMenu fabMenu;

    @BindView(R.id.plastic_scroller)
    ScrollView scrollView;
    @BindView(R.id.layout_more_sections)
    FrameLayout moreSectionsLayout;

    private ShuffleDeskSimple deskSimple;
    private Button manageButton;
    private long currentDBVersion = -1;
    private boolean isMoreSectionsButtonShowing;

    PostPagerAdapter adapter;

    public static boolean shouldNotifyDataSetChanged = false;

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
        Emitter.register(this);
        if (layoutView == null) {
            layoutView = inflater.inflate(R.layout.fragment_post_pager, container, false);
            ButterKnife.bind(this, layoutView);
            adapter = new PostPagerAdapter(getFragmentManager());
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);

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
        if (shouldNotifyDataSetChanged) {
            update();
        }
        return layoutView;
    }

    @Override
    public void onDestroyView() {
        Emitter.unregister(this);
        super.onDestroyView();
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

    @OnClick({R.id.show_more, R.id.button_write, R.id.button_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_more:
                toggleShowMore();
                break;
            case R.id.button_write:
                if (UserAPI.isLoggedIn()) {
                    Intent intent = new Intent(getActivity(), PublishPostActivity.class);
                    if (viewPager.getCurrentItem() >= subItems.size()) {
                        return;
                    }
                    SubItem item = subItems.get(viewPager.getCurrentItem());
                    if (item.getType() == SubItem.Type_Single_Channel) {
                        intent.putExtra(Extras.Extra_SubItem, item);
                    }
                    startActivityForResult(intent, RequestCode.Code_Publish_Post);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
                } else {
                    ((BaseActivity) getActivity()).gotoLogin();
                }
                break;
            case R.id.button_search:
                startActivity(SearchActivity.class);
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
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.select();
                }
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
        layoutAnimator.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(moreSectionsLayout, "alpha", 0.0f, 1);
        alphaAnimator.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(showMore, "rotation", showMore.getRotation(), 180);
        imageAnimator.setInterpolator(new FastOutSlowInInterpolator());

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
                    long lastDBVersion = PrefsUtil.readLong(Keys.Key_Last_Post_Groups_Version, 0);
                    if (currentDBVersion != lastDBVersion) {
                        resetButtons(GroupHelper.getAllMyGroups());
                        initView();
                        currentDBVersion = PrefsUtil.readLong(Keys.Key_Last_Post_Groups_Version, 0);
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
        layoutAnimator.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(moreSectionsLayout, "alpha", 1, 0.0f);
        alphaAnimator.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(showMore, "rotation", showMore.getRotation(), 360);
        imageAnimator.setInterpolator(new FastOutSlowInInterpolator());

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
        if (adapter == null) {
            return;
        }

        SubItem subItem = null;
        if (viewPager.getCurrentItem() < subItems.size()) {
            subItem = subItems.get(viewPager.getCurrentItem());
        }

        subItems = ChannelHelper.getGroupSectionsByUserState();
        adapter.notifyDataSetChanged();
        shouldNotifyDataSetChanged = false;

        if (subItem != null) {
            for (int i = 0; i < subItems.size(); i++) {
                if (subItems.get(i).getValue().equals(subItem.getValue())) {
                    viewPager.setCurrentItem(i, false);
                    break;
                }
            }
        }

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                TabLayout.Tab tab = tabLayout.getTabAt(viewPager.getCurrentItem());
                if (tab != null) {
                    tab.select();
                }
            }
        });
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
                                UiUtil.dismissDialog(progressDialog);
                            }

                            @Override
                            public void onError(Throwable e) {
                                UiUtil.dismissDialog(progressDialog);
                            }

                            @Override
                            public void onNext(ArrayList<MyGroup> myGroups) {
                                UiUtil.dismissDialog(progressDialog);
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

    public void onEventMainThread(GroupFetchedEvent event) {
        if (!isAdded()) {
            return;
        }
        update();
    }


    public void onEventMainThread(LoginStateChangedEvent e) {
        if (!isAdded()) {
            return;
        }
        update();
    }

    @Override
    public boolean reTap() {
        if (adapter == null) {
            return false;
        }
        Fragment fragment = adapter.getFragmentAt(viewPager.getCurrentItem());
        return fragment instanceof PostsFragment && ((PostsFragment) fragment).reTap();
    }

    public void onEventMainThread(ShowHideEvent event) {
        if (event.section == SubItem.Section_Post) {
            if (event.show) {
                showSearch();
            } else {
                hideSearch();
            }
        }
    }

    ObjectAnimator hideAnimator;
    ObjectAnimator showAnimator;

    private void hideSearch() {
        UiUtil.dismissAnimator(showAnimator);
        if (hideAnimator != null && hideAnimator.isRunning()) {
            return;
        }
        fabMenu.collapse();
        hideAnimator = ObjectAnimator.ofFloat(fabMenu, "translationY", fabMenu.getTranslationY(), fabMenu.getHeight());
        hideAnimator.setInterpolator(new AccelerateInterpolator(1.3f));
        hideAnimator.setDuration(400);
        hideAnimator.start();
    }

    private void showSearch() {
        UiUtil.dismissAnimator(hideAnimator);
        if (showAnimator != null && showAnimator.isRunning()) {
            return;
        }
        showAnimator = ObjectAnimator.ofFloat(fabMenu, "translationY", fabMenu.getTranslationY(), 0);
        showAnimator.setInterpolator(new DecelerateInterpolator());
        showAnimator.setDuration(400);
        showAnimator.start();
    }

    class PostPagerAdapter extends FakeFragmentStatePagerAdapter {

        PostPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return subItems.get(position).getName();
        }

        @Override
        public Fragment getItem(int position) {
            return PostsFragment.newInstance(subItems.get(position));
        }

        @Override
        public int getCount() {
            return subItems.size();
        }
    }
}

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
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.SearchActivity;
import net.nashlegend.sourcewall.adapters.FakeFragmentStatePagerAdapter;
import net.nashlegend.sourcewall.data.ChannelHelper;
import net.nashlegend.sourcewall.data.Consts.Keys;
import net.nashlegend.sourcewall.data.database.AskTagHelper;
import net.nashlegend.sourcewall.data.database.gen.AskTag;
import net.nashlegend.sourcewall.events.Emitter;
import net.nashlegend.sourcewall.events.ShowHideEvent;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.api.QuestionAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.simple.SimpleAnimationListener;
import net.nashlegend.sourcewall.util.PrefsUtil;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.view.common.shuffle.AskTagMovableButton;
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

public class QuestionPagerFragment extends BaseFragment {
    View layoutView;
    ArrayList<SubItem> subItems = ChannelHelper.getQuestionSectionsByUserState();
    @BindView(R.id.question_tabs)
    TabLayout tabLayout;
    @BindView(R.id.question_pager)
    ViewPager viewPager;
    @BindView(R.id.show_more)
    ImageView showMore;
    @BindView(R.id.button_search)
    View searchButton;

    @BindView(R.id.plastic_scroller)
    ScrollView scrollView;
    @BindView(R.id.layout_more_sections)
    FrameLayout moreSectionsLayout;

    private ShuffleDeskSimple deskSimple;
    private Button manageButton;
    private long currentDBVersion = -1;
    private boolean isMoreSectionsButtonShowing;

    QuestionPagerAdapter adapter;

    public QuestionPagerFragment() {
        // Required empty public constructor
    }

    public static QuestionPagerFragment newInstance() {
        QuestionPagerFragment fragment = new QuestionPagerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Emitter.register(this);
        if (layoutView == null) {
            layoutView = inflater.inflate(R.layout.fragment_question_pager, container, false);
            ButterKnife.bind(this, layoutView);
            adapter = new QuestionPagerAdapter(getFragmentManager());
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);

            deskSimple = new ShuffleDeskSimple(getActivity(), scrollView);
            scrollView.addView(deskSimple);
            deskSimple.setOnButtonClickListener(new ShuffleDeskSimple.OnButtonClickListener() {
                @Override
                public void onClick(MovableButton btn) {
                    onSectionButtonClicked((AskTagMovableButton) btn);
                }
            });
            ((TextView) deskSimple.findViewById(R.id.tip_of_more_sections)).setText(
                    R.string.tip_of_more_tags);
            manageButton = (Button) deskSimple.findViewById(R.id.button_manage_my_sections);
            manageButton.setText(R.string.reload_all_tags);
            manageButton.setVisibility(View.INVISIBLE);
            manageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reloadFromNet();
                }
            });
            moreSectionsLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (moreSectionsLayout.getHeight() > 0) {
                                moreSectionsLayout.getViewTreeObserver()
                                        .removeGlobalOnLayoutListener(
                                        this);
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
    public void onDestroyView() {
        Emitter.unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (UserAPI.isLoggedIn()) {
            showMore.setVisibility(View.VISIBLE);
        } else {
            showMore.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.show_more, R.id.button_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_more:
                toggleShowMore();
                break;
            case R.id.button_search:
                startActivity(SearchActivity.class);
                break;
        }
    }

    public void toggleShowMore() {
        if (isMoreSectionsButtonShowing) {
            hideMoreSections();
        } else {
            showMoreSections();
        }
    }

    @Override
    public boolean takeOverBackPress() {
        if (isMoreSectionsButtonShowing) {
            hideMoreSections();
            return true;
        }
        return false;
    }

    private void onSectionButtonClicked(AskTagMovableButton button) {
        AskTag askTag = button.getSection();
        SubItem subItem = new SubItem(askTag.getSection(), askTag.getType(), askTag.getName(),
                askTag.getValue());
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

    private void resetButtons(List<AskTag> askTagList) {
        ArrayList<MovableButton> allTagButtons = new ArrayList<>();
        for (int i = 0; i < askTagList.size(); i++) {
            AskTag section = askTagList.get(i);
            AskTagMovableButton button = new AskTagMovableButton(getActivity());
            button.setSection(section);
            allTagButtons.add(button);
        }
        deskSimple.setButtons(allTagButtons);
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
        ObjectAnimator layoutAnimator = ObjectAnimator.ofFloat(moreSectionsLayout, "translationY",
                moreSectionsLayout.getTranslationY(), 0);
        layoutAnimator.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(moreSectionsLayout, "alpha", 0.0f, 1);
        alphaAnimator.setInterpolator(new FastOutSlowInInterpolator());

        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(showMore, "rotation",
                showMore.getRotation(), 180);
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
                if (AskTagHelper.getAskTagsNumber() > 0) {
                    long lastDBVersion = PrefsUtil.readLong(Keys.Key_Last_Ask_Tags_Version, 0);
                    if (currentDBVersion != lastDBVersion) {
                        resetButtons(AskTagHelper.getAllMyTags());
                        initView();
                        currentDBVersion = PrefsUtil.readLong(Keys.Key_Last_Ask_Tags_Version, 0);
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
                .setMessage(R.string.ok_to_load_tags)
                .setPositiveButton(R.string.confirm_to_load_my_tags,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reloadFromNet();
                            }
                        })
                .setNegativeButton(R.string.use_default_tags,
                        new DialogInterface.OnClickListener() {
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

        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(showMore, "rotation",
                showMore.getRotation(), 360);
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
                AskTag tag = (AskTag) buttons.get(i - 2).getSection();
                if (!subItems.get(i).getValue().equals(tag.getValue())) {
                    changed = true;
                    break;
                }
            }
            if (!changed) {
                return;
            }
        }

        List<AskTag> sections = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i++) {
            AskTag tag = (AskTag) buttons.get(i).getSection();
            tag.setOrder(i);
            sections.add(tag);
        }
        AskTagHelper.putAllMyTags(sections);
        update();
    }

    private void update() {
        SubItem subItem = null;
        if (viewPager.getCurrentItem() < subItems.size()) {
            subItem = subItems.get(viewPager.getCurrentItem());
        }

        subItems = ChannelHelper.getQuestionSectionsByUserState();
        adapter.notifyDataSetChanged();

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
                QuestionAPI
                        .getAllMyTagsAndMerge()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<ArrayList<AskTag>>() {
                            @Override
                            public void onCompleted() {
                                UiUtil.dismissDialog(progressDialog);
                            }

                            @Override
                            public void onError(Throwable e) {
                                UiUtil.dismissDialog(progressDialog);
                            }

                            @Override
                            public void onNext(ArrayList<AskTag> askTagList) {
                                UiUtil.dismissDialog(progressDialog);
                                resetButtons(askTagList);
                                initView();
                            }
                        });
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.message_wait_a_minute));
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

    @Override
    public boolean reTap() {
        if (adapter == null) {
            return false;
        }
        Fragment fragment = adapter.getFragmentAt(viewPager.getCurrentItem());
        return fragment instanceof QuestionsFragment && ((QuestionsFragment) fragment).reTap();
    }

    public void onEventMainThread(ShowHideEvent event) {
        if (event.section == SubItem.Section_Question) {
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
        hideAnimator = ObjectAnimator.ofFloat(searchButton, "translationY",
                searchButton.getTranslationY(), searchButton.getHeight());
        hideAnimator.setDuration(300);
        hideAnimator.start();
    }

    private void showSearch() {
        UiUtil.dismissAnimator(hideAnimator);
        if (showAnimator != null && showAnimator.isRunning()) {
            return;
        }
        showAnimator = ObjectAnimator.ofFloat(searchButton, "translationY",
                searchButton.getTranslationY(), 0);
        showAnimator.setDuration(300);
        showAnimator.start();
    }

    class QuestionPagerAdapter extends FakeFragmentStatePagerAdapter {

        QuestionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return subItems.get(position).getName();
        }

        @Override
        public Fragment getItem(int position) {
            return QuestionsFragment.newInstance(subItems.get(position));
        }

        @Override
        public int getCount() {
            return subItems.size();
        }
    }
}

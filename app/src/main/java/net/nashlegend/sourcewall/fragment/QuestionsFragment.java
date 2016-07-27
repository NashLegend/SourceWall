package net.nashlegend.sourcewall.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.activities.BaseActivity;
import net.nashlegend.sourcewall.activities.MainActivity;
import net.nashlegend.sourcewall.activities.PublishPostActivity;
import net.nashlegend.sourcewall.activities.QuestionActivity;
import net.nashlegend.sourcewall.activities.ShuffleTagActivity;
import net.nashlegend.sourcewall.adapters.QuestionAdapter;
import net.nashlegend.sourcewall.db.AskTagHelper;
import net.nashlegend.sourcewall.db.gen.AskTag;
import net.nashlegend.sourcewall.events.OpenContentFragmentEvent;
import net.nashlegend.sourcewall.model.Question;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.QuestionAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.PrefsUtil;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.view.QuestionListItemView;
import net.nashlegend.sourcewall.view.common.LListView;
import net.nashlegend.sourcewall.view.common.LoadingView;
import net.nashlegend.sourcewall.view.common.shuffle.AskTagMovableButton;
import net.nashlegend.sourcewall.view.common.shuffle.MovableButton;
import net.nashlegend.sourcewall.view.common.shuffle.ShuffleDeskSimple;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class QuestionsFragment extends ChannelsFragment implements LListView.OnRefreshListener, LoadingView.ReloadListener, AdapterView.OnItemClickListener {
    private final String HOTTEST = "hottest";
    private final String HIGHLIGHT = "highlight";

    View layoutView;
    @BindView(R.id.list_questions)
    LListView listView;
    @BindView(R.id.questions_loading)
    ProgressBar progressBar;
    @BindView(R.id.question_progress_loading)
    LoadingView loadingView;
    @BindView(R.id.plastic_scroller)
    ScrollView scrollView;
    @BindView(R.id.layout_more_sections)
    FrameLayout moreSectionsLayout;

    private QuestionAdapter adapter;
    private SubItem subItem;
    private int currentPage = -1;//page从0开始，-1表示还没有数据
    private View headerView;
    private ShuffleDeskSimple deskSimple;
    private Button manageButton;
    private long currentDBVersion = -1;
    private final int Code_Publish_Question = 1055;
    private final int cacheDuration = 300;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (layoutView == null) {
            layoutView = inflater.inflate(R.layout.fragment_questions, container, false);
            ButterKnife.bind(this, layoutView);
            loadingView.setReloadListener(this);
            subItem = getArguments().getParcelable(Consts.Extra_SubItem);
            headerView = inflater.inflate(R.layout.layout_header_load_pre_page, null, false);
            adapter = new QuestionAdapter(getActivity());
            listView.setAdapter(adapter);
            listView.setOnRefreshListener(this);
            listView.setOnItemClickListener(this);
            listView.addHeaderView(headerView);
            headerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (headerView.getLayoutParams() != null) {
                        headerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        hideHeader();
                    }
                }
            });
            headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadPrePage();
                }
            });
            //防止滑动headerView的时候下拉上拉
            headerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            listView.requestDisallowInterceptTouchEvent(true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            listView.requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                    return false;
                }
            });

            deskSimple = new ShuffleDeskSimple(getActivity(), scrollView);
            scrollView.addView(deskSimple);
            deskSimple.setOnButtonClickListener(new ShuffleDeskSimple.OnButtonClickListener() {
                @Override
                public void onClick(MovableButton btn) {
                    if (btn instanceof AskTagMovableButton) {
                        onSectionButtonClicked((AskTagMovableButton) btn);
                    }
                }
            });
            ((TextView) deskSimple.findViewById(R.id.tip_of_more_sections)).setText(R.string.tip_of_more_tags);
            manageButton = (Button) deskSimple.findViewById(R.id.button_manage_my_sections);
            manageButton.setText(R.string.manage_all_tags);
            manageButton.setVisibility(View.INVISIBLE);
            manageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideMoreSections();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MobclickAgent.onEvent(getActivity(), Mob.Event_Load_My_Tags);
                            Intent intent = new Intent(getActivity(), ShuffleTagActivity.class);
                            startActivityForResult(intent, Consts.Code_Start_Shuffle_Ask_Tags);
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
            setTitle();
            loadOver();
        } else {
            if (layoutView.getParent() != null) {
                ((ViewGroup) layoutView.getParent()).removeView(layoutView);
            }
            onCreateViewAgain();
        }
        return layoutView;
    }

    public void onCreateViewAgain() {
        SubItem mSubItem = getArguments().getParcelable(Consts.Extra_SubItem);
        resetData(mSubItem);
    }

    boolean User_Has_Learned_Load_My_Tags = false;

    private void checkUserLearnedLoadTags() {
        if (User_Has_Learned_Load_My_Tags) {
            return;
        }
        if (!PrefsUtil.readBoolean(Consts.Key_User_Has_Learned_Load_My_Tags, false)) {
            PrefsUtil.saveBoolean(Consts.Key_User_Has_Learned_Load_My_Tags, true);
            User_Has_Learned_Load_My_Tags = true;
            AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.hint).setMessage(R.string.hint_of_load_my_tags).setPositiveButton(R.string.ok_i_know, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create();
            dialog.show();
        }
    }

    private void onSectionButtonClicked(AskTagMovableButton button) {
        AskTag askTag = button.getSection();
        SubItem subItem = new SubItem(askTag.getSection(), askTag.getType(), askTag.getName(), askTag.getValue());
        EventBus.getDefault().post(new OpenContentFragmentEvent(subItem));
        hideMoreSections();
    }

    private ImageView moreSectionsImageView;
    private boolean isMoreSectionsButtonShowing;
    private AnimatorSet animatorSet;

    private void hideMoreSections() {
        if (!isAdded()) {
            return;
        }
        setTitle();
        isMoreSectionsButtonShowing = false;
        moreSectionsLayout.setVisibility(View.VISIBLE);
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.cancel();
        }
        animatorSet = new AnimatorSet();
        ObjectAnimator layoutAnimator = ObjectAnimator.ofFloat(moreSectionsLayout, "translationY", moreSectionsLayout.getTranslationY(), -moreSectionsLayout.getHeight());
        layoutAnimator.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(moreSectionsImageView, "rotation", moreSectionsImageView.getRotation(), 360);
        imageAnimator.setInterpolator(new DecelerateInterpolator());

        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(layoutAnimator);
        animators.add(imageAnimator);

        animatorSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                moreSectionsImageView.setRotation(0);
                if (deskSimple.getButtons() != null && deskSimple.getButtons().size() > 0) {
                    commitChange(deskSimple.getSortedButtons());
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
        animatorSet.setDuration(300);
        animatorSet.start();
    }


    private void initView() {
        deskSimple.InitDatas();
        deskSimple.initView();
    }

    private List<AskTag> unselectedSections;

    private void getButtons() {
        unselectedSections = AskTagHelper.getUnselectedTags();
        ArrayList<MovableButton> unselectedButtons = new ArrayList<>();
        for (int i = 0; i < unselectedSections.size(); i++) {
            AskTag section = unselectedSections.get(i);
            AskTagMovableButton button = new AskTagMovableButton(getActivity());
            button.setSection(section);
            unselectedButtons.add(button);
        }
        deskSimple.setButtons(unselectedButtons);
    }

    private void showMoreSections() {
        if (!isAdded()) {
            return;
        }
        getActivity().setTitle(R.string.more_tags);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.more_tags);
        isMoreSectionsButtonShowing = true;
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.cancel();
        }
        animatorSet = new AnimatorSet();
        ObjectAnimator layoutAnimator = ObjectAnimator.ofFloat(moreSectionsLayout, "translationY", moreSectionsLayout.getTranslationY(), 0);
        layoutAnimator.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(moreSectionsImageView, "rotation", moreSectionsImageView.getRotation(), 180);
        imageAnimator.setInterpolator(new DecelerateInterpolator());

        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(layoutAnimator);
        animators.add(imageAnimator);

        animatorSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isAdded()) {
                    if (AskTagHelper.getAskTagsNumber() > 0) {
                        long lastDBVersion = PrefsUtil.readLong(Consts.Key_Last_Ask_Tags_Version, 0);
                        if (currentDBVersion != lastDBVersion) {
                            getButtons();
                            initView();
                            currentDBVersion = PrefsUtil.readLong(Consts.Key_Last_Ask_Tags_Version, 0);
                        }
                        manageButton.setVisibility(View.VISIBLE);
                    } else {
                        manageButton.setVisibility(View.INVISIBLE);
                        AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.hint).setMessage(R.string.ok_to_load_tags).setPositiveButton(R.string.confirm_to_load_my_tags, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                hideMoreSections();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(getActivity(), ShuffleTagActivity.class);
                                        intent.putExtra(Consts.Extra_Should_Load_Before_Shuffle, true);
                                        startActivityForResult(intent, Consts.Code_Start_Shuffle_Ask_Tags);
                                    }
                                }, 320);
                            }
                        }).setNegativeButton(R.string.use_default_tags, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                hideMoreSections();
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                hideMoreSections();
                            }
                        }).create();
                        dialog.show();
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

    private void commitChange(ArrayList<MovableButton> buttons) {
        List<AskTag> sections = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i++) {
            AskTag AskTag = (AskTag) buttons.get(i).getSection();
            if (!AskTag.getSelected()) {
                AskTag.setOrder(1024 + AskTag.getOrder());
            }
            sections.add(AskTag);
        }
        AskTagHelper.putUnselectedTags(sections);
    }

    @Override
    public void setTitle() {
        getActivity().setTitle(this.subItem.getName() + " -- 问答");
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(this.subItem.getName() + " -- 问答");
    }

    private void loadOver() {
        loadData(0);
        loadingView.startLoading();
    }

    private void loadData(int offset) {
        if (offset < 0) {
            offset = 0;
        }
        try {
            loadQuestions(offset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void loadPrePage() {
        listView.setCanPullToLoadMore(false);
        listView.setCanPullToRefresh(false);
        headerView.findViewById(R.id.text_header_load_hint).setVisibility(View.INVISIBLE);
        headerView.findViewById(R.id.progress_header_loading).setVisibility(View.VISIBLE);
        loadData(currentPage - 1);
    }

    private void writeAsk() {
        if (UserAPI.isLoggedIn()) {
            Intent intent = new Intent(getActivity(), PublishPostActivity.class);
            intent.putExtra(Consts.Extra_SubItem, subItem);
            startActivityForResult(intent, Consts.Code_Publish_Question);
        } else {
            ((BaseActivity) getActivity()).notifyNeedLog();
        }
    }


    /**
     * 有可能LoaderTask已经结束但是Header仍然没有layoutParams，下同
     * 不过不用担心，只会出现在刚刚进入的时候，这时不设置Height,getViewTreeObserver也会很快将其隐藏的
     */
    private void hideHeader() {
        if (headerView.getLayoutParams() != null) {
            headerView.getLayoutParams().height = 1;
        }
        headerView.setVisibility(View.GONE);
    }

    private void showHeader() {
        if (headerView.getLayoutParams() != null) {
            headerView.getLayoutParams().height = 0;
        }
        headerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStartRefresh() {
        hideHeader();
        loadData(0);
    }

    @Override
    public void onStartLoadMore() {
        loadData(currentPage + 1);
    }

    @Override
    public int getFragmentMenu() {
        return R.menu.menu_fragment_question;
    }

    @Override
    public boolean takeOverMenuInflate(MenuInflater inflater, Menu menu) {
        try {
            inflater.inflate(getFragmentMenu(), menu);
            if (!UserAPI.isLoggedIn()) {
                menu.findItem(R.id.action_more_sections).setVisible(false);
            } else {
                checkUserLearnedLoadTags();
                moreSectionsImageView = (ImageView) ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.action_view_more_sections, null);
                MenuItemCompat.setActionView(menu.findItem(R.id.action_more_sections), moreSectionsImageView);
                moreSectionsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isMoreSectionsButtonShowing) {
                            hideMoreSections();
                        } else {
                            showMoreSections();
                        }
                    }
                });
                if (isMoreSectionsButtonShowing) {
                    moreSectionsImageView.setRotation(180);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public boolean takeOverOptionsItemSelect(MenuItem item) {
        return true;
    }

    @Override
    public boolean takeOverBackPressed() {
        if (isMoreSectionsButtonShowing) {
            hideMoreSections();
            return true;
        }
        return false;
    }

    @Override
    public void resetData(SubItem subItem) {
        if (subItem.equals(this.subItem)) {
            loadingView.onLoadSuccess();
            if (adapter == null || adapter.getCount() == 0) {
                triggerRefresh();
            }
        } else {
            currentPage = -1;
            this.subItem = subItem;
            adapter.clear();
            adapter.notifyDataSetInvalidated();
            listView.setCanPullToRefresh(false);
            listView.setCanPullToLoadMore(false);
            hideHeader();
            loadOver();
        }
        if (isMoreSectionsButtonShowing) {
            hideMoreSections();
        }
        setTitle();
    }

    @Override
    public void triggerRefresh() {
        listView.startRefreshing();
    }

    @Override
    public void prepareLoading(SubItem sub) {
        if (sub == null || !sub.equals(this.subItem)) {
            loadingView.startLoading();
        }
    }

    @Override
    public void scrollToHead() {
        listView.setSelection(0);
    }

    @Override
    public void reload() {
        loadData(0);
    }

    private void loadQuestions(final int loadedPage) throws UnsupportedEncodingException {
        boolean useCache = loadedPage == 0 && adapter.getList().size() == 0;
        Observable<ResponseObject<ArrayList<Question>>> observable;
        if (subItem.getType() == SubItem.Type_Collections) {
            if (HOTTEST.equals(subItem.getValue())) {
                observable = QuestionAPI.getHotQuestions(loadedPage * 20, useCache);
            } else {
                observable = QuestionAPI.getHighlightQuestions(loadedPage + 1, useCache);
            }
        } else {
            observable = QuestionAPI.getQuestionsByTag(subItem.getValue(), loadedPage * 20, useCache);
        }
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        listView.doneOperation();
                        loadingView.onLoadSuccess();
                        if (currentPage > 0) {
                            showHeader();
                        } else {
                            hideHeader();
                        }
                        if (adapter.getCount() > 0) {
                            listView.setCanPullToLoadMore(true);
                            listView.setCanPullToRefresh(true);
                        } else {
                            listView.setCanPullToLoadMore(false);
                            listView.setCanPullToRefresh(true);
                        }
                    }
                })
                .subscribe(new Observer<ResponseObject<ArrayList<Question>>>() {
                    @Override
                    public void onCompleted() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseObject<ArrayList<Question>> result) {
                        if (result.isCached && loadedPage == 0) {
                            if (result.ok) {
                                ArrayList<Question> ars = result.result;
                                if (ars.size() > 0) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    loadingView.onLoadSuccess();
                                    adapter.setList(ars);
                                    adapter.notifyDataSetInvalidated();
                                }
                            }
                        } else {
                            listView.doneOperation();
                            progressBar.setVisibility(View.GONE);
                            if (result.ok) {
                                loadingView.onLoadSuccess();
                                ArrayList<Question> ars = result.result;
                                if (ars.size() > 0) {
                                    currentPage = loadedPage;
                                    adapter.setList(ars);
                                    adapter.notifyDataSetChanged();
                                    listView.setSelection(0);
                                } else {
                                    toast("没有加载到数据");
                                }
                            } else {
                                toast(R.string.load_failed);
                                loadingView.onLoadFailed();
                            }
                            if (currentPage > 0) {
                                showHeader();
                            } else {
                                hideHeader();
                            }
                            if (adapter.getCount() > 0) {
                                listView.setCanPullToLoadMore(true);
                                listView.setCanPullToRefresh(true);
                            } else {
                                listView.setCanPullToLoadMore(false);
                                listView.setCanPullToRefresh(true);
                            }
                            headerView.findViewById(R.id.text_header_load_hint).setVisibility(View.VISIBLE);
                            headerView.findViewById(R.id.progress_header_loading).setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (UiUtil.shouldThrottle()) {
            return;
        }
        if (view instanceof QuestionListItemView) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), QuestionActivity.class);
            intent.putExtra(Consts.Extra_Question, ((QuestionListItemView) view).getData());
            startActivity(intent);
        }
    }
}

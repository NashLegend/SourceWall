package net.nashlegend.sourcewall.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.BaseActivity;
import net.nashlegend.sourcewall.MainActivity;
import net.nashlegend.sourcewall.PostActivity;
import net.nashlegend.sourcewall.PublishPostActivity;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.ShuffleGroupActivity;
import net.nashlegend.sourcewall.adapters.PostAdapter;
import net.nashlegend.sourcewall.commonview.AAsyncTask;
import net.nashlegend.sourcewall.commonview.IStackedAsyncTaskInterface;
import net.nashlegend.sourcewall.commonview.LListView;
import net.nashlegend.sourcewall.commonview.LoadingView;
import net.nashlegend.sourcewall.commonview.shuffle.GroupMovableButton;
import net.nashlegend.sourcewall.commonview.shuffle.MovableButton;
import net.nashlegend.sourcewall.commonview.shuffle.ShuffleDeskSimple;
import net.nashlegend.sourcewall.db.GroupHelper;
import net.nashlegend.sourcewall.db.gen.MyGroup;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.request.api.PostAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;
import net.nashlegend.sourcewall.view.PostListItemView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NashLegend on 2014/9/18 0018
 * 这几个Fragment结构几乎一模一样，简直浪费
 * 对于更新很快的地方，下拉加载更多的时候列表不应该只是简单的叠加在一起，
 * 因为这样会导致一个列表里面出现多个相同的帖子，只能一屏展示一页才对。
 * 因此要改为按页加载，还要提供加载上一页的功能，按时间倒序排列的都有这问题……
 * 我擦……
 */
public class PostsFragment extends ChannelsFragment implements LListView.OnRefreshListener, LoadingView.ReloadListener {
    private LListView listView;
    private PostAdapter adapter;
    private LoaderTask task;
    private SubItem subItem;
    private LoadingView loadingView;
    private int currentPage = -1;//page从0开始，-1表示还没有数据
    private View headerView;
    private ViewGroup moreSectionsLayout;
    private ShuffleDeskSimple deskSimple;
    private Button manageButton;
    private long currentDBVersion = -1;
    private final int cacheDuration = 300;
    private ProgressBar progressBar;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public View onCreateLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        subItem = (SubItem) getArguments().getSerializable(Consts.Extra_SubItem);
        headerView = inflater.inflate(R.layout.layout_header_load_pre_page, null, false);
        loadingView = (LoadingView) view.findViewById(R.id.post_progress_loading);
        loadingView.setReloadListener(this);
        listView = (LListView) view.findViewById(R.id.list_posts);
        adapter = new PostAdapter(getActivity());
        listView.setCanPullToRefresh(false);
        listView.setCanPullToLoadMore(false);
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), PostActivity.class);
                intent.putExtra(Consts.Extra_Post, ((PostListItemView) view).getData());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
            }
        });

        listView.addHeaderView(headerView);
        headerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (headerView.getLayoutParams() != null) {
                    headerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    headerView.getLayoutParams().height = 1;
                    headerView.setVisibility(View.GONE);
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

        ScrollView scrollView = (ScrollView) view.findViewById(R.id.plastic_scroller);
        moreSectionsLayout = (ViewGroup) view.findViewById(R.id.layout_more_sections);
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
                        Intent intent = new Intent(getActivity(), ShuffleGroupActivity.class);
                        startActivityForResult(intent, Consts.Code_Start_Shuffle_Groups);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
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
        progressBar = (ProgressBar) view.findViewById(R.id.posts_loading);
        setTitle();
        loadOver();
        return view;
    }

    private void onSectionButtonClicked(GroupMovableButton button) {
        MyGroup myGroup = button.getSection();
        SubItem subItem = new SubItem(myGroup.getSection(), myGroup.getType(), myGroup.getName(), myGroup.getValue());
        Intent intent = new Intent();
        intent.setAction(Consts.Action_Open_Content_Fragment);
        intent.putExtra(Consts.Extra_SubItem, subItem);
        getActivity().sendBroadcast(intent);
        hideMoreSections();
    }

    private void initView() {
        deskSimple.InitDatas();
        deskSimple.initView();
    }

    private List<MyGroup> unselectedSections;

    private void getButtons() {
        unselectedSections = GroupHelper.getUnselectedGroups();
        ArrayList<MovableButton> unselectedButtons = new ArrayList<>();
        for (int i = 0; i < unselectedSections.size(); i++) {
            MyGroup section = unselectedSections.get(i);
            GroupMovableButton button = new GroupMovableButton(getActivity());
            button.setSection(section);
            unselectedButtons.add(button);
        }
        deskSimple.setButtons(unselectedButtons);
    }

    @Override
    public void onCreateViewAgain(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SubItem mSubItem = (SubItem) getArguments().getSerializable(Consts.Extra_SubItem);
        resetData(mSubItem);
    }

    @Override
    public void setTitle() {
        if (subItem.getType() == SubItem.Type_Collections) {
            getActivity().setTitle("小组热贴");
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("小组热贴");
        } else {
            getActivity().setTitle(this.subItem.getName() + " -- 小组");
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(this.subItem.getName() + " -- 小组");
        }
    }

    private void showMoreSections() {
        if (!isAdded()) {
            return;
        }
        getActivity().setTitle(R.string.more_groups);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.more_groups);
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
                        AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.hint)
                                .setMessage(R.string.ok_to_load_groups)
                                .setPositiveButton(R.string.confirm_to_load_my_groups, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        hideMoreSections();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                MobclickAgent.onEvent(getActivity(), Mob.Event_Load_My_Groups);
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

    private void commitChange(ArrayList<MovableButton> buttons) {
        List<MyGroup> sections = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i++) {
            MyGroup myGroup = (MyGroup) buttons.get(i).getSection();
            if (!myGroup.getSelected()) {
                myGroup.setOrder(1024 + myGroup.getOrder());
            }
            sections.add(myGroup);
        }
        GroupHelper.putUnselectedGroups(sections);
    }

    private void loadOver() {
        loadData(0);
        loadingView.startLoading();
    }

    private void loadData(int offset) {
        if (offset < 0) {
            offset = 0;
        }
        cancelPotentialTask();
        task = new LoaderTask(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, offset);
    }

    private void loadPrePage() {
        if (headerView.findViewById(R.id.text_header_load_hint).getVisibility() == View.VISIBLE) {
            listView.setCanPullToLoadMore(false);
            listView.setCanPullToRefresh(false);
            headerView.findViewById(R.id.text_header_load_hint).setVisibility(View.INVISIBLE);
            headerView.findViewById(R.id.progress_header_loading).setVisibility(View.VISIBLE);
            loadData(currentPage - 1);
        }
    }

    private void writePost() {
        if (UserAPI.isLoggedIn()) {
            Intent intent = new Intent(getActivity(), PublishPostActivity.class);
            intent.putExtra(Consts.Extra_SubItem, subItem);
            startActivityForResult(intent, Consts.Code_Publish_Post);
            getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
        } else {
            ((BaseActivity) getActivity()).notifyNeedLog();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Consts.Code_Publish_Post && resultCode == Activity.RESULT_OK) {
            //Publish OK
        }
    }

    @Override
    public void onStartRefresh() {
        headerView.getLayoutParams().height = 1;
        headerView.setVisibility(View.GONE);
        loadData(0);
    }

    @Override
    public void onStartLoadMore() {
        loadData(currentPage + 1);
    }

    @Override
    public int getFragmentMenu() {
        return R.menu.menu_fragment_post;
    }

    private ImageView moreSectionsImageView;
    private boolean isMoreSectionsButtonShowing;

    @Override
    public boolean takeOverMenuInflate(MenuInflater inflater, Menu menu) {
        inflater.inflate(getFragmentMenu(), menu);
        if (subItem.getType() == SubItem.Type_Collections || subItem.getType() == SubItem.Type_Private_Channel) {
            menu.findItem(R.id.action_write_post).setVisible(false);
        }
        if (!UserAPI.isLoggedIn()) {
            menu.findItem(R.id.action_more_sections).setVisible(false);
        } else {
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
    }

    @Override
    public boolean takeOverOptionsItemSelect(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_write_post:
                writePost();
                break;
        }
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
        loadingView.onLoadSuccess();
        if (subItem.equals(this.subItem)) {
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
            headerView.getLayoutParams().height = 1;
            headerView.setVisibility(View.GONE);
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
        if (!sub.equals(this.subItem)) {
            loadingView.startLoading();
        }
    }

    private void cancelPotentialTask() {
        if (task != null && task.getStatus() == AAsyncTask.Status.RUNNING) {
            task.cancel(true);
            listView.doneOperation();
        }
    }

    @Override
    public void reload() {
        loadData(0);
    }

    /**
     * 这几个Task都长得很像，可以封装起来
     */
    class LoaderTask extends AAsyncTask<Integer, ResultObject, ResultObject> {

        LoaderTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        int loadedPage;

        @Override
        protected ResultObject doInBackground(Integer... datas) {
            loadedPage = datas[0];
            String key = String.valueOf(subItem.getSection()) + subItem.getType() + subItem.getName() + subItem.getValue();
            if (loadedPage == 0 && adapter.getCount() == 0) {
                ResultObject cachedResultObject = PostAPI.getCachedPostList(subItem);
                if (cachedResultObject.ok) {
                    long lastLoad = SharedPreferencesUtil.readLong(key, 0l) / 1000;
                    long crtLoad = System.currentTimeMillis() / 1000;
                    if (subItem.getType() == SubItem.Type_Private_Channel || crtLoad - lastLoad > cacheDuration) {
                        //我的小组，更新较快，不缓存
                        System.out.println("小组 " + subItem.getName() + " 使用缓存内容作为临时填充");
                        publishProgress(cachedResultObject);
                    } else {
                        System.out.println("小组 " + subItem.getName() + " 本次加载使用缓存内容");
                        return cachedResultObject;
                    }
                }
            }

            ResultObject resultObject = new ResultObject();
            //解析html的page是从1开始的，所以offset要+1
            if (subItem.getType() == SubItem.Type_Collections) {
                resultObject = PostAPI.getGroupHotPostListFromMobileUrl(loadedPage + 1);// not featured
            } else if (subItem.getType() == SubItem.Type_Private_Channel) {
                resultObject = PostAPI.getMyGroupRecentRepliesPosts(loadedPage + 1);// not featured
            } else if (subItem.getType() == SubItem.Type_Single_Channel) {
                resultObject = PostAPI.getGroupPostListByJsonUrl(subItem.getValue(), loadedPage * 20);// featured
            }

            if (resultObject.ok) {
                SharedPreferencesUtil.saveLong(key, System.currentTimeMillis());
            }

            return resultObject;
        }

        /**
         * 加载缓存的列表，但是会导致点击的时候会更明显的卡一下
         *
         * @param values The values indicating progress.
         */
        @Override
        protected void onProgressUpdate(ResultObject... values) {
            ResultObject o = values[0];
            ArrayList<Post> ars = (ArrayList<Post>) o.result;
            if (ars.size() > 0) {
                progressBar.setVisibility(View.VISIBLE);
                loadingView.onLoadSuccess();
                adapter.setList(ars);
                adapter.notifyDataSetInvalidated();
            }
        }

        @Override
        protected void onPostExecute(ResultObject o) {
            listView.doneOperation();
            progressBar.setVisibility(View.GONE);
            if (o.ok) {
                loadingView.onLoadSuccess();
                ArrayList<Post> ars = (ArrayList<Post>) o.result;
                if (ars.size() > 0) {
                    currentPage = loadedPage;
                    adapter.setList(ars);
                    adapter.notifyDataSetChanged();
                    listView.setSelection(0);
                } else {
                    //没有数据，页码不变
                    toast("没有加载到数据");
                }
            } else {
                toast(R.string.load_failed);
                loadingView.onLoadFailed();
            }
            if (currentPage > 0) {
                headerView.setVisibility(View.VISIBLE);
                headerView.getLayoutParams().height = 0;
            } else {
                headerView.getLayoutParams().height = 1;
                headerView.setVisibility(View.GONE);
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

        @Override
        public void onCancel() {
            loadingView.onLoadSuccess();
            listView.doneOperation();
            if (currentPage > 0) {
                headerView.setVisibility(View.VISIBLE);
                headerView.getLayoutParams().height = 0;
            } else {
                headerView.getLayoutParams().height = 1;
                headerView.setVisibility(View.GONE);
            }
            if (adapter.getCount() > 0) {
                listView.setCanPullToLoadMore(true);
                listView.setCanPullToRefresh(true);
            } else {
                listView.setCanPullToLoadMore(false);
                listView.setCanPullToRefresh(true);
            }
        }
    }
}

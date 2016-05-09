package net.nashlegend.sourcewall.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.db.GroupHelper;
import net.nashlegend.sourcewall.db.gen.MyGroup;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.PostAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.view.common.shuffle.GroupMovableButton;
import net.nashlegend.sourcewall.view.common.shuffle.MovableButton;
import net.nashlegend.sourcewall.view.common.shuffle.ShuffleDesk;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author NashLegend
 */
public class ShuffleGroupActivity extends BaseActivity {

    private ShuffleDesk desk;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    final int defaultGroupsNumber = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuffle);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        desk = (ShuffleDesk) findViewById(R.id.shuffle_desk);
        ((TextView) desk.findViewById(R.id.text_main_sections)).setText(R.string.selected_groups);
        ((TextView) desk.findViewById(R.id.text_other_sections)).setText(R.string.more_unselected_groups);
        desk.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                desk.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initView();
            }
        });

        if (getIntent().getBooleanExtra(Consts.Extra_Should_Load_Before_Shuffle, false)) {
            loadGroupsFromNet();
        } else {
            loadFromDB();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shuffle_groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reload_my_groups) {
            commitChanges();
            loadGroupsFromNet();
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            processFinish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            processFinish();
        }
        return false;
    }

    private void processFinish() {
        commitChanges();
        finish();
    }

    private void commitChanges() {
        if (desk.getSenator().getList() != null && desk.getSenator().getList().size() > 0) {
            ArrayList<MovableButton> buttons = desk.getButtons();
            ArrayList<MyGroup> sections = new ArrayList<>();
            for (int i = 0; i < buttons.size(); i++) {
                MyGroup myGroup = (MyGroup) buttons.get(i).getSection();
                if (!myGroup.getSelected()) {
                    myGroup.setOrder(1024 + myGroup.getOrder());
                }
                sections.add(myGroup);
            }
            if (sections.size() > 0) {
                GroupHelper.putAllMyGroups(sections);
            }
            setResult(RESULT_OK);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        desk.InitDatas();
        desk.initView();
    }

    public void getButtons() {

        List<MyGroup> selectedSections = GroupHelper.getSelectedGroups();
        List<MyGroup> unselectedSections = GroupHelper.getUnselectedGroups();

        ArrayList<MovableButton> selectedButtons = new ArrayList<>();
        for (int i = 0; i < selectedSections.size(); i++) {
            MyGroup section = selectedSections.get(i);
            GroupMovableButton button = new GroupMovableButton(this);
            button.setSection(section);
            selectedButtons.add(button);
        }

        ArrayList<MovableButton> unselectedButtons = new ArrayList<>();
        for (int i = 0; i < unselectedSections.size(); i++) {
            MyGroup section = unselectedSections.get(i);
            GroupMovableButton button = new GroupMovableButton(this);
            button.setSection(section);
            unselectedButtons.add(button);
        }
        desk.setSelectedButtons(selectedButtons);
        desk.setUnselectedButtons(unselectedButtons);
    }

    private void mergeMyGroups(ArrayList<MyGroup> myGroups) {
        if (GroupHelper.getMyGroupsNumber() > 0) {
            List<MyGroup> selectedGroups = GroupHelper.getSelectedGroups();
            for (int i = 0; i < myGroups.size(); i++) {
                MyGroup tmpGroup = myGroups.get(i);
                boolean selected = false;
                int order = 0;
                for (int j = 0; j < selectedGroups.size(); j++) {
                    if (selectedGroups.get(j).getValue().equals(tmpGroup.getValue())) {
                        selected = true;
                        order = j;
                        break;
                    }
                }
                tmpGroup.setSelected(selected);
                if (selected) {
                    tmpGroup.setOrder(order);
                } else {
                    tmpGroup.setOrder(1024 + i);
                }
            }
        }
    }

    private void loadFromDB() {
        Observable
                .just("String")
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        getButtons();
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        initView();
                    }
                });
    }

    private void loadGroupsFromNet() {
        final Subscription subscription =
                PostAPI
                        .getAllMyGroups(UserAPI.getUkey())
                        .flatMap(new Func1<ResponseObject<ArrayList<SubItem>>, Observable<ArrayList<MyGroup>>>() {
                            @Override
                            public Observable<ArrayList<MyGroup>> call(ResponseObject<ArrayList<SubItem>> result) {
                                if (result.ok) {
                                    ArrayList<MyGroup> myGroups = new ArrayList<>();
                                    for (int i = 0; i < result.result.size(); i++) {
                                        SubItem item = result.result.get(i);
                                        MyGroup mygroup = new MyGroup();
                                        mygroup.setName(item.getName());
                                        mygroup.setValue(item.getValue());
                                        mygroup.setType(item.getType());
                                        mygroup.setSection(item.getSection());
                                        mygroup.setSelected(i < defaultGroupsNumber);
                                        mygroup.setOrder(i);
                                        myGroups.add(mygroup);
                                    }
                                    mergeMyGroups(myGroups);
                                    GroupHelper.putAllMyGroups(myGroups);
                                    getButtons();
                                    return Observable.just(myGroups);
                                }
                                return Observable.error(new IllegalStateException("error occurred"));
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<ArrayList<MyGroup>>() {
                            @Override
                            public void onCompleted() {
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onError(Throwable e) {
                                progressDialog.dismiss();
                                MobclickAgent.onEvent(ShuffleGroupActivity.this, Mob.Event_Load_My_Groups_Failed);
                                toast("加载我的小组失败");
                            }

                            @Override
                            public void onNext(ArrayList<MyGroup> myGroups) {
                                progressDialog.dismiss();
                                MobclickAgent.onEvent(ShuffleGroupActivity.this, Mob.Event_Load_My_Groups_OK);
                                initView();
                            }
                        });
        MobclickAgent.onEvent(ShuffleGroupActivity.this, Mob.Event_Load_My_Groups);
        progressDialog = new ProgressDialog(ShuffleGroupActivity.this);
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
}

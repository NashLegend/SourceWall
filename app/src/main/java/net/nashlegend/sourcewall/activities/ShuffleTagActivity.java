package net.nashlegend.sourcewall.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.db.AskTagHelper;
import net.nashlegend.sourcewall.db.gen.AskTag;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.ResponseError;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.QuestionAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.AAsyncTask;
import net.nashlegend.sourcewall.view.common.IStackedAsyncTaskInterface;
import net.nashlegend.sourcewall.view.common.shuffle.AskTagMovableButton;
import net.nashlegend.sourcewall.view.common.shuffle.MovableButton;
import net.nashlegend.sourcewall.view.common.shuffle.ShuffleDesk;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author NashLegend
 */
public class ShuffleTagActivity extends BaseActivity {

    private ShuffleDesk desk;
    LoaderFromNetTask netTask;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    final int defaultTagsNumber = 9;

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
        desk.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                //noinspection deprecation
                desk.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initView();
            }
        });
        ((TextView) desk.findViewById(R.id.text_main_sections)).setText(R.string.selected_tags);
        ((TextView) desk.findViewById(R.id.text_other_sections)).setText(R.string.more_unselected_tags);
        if (getIntent().getBooleanExtra(Consts.Extra_Should_Load_Before_Shuffle, false)) {
            netTask = new LoaderFromNetTask(this);
            netTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shuffle_tag, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reload_my_tags) {
            commitChanges();
            if (netTask != null && netTask.getStatus() == AAsyncTask.Status.RUNNING) {
                netTask.cancel(false);
            }
            netTask = new LoaderFromNetTask(this);
            netTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            ArrayList<AskTag> sections = new ArrayList<>();
            for (int i = 0; i < buttons.size(); i++) {
                AskTag askTag = (AskTag) buttons.get(i).getSection();
                if (!askTag.getSelected()) {
                    askTag.setOrder(1024 + askTag.getOrder());
                }
                sections.add(askTag);
            }
            if (sections.size() > 0) {
                AskTagHelper.putAllMyTags(sections);
            }
            setResult(RESULT_OK);
        }
    }

    @Override
    protected void onDestroy() {
        if (netTask != null && netTask.getStatus() == AAsyncTask.Status.RUNNING) {
            netTask.cancel(false);
        }
        super.onDestroy();
    }

    private void initView() {
        desk.InitDatas();
        desk.initView();
    }

    public void getButtons() {

        List<AskTag> selectedSections = AskTagHelper.getSelectedTags();
        List<AskTag> unselectedSections = AskTagHelper.getUnselectedTags();

        ArrayList<MovableButton> selectedButtons = new ArrayList<>();
        for (int i = 0; i < selectedSections.size(); i++) {
            AskTag section = selectedSections.get(i);
            AskTagMovableButton button = new AskTagMovableButton(this);
            button.setSection(section);
            selectedButtons.add(button);
        }

        ArrayList<MovableButton> unselectedButtons = new ArrayList<>();
        for (int i = 0; i < unselectedSections.size(); i++) {
            AskTag section = unselectedSections.get(i);
            AskTagMovableButton button = new AskTagMovableButton(this);
            button.setSection(section);
            unselectedButtons.add(button);
        }
        desk.setSelectedButtons(selectedButtons);
        desk.setUnselectedButtons(unselectedButtons);
    }

    private void mergeMyGroups(ArrayList<AskTag> myTags) {
        if (AskTagHelper.getAskTagsNumber() > 0) {
            List<AskTag> selectedGroups = AskTagHelper.getSelectedTags();
            for (int i = 0; i < myTags.size(); i++) {
                AskTag tmpTag = myTags.get(i);
                boolean selected = false;
                int order = 0;
                for (int j = 0; j < selectedGroups.size(); j++) {
                    if (selectedGroups.get(j).getValue().equals(tmpTag.getValue())) {
                        selected = true;
                        order = j;
                        break;
                    }
                }
                tmpTag.setSelected(selected);
                if (selected) {
                    tmpTag.setOrder(order);
                } else {
                    tmpTag.setOrder(1024 + i);
                }
            }
        }
    }

    class LoaderFromNetTask extends AAsyncTask<String, Integer, ResponseObject> {

        public LoaderFromNetTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ShuffleTagActivity.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage(getString(R.string.message_wait_a_minute));
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    LoaderFromNetTask.this.cancel(true);
                }
            });
            progressDialog.show();
        }

        @Override
        protected ResponseObject doInBackground(String[] params) {
            ResponseObject<ArrayList<SubItem>> result = QuestionAPI.getAllMyTags();
            if (TextUtils.isEmpty(UserAPI.getUserID())) {
                result.error_message = "无法获得用户id";
                result.error = ResponseError.NO_USER_ID;
            } else if (result.ok) {
                ArrayList<SubItem> subItems = result.result;
                ArrayList<AskTag> myTags = new ArrayList<>();
                for (int i = 0; i < subItems.size(); i++) {
                    SubItem item = subItems.get(i);
                    AskTag myTag = new AskTag();
                    myTag.setName(item.getName());
                    myTag.setValue(item.getValue());
                    myTag.setType(item.getType());
                    myTag.setSection(item.getSection());
                    myTag.setSelected(i < defaultTagsNumber);
                    myTag.setOrder(i);
                    myTags.add(myTag);
                }
                mergeMyGroups(myTags);
                AskTagHelper.putAllMyTags(myTags);
                getButtons();
            }
            return result;
        }

        @Override
        protected void onPostExecute(ResponseObject result) {
            progressDialog.dismiss();
            if (result.ok) {
                MobclickAgent.onEvent(ShuffleTagActivity.this, Mob.Event_Load_My_Tags_OK);
                initView();
            } else {
                MobclickAgent.onEvent(ShuffleTagActivity.this, Mob.Event_Load_My_Tags_Failed);
                MobclickAgent.reportError(ShuffleTagActivity.this, "加载标签失败\n是否WIFI：" + Config.isWifi() + "\n" + UserAPI.getUserInfoString() + result.error_message);
                if (result.error == ResponseError.NO_USER_ID) {
                    toast("未获得用户ID，无法加载");
                } else {
                    toast("加载标签失败");
                }
            }
        }
    }
}

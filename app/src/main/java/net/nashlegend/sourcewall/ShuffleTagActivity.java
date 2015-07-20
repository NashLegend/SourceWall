package net.nashlegend.sourcewall;

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

import net.nashlegend.sourcewall.commonview.AAsyncTask;
import net.nashlegend.sourcewall.commonview.IStackedAsyncTaskInterface;
import net.nashlegend.sourcewall.commonview.shuffle.AskTagMovableButton;
import net.nashlegend.sourcewall.commonview.shuffle.MovableButton;
import net.nashlegend.sourcewall.commonview.shuffle.ShuffleDesk;
import net.nashlegend.sourcewall.db.AskTagHelper;
import net.nashlegend.sourcewall.db.gen.AskTag;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.request.api.QuestionAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;

import java.util.ArrayList;
import java.util.List;

/**
 * @author NashLegend
 */
public class ShuffleTagActivity extends SwipeActivity {

    private ShuffleDesk desk;
    LoaderFromDBTask dbTask;
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
        desk = (ShuffleDesk) findViewById(R.id.shuffle_desk);
        desk.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
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
            dbTask = new LoaderFromDBTask(this);
            dbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            commitChanges();
            finish();
        }
        return false;
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
        if (dbTask != null && dbTask.getStatus() == AAsyncTask.Status.RUNNING) {
            dbTask.cancel(false);
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
                for (int j = 0; j < selectedGroups.size(); j++) {
                    if (selectedGroups.get(j).getValue().equals(tmpTag.getValue())) {
                        selected = true;
                        break;
                    }
                }
                tmpTag.setSelected(selected);
                if (selected) {
                    tmpTag.setOrder(i);
                } else {
                    tmpTag.setOrder(1024 + i);
                }
            }
        }
    }

    class LoaderFromDBTask extends AAsyncTask<String, Integer, Boolean> {

        public LoaderFromDBTask(IStackedAsyncTaskInterface iStackedAsyncTaskInterface) {
            super(iStackedAsyncTaskInterface);
        }

        @Override
        protected Boolean doInBackground(String[] params) {
            getButtons();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            initView();
        }
    }

    class LoaderFromNetTask extends AAsyncTask<String, Integer, ResultObject> {

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
        protected ResultObject doInBackground(String[] params) {
            ResultObject<ArrayList<SubItem>> result = QuestionAPI.getAllMyTags();
            if (TextUtils.isEmpty(UserAPI.getUserID())) {
                result.error_message = "无法获得用户id";
                result.code = ResultObject.ResultCode.CODE_NO_USER_ID;
            }else if (result.ok) {
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
        protected void onPostExecute(ResultObject result) {
            progressDialog.dismiss();
            if (result.ok) {
                MobclickAgent.onEvent(ShuffleTagActivity.this, Mob.Event_Load_My_Tags_OK);
                initView();
            } else {
                MobclickAgent.onEvent(ShuffleTagActivity.this, Mob.Event_Load_My_Tags_Failed);
                MobclickAgent.reportError(ShuffleTagActivity.this,
                        "加载标签失败\n是否WIFI：" + Config.isWifi() + "\n" + UserAPI.getUserInfoString() + result.error_message);
                if (result.code == ResultObject.ResultCode.CODE_NO_USER_ID) {
                    toast("未获得用户ID，无法加载");
                } else {
                    toast("加载标签失败");
                }
            }
        }
    }
}

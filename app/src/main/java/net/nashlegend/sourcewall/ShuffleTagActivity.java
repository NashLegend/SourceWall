package net.nashlegend.sourcewall;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import net.nashlegend.sourcewall.commonview.shuffle.AskTagMovableButton;
import net.nashlegend.sourcewall.commonview.shuffle.MovableButton;
import net.nashlegend.sourcewall.commonview.shuffle.ShuffleDesk;
import net.nashlegend.sourcewall.connection.ResultObject;
import net.nashlegend.sourcewall.connection.api.QuestionAPI;
import net.nashlegend.sourcewall.db.AskTagHelper;
import net.nashlegend.sourcewall.db.gen.AskTag;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.ToastUtil;

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
        setContentView(net.nashlegend.sourcewall.R.layout.activity_shuffle);
        toolbar = (Toolbar) findViewById(net.nashlegend.sourcewall.R.id.action_bar);
        setSupportActionBar(toolbar);
        desk = (ShuffleDesk) findViewById(net.nashlegend.sourcewall.R.id.shuffle_desk);
        desk.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                desk.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initView();
            }
        });
        ((TextView) desk.findViewById(net.nashlegend.sourcewall.R.id.text_main_sections)).setText(net.nashlegend.sourcewall.R.string.selected_tags);
        ((TextView) desk.findViewById(net.nashlegend.sourcewall.R.id.text_other_sections)).setText(net.nashlegend.sourcewall.R.string.more_unselected_tags);
        if (getIntent().getBooleanExtra(Consts.Extra_Should_Load_Before_Shuffle, false)) {
            netTask = new LoaderFromNetTask();
            netTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            dbTask = new LoaderFromDBTask();
            dbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(net.nashlegend.sourcewall.R.menu.shuffle_tag, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == net.nashlegend.sourcewall.R.id.action_reload_my_tags) {
            commitChanges();
            if (netTask != null && netTask.getStatus() == AsyncTask.Status.RUNNING) {
                netTask.cancel(false);
            }
            netTask = new LoaderFromNetTask();
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
        if (netTask != null && netTask.getStatus() == AsyncTask.Status.RUNNING) {
            netTask.cancel(false);
        }
        if (dbTask != null && dbTask.getStatus() == AsyncTask.Status.RUNNING) {
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

    class LoaderFromDBTask extends AsyncTask<String, Integer, Boolean> {

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

    class LoaderFromNetTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            addToStackedTasks(this);
            progressDialog = new ProgressDialog(ShuffleTagActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage(getString(net.nashlegend.sourcewall.R.string.message_replying));
            progressDialog.show();
        }

        @Override
        protected void onCancelled() {
            removeFromStackedTasks(this);
        }

        @Override
        protected Boolean doInBackground(String[] params) {
            ResultObject resultObject = QuestionAPI.getAllMyTags();
            if (resultObject.ok) {
                ArrayList<SubItem> subItems = (ArrayList<SubItem>) resultObject.result;
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
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            removeFromStackedTasks(this);
            progressDialog.dismiss();
            if (aBoolean) {
                initView();
            } else {
                ToastUtil.toast("Failed");
            }
        }
    }
}

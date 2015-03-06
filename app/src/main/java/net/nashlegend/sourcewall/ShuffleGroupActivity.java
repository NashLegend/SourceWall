package net.nashlegend.sourcewall;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import net.nashlegend.sourcewall.commonview.shuffle.GroupMovableButton;
import net.nashlegend.sourcewall.commonview.shuffle.MovableButton;
import net.nashlegend.sourcewall.commonview.shuffle.ShuffleDesk;
import net.nashlegend.sourcewall.connection.ResultObject;
import net.nashlegend.sourcewall.connection.api.PostAPI;
import net.nashlegend.sourcewall.db.GroupHelper;
import net.nashlegend.sourcewall.db.gen.MyGroup;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.util.Consts;

import java.util.ArrayList;
import java.util.List;

/**
 * @author NashLegend
 */
public class ShuffleGroupActivity extends SwipeActivity {

    private ShuffleDesk desk;
    LoaderFromDBTask dbTask;
    LoaderFromNetTask netTask;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    final int defaultGroupsNumber = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuffle);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
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
            netTask = new LoaderFromNetTask();
            netTask.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            dbTask = new LoaderFromDBTask();
            dbTask.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
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
            if (netTask != null && netTask.getStatus() == AsyncTask.Status.RUNNING) {
                netTask.cancel(false);
            }
            netTask = new LoaderFromNetTask();
            netTask.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
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
                for (int j = 0; j < selectedGroups.size(); j++) {
                    if (selectedGroups.get(j).getValue().equals(tmpGroup.getValue())) {
                        selected = true;
                        break;
                    }
                }
                tmpGroup.setSelected(selected);
                if (selected) {
                    tmpGroup.setOrder(i);
                } else {
                    tmpGroup.setOrder(1024 + i);
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
            progressDialog = new ProgressDialog(ShuffleGroupActivity.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage(getString(R.string.message_replying));
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    LoaderFromNetTask.this.cancel(true);
                }
            });
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String[] params) {
            ResultObject resultObject = PostAPI.getAllMyGroups();
            if (resultObject.ok) {
                ArrayList<SubItem> subItems = (ArrayList<SubItem>) resultObject.result;
                ArrayList<MyGroup> myGroups = new ArrayList<>();
                for (int i = 0; i < subItems.size(); i++) {
                    SubItem item = subItems.get(i);
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
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            if (aBoolean) {
                initView();
            } else {
                toast("加载我的小组失败");
            }
        }
    }
}

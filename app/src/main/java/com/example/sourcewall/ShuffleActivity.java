package com.example.sourcewall;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.example.sourcewall.commonview.shuffle.GroupMovableButton;
import com.example.sourcewall.commonview.shuffle.MovableButton;
import com.example.sourcewall.commonview.shuffle.ShuffleDesk;
import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.PostAPI;
import com.example.sourcewall.db.GroupHelper;
import com.example.sourcewall.db.gen.MyGroup;
import com.example.sourcewall.model.SubItem;
import com.example.sourcewall.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author NashLegend
 */
public class ShuffleActivity extends BaseActivity {

    private ShuffleDesk desk;
    LoaderFromDBTask dbTask;
    LoaderFromNetTask netTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuffle);
        desk = (ShuffleDesk) findViewById(R.id.shuffle_desk);
        desk.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                desk.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initView();
            }
        });
//        dbTask = new LoaderFromDBTask();
//        dbTask.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);

        netTask=new LoaderFromNetTask();
        netTask.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            commitAndFinish();
        }
        return false;
    }

//    @Override
//    public void onBackPressed() {
//        commitChange(desk.getButtons());
//        setResult(RESULT_OK);
//        super.onBackPressed();
//    }

    private void commitAndFinish() {
        commitChange(desk.getButtons());
        setResult(RESULT_OK);
        finish();
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

    public void commitChange(ArrayList<MovableButton> buttons) {
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
        protected Boolean doInBackground(String[] params) {
            ResultObject resultObject = PostAPI.getAllMyGroups();
            if (resultObject.ok) {
                ArrayList<SubItem> subItems = (ArrayList<SubItem>) resultObject.result;
                ArrayList<MyGroup> myGroups = new ArrayList<>();
                int sel = 18;
                for (int i = 0; i < subItems.size(); i++) {
                    SubItem item = subItems.get(i);
                    MyGroup mygroup = new MyGroup();
                    mygroup.setName(item.getName());
                    mygroup.setValue(item.getValue());
                    mygroup.setType(item.getType());
                    mygroup.setSection(item.getSection());
                    mygroup.setSelected(i < sel);
                    myGroups.add(mygroup);
                }
                GroupHelper.putAllMyGroups(myGroups);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                getButtons();
                initView();
            } else {
                ToastUtil.toast("Failed");
            }
        }
    }
}

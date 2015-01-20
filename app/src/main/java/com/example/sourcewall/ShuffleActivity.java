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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuffle);
        desk = (ShuffleDesk) findViewById(R.id.shuffle_desk);
        desk.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                desk.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                desk.InitDatas();
                desk.initView();
            }
        });
        LoaderTask task = new LoaderTask();
        task.execute();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            commitAndFinish();
        }
        return false;
    }

    private void commitAndFinish() {
        commitChange(desk.getButtons());
        setResult(RESULT_OK);
        finish();
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
            sections.add((MyGroup) buttons.get(i).getSection());
        }
        if (sections.size() > 0) {
            GroupHelper.putAllMyGroups(sections);
        }
    }

    class LoaderTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String[] params) {
            ResultObject resultObject = PostAPI.getAllMyGroups();
            if (resultObject.ok) {
                ArrayList<SubItem> subItems = (ArrayList<SubItem>) resultObject.result;
                ArrayList<MyGroup> myGroups = new ArrayList<>();
                int sel = 24;
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
                ToastUtil.toast("OK");
                getButtons();
            } else {
                ToastUtil.toast("Failed");
            }
        }
    }
}

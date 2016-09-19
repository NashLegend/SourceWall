package net.nashlegend.sourcewall.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.data.Consts;
import net.nashlegend.sourcewall.fragment.PostsFragment;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.RequestObject.SimpleCallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.PostAPI;

//from http://m.guokr.com/group/376/ or http://www.guokr.com/group/376/
public class PostListActivity extends BaseActivity {

    SubItem subItem;
    String subId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        subItem = getIntent().getParcelableExtra(Consts.Extras.Extra_SubItem);
        subId = getIntent().getStringExtra(Consts.Extras.Extra_SubItem_ID);
        if (subItem != null) {
            onGetSubItem(subItem);
        } else {
            if (TextUtils.isEmpty(subId)) {
                toastSingleton("参数错误");
                finish();
            } else {
                getSubItem();
            }
        }
    }

    private void onGetSubItem(SubItem item) {
        subItem = item;
        subId = subItem.getValue();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, PostsFragment.newInstance(subItem))
                .commitAllowingStateLoss();
        setTitle(subItem.getName());
    }

    private void getSubItem() {
        PostAPI.getGroupNameById(subId, new SimpleCallBack<String>() {
            @Override
            public void onFailure(@NonNull ResponseObject<String> result) {
                if (result.statusCode == 404) {
                    toastSingleton("小组不存在");
                    finish();
                } else {
                    SubItem subItem = new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "某小组", subId);
                    onGetSubItem(subItem);
                    toastSingleton("获取小组信息失败");
                }
            }

            @Override
            public void onSuccess(@NonNull String result) {
                SubItem subItem = new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, result, subId);
                onGetSubItem(subItem);
            }
        });
    }

    @Override
    public void setTitle(CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            super.setTitle("小组");
        } else {
            super.setTitle(title);
        }
    }
}

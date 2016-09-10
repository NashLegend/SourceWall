package net.nashlegend.sourcewall.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.data.Mob;
import net.nashlegend.sourcewall.fragment.NoticesFragment;

public class MessageCenterActivity extends BaseActivity {

    NoticesFragment noticesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);
        MobclickAgent.onEvent(this, Mob.Event_Check_Notice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        noticesFragment = new NoticesFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.message_container, noticesFragment).commitAllowingStateLoss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message_center, menu);
        if (noticesFragment == null || !noticesFragment.takeOverMenuInflate(getMenuInflater(), menu)) {
            getMenuInflater().inflate(R.menu.menu_message_center, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return super.onOptionsItemSelected(item);
        } else {
            return noticesFragment != null && noticesFragment.takeOverOptionsItemSelect(item) || super.onOptionsItemSelected(item);
        }
    }
}

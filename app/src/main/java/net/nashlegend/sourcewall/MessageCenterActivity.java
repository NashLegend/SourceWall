package net.nashlegend.sourcewall;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.fragment.NoticesFragment;
import net.nashlegend.sourcewall.util.Mob;

public class MessageCenterActivity extends SwipeActivity {

    NoticesFragment noticesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);
        MobclickAgent.onEvent(this, Mob.Event_Check_Notice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        noticesFragment = new NoticesFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.message_container, noticesFragment).commit();
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
        return noticesFragment != null && noticesFragment.takeOverOptionsItemSelect(item) || super.onOptionsItemSelected(item);
    }
}

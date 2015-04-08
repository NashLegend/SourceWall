package net.nashlegend.sourcewall;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import net.nashlegend.sourcewall.adapters.ImageAdapter;
import net.nashlegend.sourcewall.util.Consts;

import java.util.ArrayList;

public class ImageActivity extends BaseActivity {

    ArrayList<String> images;
    ViewPager pager;
    ImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        pager = (ViewPager) findViewById(R.id.image_pager);
        adapter = new ImageAdapter(this);
        images = getIntent().getStringArrayListExtra(Consts.Extra_Image_String_Array);
        int position = getIntent().getIntExtra(Consts.Extra_Image_Current_Position, 0);
        if (images != null && images.size() > 0) {
            adapter.addAll(images);
            pager.setAdapter(adapter);
            if (position < images.size()) {
                pager.setCurrentItem(position);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

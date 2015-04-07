package net.nashlegend.sourcewall;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.nashlegend.sourcewall.util.Consts;

import java.util.ArrayList;

public class ImageActivity extends ActionBarActivity {

    ArrayList<String> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ArrayList<String> urls = getIntent().getStringArrayListExtra(Consts.Extra_Image_String_Array);
        int position = getIntent().getIntExtra(Consts.Extra_Image_Current_Position, 0);
        if (urls!=null&&urls.size()>0){

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

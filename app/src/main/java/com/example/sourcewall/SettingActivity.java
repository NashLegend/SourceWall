package com.example.sourcewall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.sourcewall.connection.api.UserAPI;
import com.example.sourcewall.util.Config;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.SharedUtil;

public class SettingActivity extends SwipeActivity implements View.OnClickListener {
    Toolbar toolbar;
    View imageModeView;
    View customTailView;
    View logInOutView;
    TextView imageText;
    TextView logText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        imageModeView = findViewById(R.id.layout_image_mode);
        customTailView = findViewById(R.id.layout_custom_tail);
        logInOutView = findViewById(R.id.layout_log_in_out);
        imageText = (TextView) findViewById(R.id.text_image_mode);
        logText = (TextView) findViewById(R.id.text_log_in_out);
        imageModeView.setOnClickListener(this);
        customTailView.setOnClickListener(this);
        logInOutView.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UserAPI.isLoggedIn()) {
            logText.setText(R.string.log_out);
        } else {
            logText.setText(R.string.log_in);
        }

        int mode = Config.getImageLoadMode();
        switch (mode) {
            case Consts.MODE_ALWAYS_LOAD:
                imageText.setText(R.string.mode_always_load);
                break;
            case Consts.MODE_NEVER_LOAD:
                imageText.setText(R.string.mode_never_load);
                break;
            case Consts.MODE_LOAD_WHEN_WIFI:
                imageText.setText(R.string.mode_load_load_when_wifi);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_image_mode:
                popupImageMode();
                break;
            case R.id.layout_custom_tail:
                showCustomTailLayout();
                break;
            case R.id.layout_log_in_out:
                toggleLoginState();
                break;
        }
    }

    private void popupImageMode() {
        String[] ways = {getString(R.string.mode_always_load),
                getString(R.string.mode_never_load),
                getString(R.string.mode_load_load_when_wifi)};
        new AlertDialog.Builder(this).setTitle(R.string.way_to_load_image).setItems(ways, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        SharedUtil.saveInt(Consts.Key_Image_Load_Mode, Consts.MODE_ALWAYS_LOAD);
                        imageText.setText(R.string.mode_always_load);
                        break;
                    case 1:
                        SharedUtil.saveInt(Consts.Key_Image_Load_Mode, Consts.MODE_NEVER_LOAD);
                        imageText.setText(R.string.mode_never_load);
                        break;
                    case 2:
                        SharedUtil.saveInt(Consts.Key_Image_Load_Mode, Consts.MODE_LOAD_WHEN_WIFI);
                        imageText.setText(R.string.mode_load_load_when_wifi);
                        break;
                }
            }
        }).create().show();
    }

    private void showCustomTailLayout() {

    }

    private void toggleLoginState() {
        if (UserAPI.isLoggedIn()) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.hint).setMessage(R.string.ok_to_logout)
                    .setPositiveButton(R.string.log_out, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UserAPI.clearMyInfo();
                            logText.setText(R.string.log_in);
                        }
                    }).setNegativeButton(R.string.cancel, null).create();
            dialog.show();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}

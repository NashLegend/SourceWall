package net.nashlegend.sourcewall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedUtil;

public class SettingActivity extends SwipeActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private TextView imageText;
    private TextView logText;
    private View tailsView;
    private ImageView tailArrow;
    private EditText tailText;
    private RadioButton buttonDefault;
    private RadioButton buttonPhone;
    private RadioButton buttonCustom;
    private int tailsHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        View imageModeView = findViewById(R.id.layout_image_mode);
        View customTailView = findViewById(R.id.layout_custom_tail);
        View logInOutView = findViewById(R.id.layout_log_in_out);
        View aboutView = findViewById(R.id.layout_about_app);
        imageText = (TextView) findViewById(R.id.text_image_mode);
        logText = (TextView) findViewById(R.id.text_log_in_out);
        tailsView = findViewById(R.id.layout_tails);
        tailText = (EditText) findViewById(R.id.text_tail);
        tailArrow = (ImageView) findViewById(R.id.image_tail_arrow);
        buttonDefault = (RadioButton) findViewById(R.id.button_use_default);
        buttonPhone = (RadioButton) findViewById(R.id.button_use_phone);
        buttonCustom = (RadioButton) findViewById(R.id.button_use_custom);
        buttonDefault.setOnCheckedChangeListener(this);
        buttonPhone.setOnCheckedChangeListener(this);
        buttonCustom.setOnCheckedChangeListener(this);

        imageModeView.setOnClickListener(this);
        customTailView.setOnClickListener(this);
        logInOutView.setOnClickListener(this);
        aboutView.setOnClickListener(this);
        tailsView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (tailsView.getHeight() > 0) {
                    tailsView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    tailsHeight = tailsView.getHeight();
                    tailsView.getLayoutParams().height = 0;
                }
            }
        });
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

        switch (SharedUtil.readInt(Consts.key_Use_Tail_Type, Consts.Type_Use_Default_Tail)) {
            case Consts.Type_Use_Default_Tail:
                buttonDefault.setChecked(true);
                tailText.setText(Config.getDefaultPlainTail());
                tailText.setEnabled(false);
                break;
            case Consts.Type_Use_Phone_Tail:
                buttonPhone.setChecked(true);
                tailText.setText(Config.getPhonePlainTail());
                tailText.setEnabled(false);
                break;
            case Consts.Type_Use_Custom_Tail:
                buttonCustom.setChecked(true);
                tailText.setText(SharedUtil.readString(Consts.key_Custom_Tail, ""));
                tailText.setEnabled(true);
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
                toggleCustomTailLayout();
                break;
            case R.id.layout_log_in_out:
                toggleLoginState();
                break;
            case R.id.layout_about_app:
                showAboutApp();
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

    private void toggleCustomTailLayout() {
        ViewGroup.LayoutParams params = tailsView.getLayoutParams();
        if (params.height > 0) {
            params.height = 0;
            tailArrow.setRotation(0);
        } else {
            params.height = tailsHeight;
            tailArrow.setRotation(-90);
        }
        tailsView.setLayoutParams(params);
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

    private void showAboutApp() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.about_app)
                .setMessage(R.string.introduction_about_app)
                .create().show();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.button_use_default:
                    tailText.setText(Config.getDefaultPlainTail());
                    tailText.setEnabled(false);
                    break;
                case R.id.button_use_phone:
                    tailText.setText(Config.getPhonePlainTail());
                    tailText.setEnabled(false);
                    break;
                case R.id.button_use_custom:
                    tailText.setEnabled(true);
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        if (buttonDefault.isChecked()) {
            SharedUtil.saveInt(Consts.key_Use_Tail_Type, Consts.Type_Use_Default_Tail);
        } else if (buttonPhone.isChecked()) {
            SharedUtil.saveInt(Consts.key_Use_Tail_Type, Consts.Type_Use_Phone_Tail);
        } else {
            SharedUtil.saveInt(Consts.key_Use_Tail_Type, Consts.Type_Use_Custom_Tail);
            SharedUtil.saveString(Consts.key_Custom_Tail, tailText.getText().toString());
        }
        super.onPause();
    }
}

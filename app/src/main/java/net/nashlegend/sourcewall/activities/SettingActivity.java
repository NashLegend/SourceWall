package net.nashlegend.sourcewall.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;

public class SettingActivity extends SwipeActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private TextView imageText;
    private TextView logText;
    private View tailsView;
    private View modesView;
    private ImageView tailArrow;
    private ImageView modeArrow;

    private RadioButton buttonAlways;
    private RadioButton buttonNever;
    private RadioButton buttonWifi;
    private CheckBox checkBox;

    private RadioButton buttonDefault;
    private RadioButton buttonPhone;
    private RadioButton buttonCustom;
    private EditText tailText;

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
        modesView = findViewById(R.id.layout_modes);
        tailArrow = (ImageView) findViewById(R.id.image_tail_arrow);
        modeArrow = (ImageView) findViewById(R.id.image_mode_arrow);

        buttonDefault = (RadioButton) findViewById(R.id.button_use_default);
        buttonPhone = (RadioButton) findViewById(R.id.button_use_phone);
        buttonCustom = (RadioButton) findViewById(R.id.button_use_custom);
        tailText = (EditText) findViewById(R.id.text_tail);

        buttonAlways = (RadioButton) findViewById(R.id.button_always_load);
        buttonNever = (RadioButton) findViewById(R.id.button_never_load);
        buttonWifi = (RadioButton) findViewById(R.id.button_wifi_only);
        checkBox = (CheckBox) findViewById(R.id.check_homepage);

        buttonDefault.setOnCheckedChangeListener(this);
        buttonPhone.setOnCheckedChangeListener(this);
        buttonCustom.setOnCheckedChangeListener(this);

        buttonAlways.setOnCheckedChangeListener(this);
        buttonNever.setOnCheckedChangeListener(this);
        buttonWifi.setOnCheckedChangeListener(this);

        checkBox.setOnCheckedChangeListener(this);

        imageModeView.setOnClickListener(this);
        customTailView.setOnClickListener(this);
        logInOutView.setOnClickListener(this);
        aboutView.setOnClickListener(this);
        tailsView.setVisibility(View.GONE);
        modesView.setVisibility(View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (UserAPI.isLoggedIn()) {
            logText.setText(R.string.log_out);
        } else {
            logText.setText(R.string.log_in);
        }

        checkBox.setChecked(SharedPreferencesUtil.readBoolean(Consts.Key_Image_No_Load_Homepage, false));

        int mode = Config.getImageLoadMode();
        switch (mode) {
            case Consts.MODE_ALWAYS_LOAD:
                imageText.setText(R.string.mode_always_load);
                buttonAlways.setChecked(true);
                checkBox.setEnabled(true);
                break;
            case Consts.MODE_NEVER_LOAD:
                imageText.setText(R.string.mode_never_load);
                buttonNever.setChecked(true);
                checkBox.setEnabled(false);
                break;
            case Consts.MODE_LOAD_WHEN_WIFI:
                imageText.setText(R.string.mode_load_load_when_wifi);
                buttonWifi.setChecked(true);
                checkBox.setEnabled(true);
                break;
        }

        switch (SharedPreferencesUtil.readInt(Consts.key_Use_Tail_Type, Consts.Type_Use_Default_Tail)) {
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
                tailText.setText(SharedPreferencesUtil.readString(Consts.key_Custom_Tail, ""));
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
        if (modesView.getVisibility() == View.VISIBLE) {
            modesView.setVisibility(View.GONE);
            modeArrow.setRotation(0);
        } else {
            modesView.setVisibility(View.VISIBLE);
            modeArrow.setRotation(-90);
        }
    }

    private void toggleCustomTailLayout() {
        if (tailsView.getVisibility() == View.VISIBLE) {
            tailsView.setVisibility(View.GONE);
            tailArrow.setRotation(0);
        } else {
            tailsView.setVisibility(View.VISIBLE);
            tailArrow.setRotation(-90);
        }
    }

    private void toggleLoginState() {
        if (UserAPI.isLoggedIn()) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.hint).setMessage(R.string.ok_to_logout).setPositiveButton(R.string.log_out, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UserAPI.clearMyInfo();
                    MobclickAgent.onEvent(SettingActivity.this, Mob.Event_Logout);
                    logText.setText(R.string.log_in);
                }
            }).setNegativeButton(R.string.cancel, null).create();
            dialog.show();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void showAboutApp() {
        new AlertDialog.Builder(this).setTitle(R.string.about_app).setMessage(R.string.introduction_about_app).create().show();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.check_homepage) {
            SharedPreferencesUtil.saveBoolean(Consts.Key_Image_No_Load_Homepage, isChecked);
        } else {
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
                        String cus = SharedPreferencesUtil.readString(Consts.key_Custom_Tail, "");
                        if (!TextUtils.isEmpty(cus)) {
                            tailText.setText(cus);
                        }
                        tailText.setEnabled(true);
                        break;
                    case R.id.button_always_load:
                        checkBox.setEnabled(true);
                        break;
                    case R.id.button_never_load:
                        checkBox.setEnabled(false);
                        break;
                    case R.id.button_wifi_only:
                        checkBox.setEnabled(true);
                        break;
                }
            }
        }
    }

    @Override
    protected void onPause() {
        String preTail = Config.getSimpleReplyTail();
        if (buttonDefault.isChecked()) {
            SharedPreferencesUtil.saveInt(Consts.key_Use_Tail_Type, Consts.Type_Use_Default_Tail);
        } else if (buttonPhone.isChecked()) {
            SharedPreferencesUtil.saveInt(Consts.key_Use_Tail_Type, Consts.Type_Use_Phone_Tail);
        } else {
            SharedPreferencesUtil.saveInt(Consts.key_Use_Tail_Type, Consts.Type_Use_Custom_Tail);
            SharedPreferencesUtil.saveString(Consts.key_Custom_Tail, tailText.getText().toString());
        }
        String crtTail = Config.getSimpleReplyTail();

        if (crtTail.equals(preTail)) {
            MobclickAgent.onEvent(this, Mob.Event_Modify_Tail);
        }

        if (buttonAlways.isChecked()) {
            SharedPreferencesUtil.saveInt(Consts.Key_Image_Load_Mode, Consts.MODE_ALWAYS_LOAD);
        } else if (buttonNever.isChecked()) {
            SharedPreferencesUtil.saveInt(Consts.Key_Image_Load_Mode, Consts.MODE_NEVER_LOAD);
        } else {
            SharedPreferencesUtil.saveInt(Consts.Key_Image_Load_Mode, Consts.MODE_LOAD_WHEN_WIFI);
        }
        super.onPause();
    }
}

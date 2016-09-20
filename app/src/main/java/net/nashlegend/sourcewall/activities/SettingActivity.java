package net.nashlegend.sourcewall.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import net.nashlegend.sourcewall.BuildConfig;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.data.Config;
import net.nashlegend.sourcewall.data.Consts.ImageLoadMode;
import net.nashlegend.sourcewall.data.Consts.Keys;
import net.nashlegend.sourcewall.data.Consts.TailType;
import net.nashlegend.sourcewall.data.Mob;
import net.nashlegend.sourcewall.data.Tail;
import net.nashlegend.sourcewall.events.LoginStateChangedEvent;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.PrefsUtil;
import net.nashlegend.sourcewall.util.UpdateChecker;
import net.nashlegend.sourcewall.util.UrlCheckUtil;

import de.greenrobot.event.EventBus;

public class SettingActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private TextView imageText;
    private TextView logText;
    private View tailsView;
    private View modesView;
    private View replyView;
    private ImageView tailArrow;
    private ImageView modeArrow;
    private ImageView replyArrow;

    private RadioButton buttonAlways;
    private RadioButton buttonNever;
    private RadioButton buttonWifi;
    private CheckBox checkBox;

    private CheckBox checkSimple;
    private CheckBox checkGroup;
    private CheckBox checkBadge;

    private RadioButton buttonDefault;
    private RadioButton buttonPhone;
    private RadioButton buttonCustom;
    private EditText tailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Mob.onEvent(Mob.Event_Open_Setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        View imageModeView = findViewById(R.id.layout_image_mode);
        View replyModeView = findViewById(R.id.layout_reply_mode);
        View customTailView = findViewById(R.id.layout_custom_tail);
        View logInOutView = findViewById(R.id.layout_log_in_out);
        View updateView = findViewById(R.id.layout_app_update);
        View aboutView = findViewById(R.id.layout_about_app);
        TextView updateText = (TextView) findViewById(R.id.text_app_version);

        imageText = (TextView) findViewById(R.id.text_image_mode);
        logText = (TextView) findViewById(R.id.text_log_in_out);
        tailsView = findViewById(R.id.layout_tails);
        modesView = findViewById(R.id.layout_modes);
        replyView = findViewById(R.id.layout_reply_modes);
        tailArrow = (ImageView) findViewById(R.id.image_tail_arrow);
        modeArrow = (ImageView) findViewById(R.id.image_mode_arrow);
        replyArrow = (ImageView) findViewById(R.id.reply_mode_arrow);

        buttonDefault = (RadioButton) findViewById(R.id.button_use_default);
        buttonPhone = (RadioButton) findViewById(R.id.button_use_phone);
        buttonCustom = (RadioButton) findViewById(R.id.button_use_custom);
        tailText = (EditText) findViewById(R.id.text_tail);

        buttonAlways = (RadioButton) findViewById(R.id.button_always_load);
        buttonNever = (RadioButton) findViewById(R.id.button_never_load);
        buttonWifi = (RadioButton) findViewById(R.id.button_wifi_only);

        checkBox = (CheckBox) findViewById(R.id.check_homepage);
        checkSimple = (CheckBox) findViewById(R.id.check_simple);
        checkGroup = (CheckBox) findViewById(R.id.check_group_first);
        checkBadge = (CheckBox) findViewById(R.id.check_badge_fucker);

        buttonDefault.setOnCheckedChangeListener(this);
        buttonPhone.setOnCheckedChangeListener(this);
        buttonCustom.setOnCheckedChangeListener(this);

        buttonAlways.setOnCheckedChangeListener(this);
        buttonNever.setOnCheckedChangeListener(this);
        buttonWifi.setOnCheckedChangeListener(this);

        checkBox.setOnCheckedChangeListener(this);
        checkSimple.setOnCheckedChangeListener(this);
        checkGroup.setOnCheckedChangeListener(this);
        checkBadge.setOnCheckedChangeListener(this);

        imageModeView.setOnClickListener(this);
        replyModeView.setOnClickListener(this);
        customTailView.setOnClickListener(this);
        logInOutView.setOnClickListener(this);
        updateView.setOnClickListener(this);
        aboutView.setOnClickListener(this);
        tailsView.setVisibility(View.GONE);
        modesView.setVisibility(View.GONE);
        updateText.setText("当前版本 v" + BuildConfig.VERSION_NAME);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (UserAPI.isLoggedIn()) {
            logText.setText(R.string.log_out);
        } else {
            logText.setText(R.string.log_in);
        }

        checkBox.setChecked(PrefsUtil.readBoolean(Keys.Key_Image_No_Load_Homepage, false));

        int mode = Config.getImageLoadMode();
        switch (mode) {
            case ImageLoadMode.MODE_ALWAYS_LOAD:
                imageText.setText(R.string.mode_always_load);
                buttonAlways.setChecked(true);
                checkBox.setEnabled(true);
                break;
            case ImageLoadMode.MODE_NEVER_LOAD:
                imageText.setText(R.string.mode_never_load);
                buttonNever.setChecked(true);
                checkBox.setEnabled(false);
                break;
            case ImageLoadMode.MODE_LOAD_WHEN_WIFI:
                imageText.setText(R.string.mode_load_load_when_wifi);
                buttonWifi.setChecked(true);
                checkBox.setEnabled(true);
                break;
        }

        checkSimple.setChecked(PrefsUtil.readBoolean(Keys.Key_Reply_With_Simple, false));
        checkGroup.setChecked(PrefsUtil.readBoolean(Keys.Key_Show_Group_First_Homepage, false));
        checkBadge.setChecked(PrefsUtil.readBoolean(Keys.Key_I_Hate_Badge, false));

        switch (PrefsUtil.readInt(Keys.Key_Use_Tail_Type, TailType.Type_Use_Default_Tail)) {
            case TailType.Type_Use_Default_Tail:
                buttonDefault.setChecked(true);
                tailText.setText(Tail.getDefaultPlainTail());
                tailText.setEnabled(false);
                break;
            case TailType.Type_Use_Phone_Tail:
                buttonPhone.setChecked(true);
                tailText.setText(Tail.getPhonePlainTail());
                tailText.setEnabled(false);
                break;
            case TailType.Type_Use_Custom_Tail:
                buttonCustom.setChecked(true);
                tailText.setText(PrefsUtil.readString(Keys.Key_Custom_Tail, ""));
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
            case R.id.layout_reply_mode:
                popupReplyMode();
                break;
            case R.id.layout_custom_tail:
                toggleCustomTailLayout();
                break;
            case R.id.layout_log_in_out:
                toggleLoginState();
                break;
            case R.id.layout_app_update:
                checkUpdate();
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

    private void popupReplyMode() {
        if (replyView.getVisibility() == View.VISIBLE) {
            replyView.setVisibility(View.GONE);
            replyArrow.setRotation(0);
        } else {
            replyView.setVisibility(View.VISIBLE);
            replyArrow.setRotation(-90);
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
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.hint)
                    .setMessage(R.string.ok_to_logout)
                    .setPositiveButton(R.string.log_out, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UserAPI.logout();
                            Mob.onEvent(Mob.Event_Logout);
                            logText.setText(R.string.log_in);
                            EventBus.getDefault().post(new LoginStateChangedEvent());
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create();
            dialog.show();
        } else {
            startOneActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void checkUpdate() {
        UpdateChecker.getInstance(this, null).checkForUpdate();
    }

    private void showAboutApp() {
        UrlCheckUtil.openWithBrowser("https://github.com/NashLegend/SourceWall/blob/master/README.md");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.check_homepage) {
            PrefsUtil.saveBoolean(Keys.Key_Image_No_Load_Homepage, isChecked);
        } else if (buttonView.getId() == R.id.check_simple) {
            PrefsUtil.saveBoolean(Keys.Key_Reply_With_Simple, isChecked);
        } else if (buttonView.getId() == R.id.check_group_first) {
            PrefsUtil.saveBoolean(Keys.Key_Show_Group_First_Homepage, isChecked);
        } else if (buttonView.getId() == R.id.check_badge_fucker) {
            PrefsUtil.saveBoolean(Keys.Key_I_Hate_Badge, isChecked);
        } else {
            if (isChecked) {
                switch (buttonView.getId()) {
                    case R.id.button_use_default:
                        tailText.setText(Tail.getDefaultPlainTail());
                        tailText.setEnabled(false);
                        break;
                    case R.id.button_use_phone:
                        tailText.setText(Tail.getPhonePlainTail());
                        tailText.setEnabled(false);
                        break;
                    case R.id.button_use_custom:
                        String cus = PrefsUtil.readString(Keys.Key_Custom_Tail, "");
                        if (!TextUtils.isEmpty(cus)) {
                            tailText.setText(cus);
                        }
                        tailText.setEnabled(true);
                        break;
                    case R.id.button_always_load:
                        imageText.setText(R.string.mode_always_load);
                        checkBox.setEnabled(true);
                        break;
                    case R.id.button_never_load:
                        imageText.setText(R.string.mode_never_load);
                        checkBox.setEnabled(false);
                        break;
                    case R.id.button_wifi_only:
                        imageText.setText(R.string.mode_load_load_when_wifi);
                        checkBox.setEnabled(true);
                        break;
                }
            }
        }
    }

    @Override
    protected void onPause() {
        String preTail = Tail.getSimpleReplyTail();
        if (buttonDefault.isChecked()) {
            PrefsUtil.saveInt(Keys.Key_Use_Tail_Type, TailType.Type_Use_Default_Tail);
        } else if (buttonPhone.isChecked()) {
            PrefsUtil.saveInt(Keys.Key_Use_Tail_Type, TailType.Type_Use_Phone_Tail);
        } else {
            PrefsUtil.saveInt(Keys.Key_Use_Tail_Type, TailType.Type_Use_Custom_Tail);
            PrefsUtil.saveString(Keys.Key_Custom_Tail, tailText.getText().toString());
        }
        String crtTail = Tail.getSimpleReplyTail();

        if (crtTail.equals(preTail)) {
            Mob.onEvent(Mob.Event_Modify_Tail);
        }

        if (buttonAlways.isChecked()) {
            PrefsUtil.saveInt(Keys.Key_Image_Load_Mode, ImageLoadMode.MODE_ALWAYS_LOAD);
        } else if (buttonNever.isChecked()) {
            PrefsUtil.saveInt(Keys.Key_Image_Load_Mode, ImageLoadMode.MODE_NEVER_LOAD);
        } else {
            PrefsUtil.saveInt(Keys.Key_Image_Load_Mode, ImageLoadMode.MODE_LOAD_WHEN_WIFI);
        }
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

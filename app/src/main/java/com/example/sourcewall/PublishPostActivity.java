package com.example.sourcewall;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.APIBase;
import com.example.sourcewall.connection.api.PostAPI;
import com.example.sourcewall.dialogs.InputDialog;
import com.example.sourcewall.util.Config;
import com.example.sourcewall.util.FileUtil;
import com.example.sourcewall.util.ImageFetcher.AsyncTask;
import com.example.sourcewall.util.ToastUtil;

import java.io.File;


public class PublishPostActivity extends SwipeActivity implements View.OnClickListener {
    EditText titleEditText;
    EditText bodyEditText;
    ImageButton publishButton;
    ImageButton imgButton;
    ImageButton insertButton;
    ImageButton cameraButton;
    ImageButton linkButton;
    ProgressBar uploadingProgress;
    ProgressDialog progressDialog;
    String tmpImagePath;
    Toolbar toolbar;
    String group_id = "";
    String group_name = "";
    String csrf = "";
    String topic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_post);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        titleEditText = (EditText) findViewById(R.id.text_post_title);
        bodyEditText = (EditText) findViewById(R.id.text_post_body);
        publishButton = (ImageButton) findViewById(R.id.btn_publish);
        imgButton = (ImageButton) findViewById(R.id.btn_add_img);
        insertButton = (ImageButton) findViewById(R.id.btn_insert_img);
        cameraButton = (ImageButton) findViewById(R.id.btn_camera);
        linkButton = (ImageButton) findViewById(R.id.btn_link);
        uploadingProgress = (ProgressBar) findViewById(R.id.prg_uploading_img);
        publishButton.setOnClickListener(this);
        imgButton.setOnClickListener(this);
        insertButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        linkButton.setOnClickListener(this);
    }

    private void invokeImageDialog() {
        String[] ways = {getString(R.string.add_image_from_disk),
                getString(R.string.add_image_from_camera),
                getString(R.string.add_image_from_link)};
        new AlertDialog.Builder(this).setTitle(R.string.way_to_add_image).setItems(ways, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, 1024);
                        break;
                    case 1:
                        invokeCamera();
                    case 2:
                        invokeImageUrlDialog();
                        break;
                }
            }
        }).create().show();
    }

    private String getPossibleUrlFromClipBoard() {
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = manager.getPrimaryClip();
        String chars = "";
        if (clip != null && clip.getItemCount() > 0) {
            String tmpChars = (clip.getItemAt(0).coerceToText(this).toString()).trim();
            if (tmpChars.startsWith("http://") || tmpChars.startsWith("https://")) {
                chars = tmpChars;
            }
        }
        return chars;
    }

    private void invokeImageUrlDialog() {
        InputDialog.Builder builder = new InputDialog.Builder(this);
        builder.setTitle(R.string.input_image_url);
        builder.setCancelable(true);
        builder.setCanceledOnTouchOutside(false);
        builder.setSingleLine();
        builder.setInputText(getPossibleUrlFromClipBoard());
        builder.setOnClickListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    InputDialog d = (InputDialog) dialog;
                    String text = d.InputString;
                    insertImagePath(text.trim());
                }
            }
        });
        InputDialog inputDialog = builder.create();
        inputDialog.show();
    }

    public void uploadImage(String path) {
        if (FileUtil.isImage(path)) {
            File file = new File(path);
            if (file.exists()) {
                ImageUploadTask task = new ImageUploadTask();
                task.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR, path);
            } else {
                ToastUtil.toast(R.string.file_not_exists);
            }
        } else {
            ToastUtil.toast(R.string.file_not_image);
        }
    }

    private void doneUploadingImage(String url) {
        // tap to insert image
        tmpImagePath = url;
        setImageButtonsPrepared();
    }

    /**
     * 插入图片
     */
    private void insertImagePath(String url) {
        bodyEditText.getText().insert(bodyEditText.getSelectionStart(), "[image]" + url + "[/image]");
        resetImageButtons();
    }

    private void invokeCamera() {
        //TODO
    }

    /**
     * 插入链接
     */
    private void insertLink() {
        //TODO
        InputDialog.Builder builder = new InputDialog.Builder(this);
        builder.setTitle(R.string.input_link_url);
        builder.setCancelable(true);
        builder.setCanceledOnTouchOutside(false);
        builder.setTwoLine();
        builder.setInputText(getPossibleUrlFromClipBoard());
        builder.setOnClickListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    InputDialog d = (InputDialog) dialog;
                    String url = d.InputString;
                    String title = d.InputString2;
                    String result = "[url=" + url + "]" + title + "[/url]";
                    bodyEditText.getText().insert(bodyEditText.getSelectionStart(), result);
                }
            }
        });
        InputDialog inputDialog = builder.create();
        inputDialog.show();
    }

    private void publishPost() {
        if (!TextUtils.isEmpty(titleEditText.getText().toString().trim())) {
            ToastUtil.toast(R.string.title_cannot_be_empty);
            return;
        }

        if (!TextUtils.isEmpty(bodyEditText.getText().toString().trim())) {
            ToastUtil.toast(R.string.content_cannot_be_empty);
            return;
        }

        if (!TextUtils.isEmpty(csrf)) {
            ToastUtil.toast("No csrf_token");
            return;
        }

        PublishPostTask task = new PublishPostTask();
        String title = titleEditText.getText().toString();
        String body = bodyEditText.getText().toString() + Config.getComplexReplyTail();
        task.execute(group_id, csrf, title, body, topic);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_publish:
                publishPost();
                break;
            case R.id.btn_add_img:
                invokeImageDialog();
                break;
            case R.id.btn_insert_img:
                insertImagePath(tmpImagePath);
                break;
            case R.id.btn_camera:
                invokeCamera();
                break;
            case R.id.btn_link:
                insertLink();
                break;
        }
    }

    class PublishPostTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(PublishPostActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage(getString(R.string.message_replying));
            progressDialog.show();
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            String group_id = params[0];
            String csrf = params[1];
            String title = params[2];
            String body = params[3];
            String topic = params[4];
            return PostAPI.publishPost(group_id, csrf, title, body, topic);
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            progressDialog.dismiss();
            if (resultObject.ok) {
                ToastUtil.toast(R.string.reply_ok);
                setResult(RESULT_OK);
                finish();
            } else {
                ToastUtil.toast(R.string.reply_failed);
            }
        }
    }

    private void resetImageButtons() {
        tmpImagePath = "";
        insertButton.setVisibility(View.GONE);
        imgButton.setVisibility(View.VISIBLE);
        uploadingProgress.setVisibility(View.GONE);
    }

    private void setImageButtonsUploading() {
        insertButton.setVisibility(View.GONE);
        imgButton.setVisibility(View.GONE);
        uploadingProgress.setVisibility(View.VISIBLE);
    }

    private void setImageButtonsPrepared() {
        insertButton.setVisibility(View.VISIBLE);
        imgButton.setVisibility(View.GONE);
        uploadingProgress.setVisibility(View.GONE);
    }

    class ImageUploadTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            setImageButtonsUploading();
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            String path = params[0];
            return APIBase.uploadImage(path, true);
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                // tap to insert image
                doneUploadingImage((String) resultObject.result);
            } else {
                resetImageButtons();
                ToastUtil.toast("Upload Failed");
            }
        }

        @Override
        protected void onCancelled() {
            resetImageButtons();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1024 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String path = FileUtil.getActualPath(this, uri);
            if (!TextUtils.isEmpty(path)) {
                uploadImage(path);
            } else {
                //么有图
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_publish_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

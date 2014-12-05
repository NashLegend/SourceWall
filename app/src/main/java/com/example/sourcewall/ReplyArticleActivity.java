package com.example.sourcewall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.APIBase;
import com.example.sourcewall.connection.api.ArticleAPI;
import com.example.sourcewall.dialogs.InputDialog;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.FileUtil;
import com.example.sourcewall.util.ImageFetcher.AsyncTask;
import com.example.sourcewall.util.ToastUtil;

import java.io.File;

public class ReplyArticleActivity extends ActionBarActivity implements View.OnClickListener {

    EditText editText;
    Article article;
    Button publishButton;
    Button imgButton;
    Button insertButton;
    ProgressBar uploadingProgress;
    String tmpImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_article);
        article = (Article) getIntent().getSerializableExtra(Consts.Extra_Article);
        editText = (EditText) findViewById(R.id.text_reply);
        publishButton = (Button) findViewById(R.id.btn_publish);
        imgButton = (Button) findViewById(R.id.btn_add_img);
        insertButton = (Button) findViewById(R.id.btn_insert_img);
        uploadingProgress = (ProgressBar) findViewById(R.id.prg_uploading_img);
        publishButton.setOnClickListener(this);
        imgButton.setOnClickListener(this);
        insertButton.setOnClickListener(this);
    }

    private void invokeImageDialog() {
        String[] ways = {getResources().getString(R.string.add_image_from_disk), getResources().getString(R.string.add_image_from_link)};
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
                        invokeUrlDialog();
                        break;
                }
            }
        }).create().show();
    }

    private void invokeUrlDialog() {
        InputDialog.Builder builder = new InputDialog.Builder(this);
        builder.setTitle(R.string.sample_title);
        builder.setCancelable(true);
        builder.setSingleLine();
        builder.setCanceledOnTouchOutside(false);
        builder.setOnClickListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    InputDialog d = (InputDialog) dialog;
                    String text = d.InputString;
                    ToastUtil.toast(text);
                } else {
                    // cancel recommend
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
                task.execute(path);
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
     *
     */
    private void insertImagePath() {
        editText.getText().insert(editText.getSelectionStart(), tmpImagePath);
        resetImageButtons();
    }

    private void publishReply(String rep) {
        PublishReplyTask task = new PublishReplyTask();
        task.execute(article.getId(), rep);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_publish:
                if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
                    publishReply(editText.getText().toString());
                } else {
                    ToastUtil.toast(R.string.content_cannot_be_empty);
                }
                break;
            case R.id.btn_add_img:
                invokeImageDialog();
                break;
            case R.id.btn_insert_img:
                insertImagePath();
                break;
        }
    }

    class PublishReplyTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            String id = params[0];
            String content = params[1];
            return ArticleAPI.replyArticle(id, content);
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                ToastUtil.toast(R.string.reply_ok);
                finish();
            } else {
                ToastUtil.toast(R.string.reply_failed);
            }
        }
    }

    private void resetImageButtons() {
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
        getMenuInflater().inflate(R.menu.menu_reply_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

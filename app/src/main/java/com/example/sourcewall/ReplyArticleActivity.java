package com.example.sourcewall;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.APIBase;
import com.example.sourcewall.connection.api.ArticleAPI;
import com.example.sourcewall.dialogs.InputDialog;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.model.SimpleComment;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.FileUtil;
import com.example.sourcewall.util.ImageFetcher.AsyncTask;
import com.example.sourcewall.util.MDUtil;
import com.example.sourcewall.util.RegUtil;
import com.example.sourcewall.util.ToastUtil;

import java.io.File;

public class ReplyArticleActivity extends ActionBarActivity implements View.OnClickListener {

    EditText editText;
    TextView hostText;
    Article article;
    ImageButton publishButton;
    ImageButton imgButton;
    ImageButton insertButton;
    ImageButton cameraButton;
    ImageButton linkButton;
    ProgressBar uploadingProgress;
    String tmpImagePath;
    Toolbar toolbar;
    SimpleComment comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_article);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        article = (Article) getIntent().getSerializableExtra(Consts.Extra_Article);
        comment = (SimpleComment) getIntent().getSerializableExtra(Consts.Extra_Simple_Comment);
        editText = (EditText) findViewById(R.id.text_reply);
        hostText = (TextView) findViewById(R.id.text_reply_host);
        if (comment != null) {
            hostText.setVisibility(View.VISIBLE);
            String cont = RegUtil.html2PlainTextWithoutBlockQuote(comment.getContent());
            if (cont.length() > 100) {
                cont = cont.substring(0, 100) + "...";
            }
            hostText.setText("引用@" + comment.getAuthor() + " 的话：" + cont);
        }
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
        String[] ways = {getResources().getString(R.string.add_image_from_disk),
                getResources().getString(R.string.add_image_from_camera),
                getResources().getString(R.string.add_image_from_link)};
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

    private String getPossibleUrlFromClipBoard(){
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = manager.getPrimaryClip();
        String chars="";
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
     * 插入图片
     */
    private void insertImagePath(String url) {
        editText.getText().insert(editText.getSelectionStart(), "[image]" + url + "[/image]");
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
                    editText.getText().insert(editText.getSelectionStart(), result);
                }
            }
        });
        InputDialog inputDialog = builder.create();
        inputDialog.show();
    }

    private void publishReply(String rep) {
        PublishReplyTask task = new PublishReplyTask();
        String header = "";
        String tail = "\n\n[blockquote]Send by [url=https://github.com/NashLegend/SourceWall/]The Great Source Wall[/url][/blockquote]";
        if (comment != null) {
            header = "[blockquote]" + hostText.getText() + "[/blockquote]";
        }
        task.execute(article.getId(), header, rep, tail, Simple_Reply);
    }

    private void publishAdvancedReply(String rep) {
        PublishReplyTask task = new PublishReplyTask();
        String header = "";
        if (comment != null) {
            header = "<blockquote>" + hostText.getText() + "<blockquote>";
        }
        String tail = "<p></p><p>From <a href=\"https://github.com/NashLegend/SourceWall\" target=\"_blank\">The Great Source Wall</a></p>";
        task.execute(article.getId(), header, rep, tail, Advanced_Reply);
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

    final String Simple_Reply = "Simple_Reply";
    final String Advanced_Reply = "Advanced_Reply";

    class PublishReplyTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResultObject doInBackground(String... params) {
            String id = params[0];
            String header = params[1];
            String content = params[2];
            String tail = params[3];
            String reply_format = params[4];
            String result;
            if (Advanced_Reply.equals(reply_format)) {
                ResultObject resultObject = MDUtil.parseMarkdownByGitHub(content);
                if (resultObject.ok) {
                    content = (String) resultObject.result;
                } else {
                    content = MDUtil.Markdown2HtmlDumb(content);
                }
                result = header + content + tail;
                return ArticleAPI.replyArticleAdvanced(id, result);
            }
            result = header + content + tail;
            return ArticleAPI.replyArticle(id, result);
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
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

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
import android.widget.EditText;

import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.APIBase;
import com.example.sourcewall.connection.api.ArticleAPI;
import com.example.sourcewall.model.Article;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.FileUtil;
import com.example.sourcewall.util.ImageFetcher.AsyncTask;
import com.example.sourcewall.util.ToastUtil;

public class ReplyArticleActivity extends ActionBarActivity implements View.OnClickListener {

    EditText reply;
    Article article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_article);
        article = (Article) getIntent().getSerializableExtra(Consts.Extra_Article);
        reply = (EditText) findViewById(R.id.text_reply);
        findViewById(R.id.btn_publish).setOnClickListener(this);
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
                        //Net Image
                        break;
                }
            }
        }).create().show();
    }

    public void uploadImage(String path) {
        ImageUploadTask task = new ImageUploadTask();
        task.execute(path);
    }

    private void publishReply(String rep) {
        PublishReplyTask task = new PublishReplyTask();
        task.execute(article.getId(), rep);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_publish:
                if (!TextUtils.isEmpty(reply.getText().toString().trim())) {
                    publishReply(reply.getText().toString());
                } else {
                    // empty
                }
                break;
            case R.id.btn_add_img:
                invokeImageDialog();
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
                //notify ok
                ToastUtil.toast("Reply OK");
                finish();
            } else {
                //failed
                ToastUtil.toast("Reply Failed");
            }
        }
    }

    class ImageUploadTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                String url = (String) resultObject.result;
                ToastUtil.toast("Upload OK,url is : " + url);
            } else {
                //upload failed
                ToastUtil.toast("Upload Failed");
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
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

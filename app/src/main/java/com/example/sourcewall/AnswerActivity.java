package com.example.sourcewall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sourcewall.connection.ResultObject;
import com.example.sourcewall.connection.api.QuestionAPI;
import com.example.sourcewall.model.Question;
import com.example.sourcewall.model.QuestionAnswer;
import com.example.sourcewall.util.Consts;
import com.example.sourcewall.util.ToastUtil;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

public class AnswerActivity extends SwipeActivity implements View.OnClickListener {

    View authorLayout;
    Toolbar toolbar;
    View bottomLayout;
    WebView webView;
    ImageView avatar;
    TextView questionText;
    TextView authorName;
    TextView authorTitle;
    TextView supportText;
    View supportView;
    Question question;
    QuestionAnswer answer;
    FloatingActionButton replyButton;
    FloatingActionButton notAnButton;
    FloatingActionButton thankButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        answer = (QuestionAnswer) getIntent().getSerializableExtra(Consts.Extra_Answer);
        question = (Question) getIntent().getSerializableExtra(Consts.Extra_Question);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        authorLayout = findViewById(R.id.layout_author);
        bottomLayout = findViewById(R.id.layout_operation);
        webView = (WebView) findViewById(R.id.web_content);
        questionText = (TextView) findViewById(R.id.text_title);
        avatar = (ImageView) findViewById(R.id.image_avatar);
        authorName = (TextView) findViewById(R.id.text_author);
        authorTitle = (TextView) findViewById(R.id.text_author_title);
        supportView = findViewById(R.id.layout_opinion);
        supportText = (TextView) findViewById(R.id.text_num_support);
        replyButton = (FloatingActionButton) findViewById(R.id.button_reply);
        notAnButton = (FloatingActionButton) findViewById(R.id.button_Bury);
        thankButton = (FloatingActionButton) findViewById(R.id.button_thank);

        questionText.setText(question.getTitle());
        supportText.setText(answer.getUpvoteNum() + "");
        authorName.setText(answer.getAuthor());
        authorTitle.setText(answer.getAuthorTitle());
        Picasso.with(this).load(answer.getAuthorAvatarUrl())
                .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen)
                .into(avatar);

        questionText.setOnClickListener(this);
        supportView.setOnClickListener(this);
        replyButton.setOnClickListener(this);
        notAnButton.setOnClickListener(this);
        thankButton.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_answer, menu);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_title:
                finish();
                break;
            case R.id.layout_opinion:
                invokeOpinionDialog();
                break;
            case R.id.button_reply:
                replyAnswer();
                break;
            case R.id.button_Bury:
                buryAnswer();
                break;
            case R.id.button_thank:
                thankAnswer();
                break;
        }
    }

    private void replyAnswer() {
        Intent intent = new Intent(this, SimpleReplyActivity.class);
        intent.putExtra(Consts.Extra_Ace_Model, answer);
        startActivity(intent);
    }

    private void invokeOpinionDialog() {
        String[] operations = {getString(R.string.action_support), getString(R.string.action_oppose)};
        new AlertDialog.Builder(this).setTitle("").setItems(operations, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Boolean support = which == 0;
                OpinionTask task = new OpinionTask();
                task.execute(support);
            }
        }).create().show();
    }

    private void buryAnswer() {
        BuryTask task = new BuryTask();
        task.execute();
    }

    private void thankAnswer() {
        ThankTask task = new ThankTask();
        task.execute();
    }

    class OpinionTask extends AsyncTask<Boolean, Integer, ResultObject> {

        boolean isSupport;

        @Override
        protected ResultObject doInBackground(Boolean... params) {
            isSupport = params[0];
            if (isSupport) {
                return QuestionAPI.supportAnswer(answer.getID());
            }
            return QuestionAPI.opposeAnswer(answer.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                answer.setUpvoteNum(answer.getUpvoteNum() + 1);
                supportText.setText(answer.getUpvoteNum() + "");
                ToastUtil.toast((isSupport ? "赞同" : "反对") + "成功");
            } else {
                ToastUtil.toast((isSupport ? "赞同" : "反对") + "未遂");
            }
        }
    }

    class BuryTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(String... params) {
            return QuestionAPI.buryAnswer(answer.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                //TODO
                ToastUtil.toast("Bury 成功");
            } else {
                ToastUtil.toast("Bury 未遂");
            }

        }
    }

    class ThankTask extends AsyncTask<String, Integer, ResultObject> {

        @Override
        protected ResultObject doInBackground(String... params) {
            return QuestionAPI.thankAnswer(answer.getID());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                //TODO
                ToastUtil.toast("感谢成功");
            } else {
                ToastUtil.toast("感谢未遂");
            }
        }
    }
}

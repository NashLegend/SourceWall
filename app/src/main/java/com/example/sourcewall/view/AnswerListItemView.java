package com.example.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.model.QuestionAnswer;
import com.squareup.picasso.Picasso;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class AnswerListItemView extends AceView<QuestionAnswer> {
    private QuestionAnswer answer;
    private WebView contentView;
    private TextView authorView;
    private ImageView avatar;
    private TextView dateView;
    private TextView authorTitleView;
    private ImageButton upButton;
    private ImageButton downButton;

    public AnswerListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_answer_item_view, this);
        contentView = (WebView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        authorTitleView = (TextView) findViewById(R.id.text_author_title);
        dateView = (TextView) findViewById(R.id.text_date);
        avatar = (ImageView) findViewById(R.id.image_avatar);
//        upButton = (ImageButton) findViewById(R.id.button_upvote);
//        downButton = (ImageButton) findViewById(R.id.button_downvote);
    }

    public AnswerListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnswerListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(QuestionAnswer model) {
        answer = model;
        authorView.setText(answer.getAuthor());
        authorTitleView.setText(answer.getAuthorTitle());
        dateView.setText(answer.getDate_created());
        Picasso.with(getContext()).load(answer.getAuthorAvatarUrl())
                .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen)
                .into(avatar);
        String html = "<html>\n" +
                " <head> \n" +
                "  <meta charset=\"UTF-8\" /> \n" +
                "  <meta content=\"width=device-width,initial-scale=1.0,maximum-scale=1,minimum-scale=1,user-scalable=no\" name=\"viewport\" /> \n" +
                "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/msite/styles/27dc13be.m.css\" /> \n" +
                "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/msite/styles/cfb7569b.ask.css\" type=\"text/css\" /> \n" +
                "  <style id=\"style-1-cropbar-clipper\">/* Copyright 2014 Evernote Corporation. All rights reserved. */\n" +
                ".en-markup-crop-options {\n" +
                "    top: 18px !important;\n" +
                "    left: 50% !important;\n" +
                "    margin-left: -100px !important;\n" +
                "    width: 200px !important;\n" +
                "    border: 2px rgba(255,255,255,.38) solid !important;\n" +
                "    border-radius: 4px !important;\n" +
                "}\n" +
                "\n" +
                ".en-markup-crop-options div div:first-of-type {\n" +
                "    margin-left: 0px !important;\n" +
                "}\n" +
                "</style>\n" +
                " </head> \n" +
                " <body> \n" +
                "  <div class=\"msite-container \"> \n" +
                "   <div> \n" +
                "    <div class=\"quality-answer\"> \n" +
                "     <section class=\"content-block\"> \n" +
                "      <div id=\"answersList\" class=\"content-main\"> \n" +
                "       <div id=\"answer755710\" class=\"answer-padding15 answerItem\" style=\"-webkit-transform-origin: 0px 0px; opacity: 1; -webkit-transform: scale(1, 1);\"> \n" +
                "        <div class=\"askcontent\"> " + answer.getContent() +
                "        </div>\n" +
                "       </div> \n" +
                "      </div> \n" +
                "     </section> \n" +
                "    </div> \n" +
                "   </div> \n" +
                "  </div> \n" +
                " </body>\n" +
                "</html>";
        contentView.getSettings().setDefaultTextEncodingName("UTF-8");
        contentView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "charset=UTF-8", null);
    }

    @Override
    public QuestionAnswer getData() {
        return answer;
    }
}

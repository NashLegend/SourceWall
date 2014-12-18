package com.example.sourcewall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.model.Question;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class QuestionView extends AceView<Question> {
    private Question question;
    private TextView titleView;
    private TextView authorView;
    private TextView dateView;
    private WebView contentView;

    public QuestionView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_question_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        contentView = (WebView) findViewById(R.id.web_content);
    }

    public QuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuestionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Question model) {
        if (question==null){
            question = model;
            titleView.setText(question.getTitle());
            authorView.setText(question.getAuthor());
            dateView.setText(question.getDate());
            String html = "<html>\n" +
                    " <head> \n" +
                    "  <meta charset=\"UTF-8\" /> \n" +
                    "  <meta content=\"width=device-width,initial-scale=1.0,maximum-scale=1,minimum-scale=1,user-scalable=no\" name=\"viewport\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/msite/styles/27dc13be.m.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/msite/styles/cfb7569b.ask.css\" type=\"text/css\" /> \n" +
                    "  <style id=\"style-1-cropbar-clipper\">\n" +
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
                    "    <article class=\"content-main question\"> \n" +
                    "     <div id=\"askContent\" class=\"html-text-mixin\" style=\"position: relative;\">" + question.getContent() +
                    "     </div> \n" +
                    "    </article> \n" +
                    "   </div> \n" +
                    "  </div> \n" +
                    " </body>\n" +
                    "</html>";
            contentView.getSettings().setDefaultTextEncodingName("UTF-8");
            contentView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "charset=UTF-8", null);
        }
    }

    @Override
    public Question getData() {
        return question;
    }
}

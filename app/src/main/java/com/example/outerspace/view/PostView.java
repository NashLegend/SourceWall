package com.example.outerspace.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.TextView;

import com.example.outerspace.R;
import com.example.outerspace.model.Post;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class PostView extends AceView<Post> {
    private Post post;
    private TextView titleView;
    private WebView contentView;
    private TextView authorView;
    private TextView dateView;

    public PostView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_post_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        contentView = (WebView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
    }

    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Post model) {
        post = model;
        titleView.setText(post.getTitle());
        authorView.setText(post.getAuthor());
        dateView.setText(post.getDate());
        String html = "<html>\n" +
                " <head> \n" +
                "  <meta charset=\"UTF-8\" /> \n" +
                "  <meta content=\"width=device-width,initial-scale=1.0,maximum-scale=1,minimum-scale=1,user-scalable=no\" name=\"viewport\" /> \n" +
                "  <link rel=\"stylesheet\" href=\"http://static.guokr.com/apps/msite/styles/27dc13be.m.css\" /> \n" +
                "  <link rel=\"stylesheet\" href=\"http://static.guokr.com/apps/group/styles/e8ff5a9c.gbbcode.css\" type=\"text/css\" /> \n" +
                "  <link rel=\"stylesheet\" href=\"http://static.guokr.com/apps/msite/styles/81e10205.group.css\" type=\"text/css\" /> \n" +
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
                "  <link rel=\"stylesheet\" href=\"http://bdimg.share.baidu.com/static/api/css/share_style0_16.css?v=f4b44e79.css\" />\n" +
                " </head> \n" +
                " <body> \n" +
//                    "  <div class=\"msite-container \"> \n" +
                "   <article id=\"contentMain\" class=\"content-main post\"> " + post.getContent() + "   </article> \n" +
//                    "  </div> \n" +
                " </body>\n" +
                "</html>";
        contentView.getSettings().setDefaultTextEncodingName("UTF-8");
        contentView.loadData(html, "text/html; charset=UTF-8", null);
    }
}

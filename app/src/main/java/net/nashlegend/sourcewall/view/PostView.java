package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.PostDetailAdapter;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.PostAPI;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Consts.Actions;
import net.nashlegend.sourcewall.util.Consts.Extras;
import net.nashlegend.sourcewall.util.Consts.Web;
import net.nashlegend.sourcewall.util.DateTimeUtil;
import net.nashlegend.sourcewall.util.ImageUtils;
import net.nashlegend.sourcewall.util.StyleChecker;
import net.nashlegend.sourcewall.view.common.WWebView;

import java.util.ArrayList;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by NashLegend on 2014/9/18 0018
 */
public class PostView extends AceView<Post> {
    private Post post;
    private TextView titleView;
    private WWebView contentView;
    private TextView authorView;
    private TextView dateView;
    private ImageView avatarImage;
    private View loadDesc;
    private PostDetailAdapter adapter;

    public PostView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_post_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        contentView = (WWebView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        avatarImage = (ImageView) findViewById(R.id.image_avatar);
        loadDesc = findViewById(R.id.view_load_latest);
        contentView.setBackgroundColor(getResources().getColor(R.color.list_background));
    }

    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(Post model) {
        if (post == null) {
            post = model;
            titleView.setText(post.getTitle());
            authorView.setText(post.getAuthor().getName());
            dateView.setText(DateTimeUtil.time2HumanReadable(post.getDate()));
            String html = StyleChecker.getPostHtml(post.getContent());
            contentView.setPrimarySource(post.getContent());
            contentView.loadDataWithBaseURL(Web.Base_Url, html, "text/html", "charset=UTF-8", null);
            if (Config.shouldLoadImage()) {
                ImageLoader.getInstance().displayImage(post.getAuthor().getAvatar(), avatarImage, ImageUtils.avatarOptions);
            } else {
                avatarImage.setImageResource(R.drawable.default_avatar);
            }
        } else {
            post = model;
        }

        if (post.isDesc()) {
            loadDesc.setVisibility(VISIBLE);
            contentView.setVisibility(GONE);
        } else {
            loadDesc.setVisibility(GONE);
            contentView.setVisibility(VISIBLE);
        }

        loadDesc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLatest();
            }
        });
    }

    private void loadLatest() {
        if (loadDesc.findViewById(R.id.text_header_load_hint).getVisibility() != View.VISIBLE) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Actions.Action_Start_Loading_Latest);
        App.getApp().sendBroadcast(intent);
        loadDesc.findViewById(R.id.text_header_load_hint).setVisibility(View.INVISIBLE);
        loadDesc.findViewById(R.id.progress_header_loading).setVisibility(View.VISIBLE);

        PostAPI
                .getPostReplies(post.getId(), post.getReplyNum(), 4999)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseObject<ArrayList<UComment>>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Intent intent = new Intent();
                        intent.setAction(Actions.Action_Finish_Loading_Latest);
                        intent.putExtra(Extras.Extra_Activity_Hashcode, getContext().hashCode());
                        App.getApp().sendBroadcast(intent);
                    }

                    @Override
                    public void onNext(ResponseObject<ArrayList<UComment>> result) {
                        loadDesc.findViewById(R.id.text_header_load_hint).setVisibility(View.VISIBLE);
                        loadDesc.findViewById(R.id.progress_header_loading).setVisibility(View.INVISIBLE);
                        if (result.ok) {
                            ArrayList<UComment> ars = result.result;
                            if (ars.size() > 0) {
                                adapter.addAllReversely(ars, 1);
                                adapter.notifyDataSetChanged();
                            }
                            post.setReplyNum(post.getReplyNum() + ars.size());
                        }
                        Intent intent = new Intent();
                        intent.setAction(Actions.Action_Finish_Loading_Latest);
                        intent.putExtra(Extras.Extra_Activity_Hashcode, getContext().hashCode());
                        App.getApp().sendBroadcast(intent);
                    }
                });
    }

    public void setAdapter(PostDetailAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public Post getData() {
        return post;
    }
}

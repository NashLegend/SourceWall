package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.nashlegend.sourcewall.AppApplication;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.PostDetailAdapter;
import net.nashlegend.sourcewall.commonview.AAsyncTask;
import net.nashlegend.sourcewall.commonview.WWebView;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.model.Post;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.request.api.PostAPI;
import net.nashlegend.sourcewall.util.Config;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.RoundTransformation;
import net.nashlegend.sourcewall.util.SharedPreferencesUtil;
import net.nashlegend.sourcewall.util.StyleChecker;

import java.util.ArrayList;

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
    private LoaderTask task;

    public PostView(Context context) {
        super(context);
        if (SharedPreferencesUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background_night));
        } else {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background));
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_post_view, this);
        titleView = (TextView) findViewById(R.id.text_title);
        contentView = (WWebView) findViewById(R.id.web_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        avatarImage = (ImageView) findViewById(R.id.image_avatar);
        loadDesc = findViewById(R.id.view_load_latest);

        Resources.Theme theme = getContext().getTheme();
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{R.attr.cardBackgroundColor});
        int colorBack = typedArray.getColor(0, 0);
        typedArray.recycle();
        contentView.setBackgroundColor(colorBack);
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
            authorView.setText(post.getAuthor());
            dateView.setText(post.getDate());
            String html = StyleChecker.getPostHtml(post.getContent());
            contentView.loadDataWithBaseURL(Consts.Base_Url, html, "text/html", "charset=UTF-8", null);
            if (Config.shouldLoadImage()) {
                Picasso.with(getContext()).load(post.getAuthorAvatarUrl())
                        .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen).placeholder(R.drawable.default_avatar)
                        .transform(new RoundTransformation(Color.parseColor("#00000000"), 0, true)).into(avatarImage);
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
        if (loadDesc.findViewById(R.id.text_header_load_hint).getVisibility() == View.VISIBLE) {
            cancelPotentialTask();
            task = new LoaderTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void cancelPotentialTask() {
        if (task != null && task.getStatus() == AAsyncTask.Status.RUNNING) {
            task.cancel(true);
            loadDesc.findViewById(R.id.text_header_load_hint).setVisibility(View.VISIBLE);
            loadDesc.findViewById(R.id.progress_header_loading).setVisibility(View.INVISIBLE);
        }
    }

    public void setAdapter(PostDetailAdapter adapter) {
        this.adapter = adapter;
    }

    class LoaderTask extends AAsyncTask<Integer, ResultObject, ResultObject<ArrayList<AceModel>>> {

        @Override
        protected void onPreExecute() {
            Intent intent = new Intent();
            intent.setAction(Consts.Action_Start_Loading_Latest);
            AppApplication.getApplication().sendBroadcast(intent);
            loadDesc.findViewById(R.id.text_header_load_hint).setVisibility(View.INVISIBLE);
            loadDesc.findViewById(R.id.progress_header_loading).setVisibility(View.VISIBLE);
        }

        @Override
        protected ResultObject<ArrayList<AceModel>> doInBackground(Integer... params) {
            return PostAPI.getPostCommentsFromJsonUrl(post.getId(), post.getReplyNum(), 1000);//1000足够了
        }

        @Override
        protected void onPostExecute(ResultObject<ArrayList<AceModel>> result) {
            loadDesc.findViewById(R.id.text_header_load_hint).setVisibility(View.VISIBLE);
            loadDesc.findViewById(R.id.progress_header_loading).setVisibility(View.INVISIBLE);
            if (result.ok) {
                ArrayList<AceModel> ars = result.result;
                if (ars.size() > 0) {
                    adapter.addAllReversely(ars, 1);
                    adapter.notifyDataSetChanged();
                }
                post.setReplyNum(post.getReplyNum() + ars.size());
            }
            Intent intent = new Intent();
            intent.setAction(Consts.Action_Finish_Loading_Latest);
            intent.putExtra(Consts.Extra_Activity_Hashcode, getContext().hashCode());
            AppApplication.getApplication().sendBroadcast(intent);
        }
    }

    @Override
    public Post getData() {
        return post;
    }
}

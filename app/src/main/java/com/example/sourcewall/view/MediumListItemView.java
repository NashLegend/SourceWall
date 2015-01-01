package com.example.sourcewall.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sourcewall.R;
import com.example.sourcewall.model.UComment;
import com.example.sourcewall.util.DisplayUtil;
import com.example.sourcewall.util.ImageUtil.ImageCache;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by NashLegend on 2014/9/18 0018.
 * Shared by PostComment and ArticleComment
 */
public class MediumListItemView extends AceView<UComment> {

    private TextView contentView;
    private TextView authorView;
    private TextView dateView;
    private TextView likesView;
    private TextView floorView;
    private ImageView avatarImage;
    private UComment comment;

    public MediumListItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_medium_comment_item_view, this);
        contentView = (TextView) findViewById(R.id.text_content);
        authorView = (TextView) findViewById(R.id.text_author);
        dateView = (TextView) findViewById(R.id.text_date);
        likesView = (TextView) findViewById(R.id.text_like_num);
        floorView = (TextView) findViewById(R.id.text_floor);
        avatarImage = (ImageView) findViewById(R.id.image_avatar);
    }

    public MediumListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediumListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(UComment model) {
        comment = model;
        authorView.setText(comment.getAuthor());
        dateView.setText(comment.getDate());
        likesView.setText(comment.getLikeNum() + "");
        floorView.setText(comment.getFloor());
        contentView.setText(Html.fromHtml(comment.getContent()));
        if (htmlTask != null && htmlTask.getStatus() == AsyncTask.Status.RUNNING) {
            htmlTask.cancel(true);
        }
        if (comment.getContent().contains("<img")) {
            htmlTask = new HtmlLoaderTask();
            htmlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, comment.getContent());
        }
        Picasso.with(getContext()).load(comment.getAuthorAvatarUrl())
                .resizeDimen(R.dimen.list_standard_comment_avatar_dimen, R.dimen.list_standard_comment_avatar_dimen)
                .into(avatarImage);
    }

    @Override
    public UComment getData() {
        return comment;
    }

    HtmlLoaderTask htmlTask;

    class HtmlLoaderTask extends AsyncTask<String, Integer, Spanned> {

        @Override
        protected Spanned doInBackground(String... params) {
            return Html.fromHtml(params[0], imageGetter, null);
        }

        @Override
        protected void onPostExecute(Spanned spanned) {
            contentView.setText(spanned);
        }

        Html.ImageGetter imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                float stretch = DisplayUtil.getPixelDensity(getContext());
                double maxWidth = (DisplayUtil.getScreenWidth(getContext()) * 0.9);
                Drawable drawable = null;
                File file = new File(ImageCache.getBitmapCacheFileDir(source));
                if (!file.exists()) {
                    ImageCache.downloadImageToFile(source, false);
                }
                if (file.exists()) {
                    //防止图片超出屏幕
                    drawable = decodeSampledBitmapFromFile(file.getAbsolutePath(), (int) (maxWidth / stretch));
                }

                if (drawable != null) {
                    int width = (int) (drawable.getIntrinsicWidth() * stretch);
                    int height = (int) (drawable.getIntrinsicHeight() * stretch);

                    System.out.println(width + " " + height);

                    if (width > maxWidth) {
                        height *= (maxWidth / width);
                        width = (int) maxWidth;
                    }

                    drawable.setBounds(0, 0, width, height);
                }
                return drawable;
            }
        };
    }

    public BitmapDrawable decodeSampledBitmapFromFile(String filename, int maxWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filename, options);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        return bitmapDrawable;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            final int halfWidth = width / 2;
            while ((halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}

package net.nashlegend.sourcewall;

import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.nashlegend.sourcewall.adapters.ImageAdapter;
import net.nashlegend.sourcewall.request.RequestCache;
import net.nashlegend.sourcewall.request.ResultObject;
import net.nashlegend.sourcewall.util.Consts;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ImageActivity extends BaseActivity {

    ArrayList<String> images;
    ViewPager pager;
    ImageAdapter adapter;
    TextView indicator;
    ImageButton downloadButton;
    View head;
    int imageCount = 0;

    @Override
    public void setTheme(int resId) {
        super.setTheme(R.style.ImageThemeNight);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        head = findViewById(R.id.layoutHead);
        indicator = (TextView) findViewById(R.id.tvIndicator);
        downloadButton = (ImageButton) findViewById(R.id.btn_download);
        pager = (ViewPager) findViewById(R.id.image_pager);
        adapter = new ImageAdapter(this);
        images = getIntent().getStringArrayListExtra(Consts.Extra_Image_String_Array);
        int position = getIntent().getIntExtra(Consts.Extra_Image_Current_Position, 0);

        if (images != null && images.size() > 0) {
            imageCount = images.size();
            adapter.addAll(images);
            pager.setAdapter(adapter);
            if (position < images.size()) {
                pager.setCurrentItem(position);
            } else {
                position = 0;
            }
            indicator.setText((position + 1) + "/" + imageCount);
        }

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                indicator.setText((position + 1) + "/" + imageCount);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });
    }

    private void download() {
        new DownloadTask().execute(images.get(pager.getCurrentItem()));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.scale_out_center);
    }

    class DownloadTask extends AsyncTask<String, Integer, ResultObject<String>> {

        @Override
        protected ResultObject<String> doInBackground(String... params) {
            ResultObject<String> resultObject = new ResultObject<>();
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_name));
                if ((folder.exists() || !folder.exists() && folder.mkdirs())) {
                    String url = params[0];
                    String filePath = RequestCache.getInstance().getCachedFile(url);
                    if (filePath == null || !new File(filePath).exists()) {
                        Picasso.with(ImageActivity.this).load(url).download();
                        filePath = RequestCache.getInstance().getCachedFile(url);
                    }
                    if (filePath != null && new File(filePath).exists()) {
                        String trimmedUrl = url.replaceAll("\\?.+", "");
                        String suffix = ".jpg";
                        if (trimmedUrl.lastIndexOf(".") > 0) {
                            suffix = trimmedUrl.substring(trimmedUrl.lastIndexOf(".")).toLowerCase();
                            if (suffix.length() > 5 || suffix.length() <= 1) {
                                suffix = ".jpg";
                            }
                        }
                        File srcFile = new File(filePath);
                        File destFile = new File(folder, srcFile.getName() + suffix);
                        resultObject.ok = copy2SingleFile(srcFile, destFile);
                        resultObject.result = destFile.getAbsolutePath();
                    }
                }
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResultObject<String> result) {
            if (result.ok) {
                MediaScannerConnection.scanFile(ImageActivity.this, new String[]{result.result}, null, null);
                toastSingleton(getString(R.string.hint_download_successfully_to) + result.result);
            } else {
                toastSingleton(R.string.hint_download_failed);
            }
        }
    }

    private static boolean copy2SingleFile(File sourceFile, File destFile) {
        if (destFile.exists()) {
            return true;
        }
        // 此处destFile一定不存在
        boolean copyOK = true;
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(destFile));
            byte[] buffer = new byte[1024 * 5];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (Exception e) {
            copyOK = false;
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                copyOK = false;
                e.printStackTrace();
            }
        }

        return copyOK;
    }
}

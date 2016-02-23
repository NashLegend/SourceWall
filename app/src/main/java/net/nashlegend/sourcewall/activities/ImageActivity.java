package net.nashlegend.sourcewall.activities;

import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.ImageAdapter;
import net.nashlegend.sourcewall.request.RequestCache;
import net.nashlegend.sourcewall.swrequest.ResponseObject;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.Mob;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        directlySetTheme(R.style.ImageThemeNight);
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
        MobclickAgent.onEvent(this, Mob.Event_Download_Image_In_Pager);
        new DownloadTask().execute(images.get(pager.getCurrentItem()));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.scale_out_center);
    }

    class DownloadTask extends AsyncTask<String, Integer, ResponseObject<String>> {

        @Override
        protected ResponseObject<String> doInBackground(String... params) {
            ResponseObject<String> resultObject = new ResponseObject<>();
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_name));
                if ((folder.exists() || !folder.exists() && folder.mkdirs())) {
                    String url = params[0];
                    if (url.startsWith("http")) {
                        String filePath = RequestCache.getInstance().getCachedFile(url);
                        if (filePath == null || !new File(filePath).exists()) {
                            // FIXME: 16/2/24 改为ImageLoader的
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
                    } else if (url.startsWith("data:image/")) {
                        try {
                            url = URLDecoder.decode(url, "utf-8");
                            Matcher matcher = Pattern.compile("data:image/(\\w{3,4});base64").matcher(url);
                            String suffix = ".jpg";
                            if (matcher.find()) {
                                suffix = matcher.group(1);
                            }
                            File destFile = new File(folder, System.currentTimeMillis() + "." + suffix);
                            String encodedBitmap = url.replaceAll("data:image/\\w{3,4};base64,", "");
                            byte[] data = Base64.decode(encodedBitmap, Base64.DEFAULT);
                            FileOutputStream outputStream = new FileOutputStream(destFile);
                            outputStream.write(data);
                            outputStream.flush();
                            outputStream.close();
                            resultObject.ok = true;
                            resultObject.result = destFile.getAbsolutePath();
                        } catch (Exception ignored) {

                        }
                    }
                }
            }
            return resultObject;
        }

        @Override
        protected void onPostExecute(ResponseObject<String> result) {
            if (result.ok) {
                MediaScannerConnection.scanFile(ImageActivity.this, new String[]{result.result}, null, null);
                toastSingleton(getString(R.string.hint_download_successfully_to) + new File(result.result).getParent());
            } else {
                toastSingleton(R.string.hint_download_failed);
            }
        }
    }

    /**
     * 保存Base64图片
     */
    private void saveBase64Image(String src, String path) {

        Matcher matcher = Pattern.compile("data:image/(\\w{3,4});base64").matcher(src);
        String suffix = ".jpg";
        if (matcher.find()) {
            suffix = matcher.group(1);
        }
        File destFile = new File(path, System.currentTimeMillis() + "." + suffix);
        try {
            String encodedBitmap = src.replaceAll("data:image/\\w{3,4};base64,", "");
            byte[] data = Base64.decode(encodedBitmap, Base64.DEFAULT);
            FileOutputStream outputStream = new FileOutputStream(destFile);
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
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

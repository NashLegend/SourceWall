package net.nashlegend.sourcewall.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import net.nashlegend.sourcewall.BuildConfig;
import net.nashlegend.sourcewall.model.UpdateInfo;
import net.nashlegend.sourcewall.request.RequestObject.SimpleCallBack;
import net.nashlegend.sourcewall.request.api.APIBase;

import static android.text.TextUtils.isEmpty;

/**
 * Created by NashLegend on 16/4/9.
 */
public class UpdateChecker {

    Activity activity;
    UpdateDelegate delegate;

    public static UpdateChecker getInstance(@NonNull Activity activity) {
        return getInstance(activity, null);
    }

    public static UpdateChecker getInstance(@NonNull final Activity activity, UpdateDelegate delegate) {
        return new UpdateChecker(activity, delegate);
    }

    private UpdateChecker(@NonNull final Activity activity, UpdateDelegate delegate) {
        this.activity = activity;
        if (delegate == null) {
            delegate = new SimpleUpdateDelegate();
        }
        this.delegate = delegate;
    }

    public interface UpdateDelegate {
        void beforeCheckUpdate();

        void afterCheckForUpdate();

        boolean shouldInterceptUpdate(@NonNull UpdateInfo updateInfo);

        void onNoUpdate();
    }

    public static class SimpleUpdateDelegate implements UpdateDelegate {

        @Override
        public void beforeCheckUpdate() {

        }

        @Override
        public void afterCheckForUpdate() {

        }

        @Override
        public boolean shouldInterceptUpdate(@NonNull UpdateInfo updateInfo) {
            return false;
        }

        @Override
        public void onNoUpdate() {

        }
    }

    public void checkForUpdate() {
        delegate.beforeCheckUpdate();
        APIBase.checkForUpdate(new SimpleCallBack<UpdateInfo>() {
            @Override
            public void onFailure() {
                delegate.afterCheckForUpdate();
            }

            @Override
            public void onSuccess(@NonNull final UpdateInfo info) {
                delegate.afterCheckForUpdate();
                if (activity.isFinishing() || delegate.shouldInterceptUpdate(info)) {
                    return;
                }
                if (info.getVersionCode() > BuildConfig.VERSION_CODE && !isEmpty(info.getUrl())) {
                    new AlertDialog.Builder(activity)
                            .setTitle("检查到新版本:" + info.getVersionName())
                            .setPositiveButton("下载更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    UrlCheckUtil.openWithBrowser(info.getUrl());
                                }
                            })
                            .setNegativeButton("下次再说", null)
                            .setMessage(!isEmpty(info.getUpdateInfo()) ? "本次更新内容\n\n" + info.getUpdateInfo() : "")
                            .show();
                } else {
                    delegate.onNoUpdate();
                }
            }
        });
    }
}

package com.example.sourcewall.dialogs;

import android.app.Dialog;
import android.content.Context;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class FavorDialog extends Dialog {

    public FavorDialog(Context context) {
        super(context);
    }

    public FavorDialog(Context context, int theme) {
        super(context, theme);
    }

    protected FavorDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static class Builder {
        private Context mContext;
        private FavorDialog dialog;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setTitle(String title) {
            return this;
        }

        public FavorDialog create() {
            dialog = new FavorDialog(mContext);

            return dialog;
        }
    }
}

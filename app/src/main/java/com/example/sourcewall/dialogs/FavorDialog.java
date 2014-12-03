package com.example.sourcewall.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.sourcewall.R;
import com.example.sourcewall.model.AceModel;
import com.example.sourcewall.view.FavorView;

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
        private FavorView favorView;
        private View okButton;
        private String title;
        private boolean cancelable = true;
        private boolean canceledOnTouchOutside = false;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public FavorDialog create(AceModel data) {
            dialog = new FavorDialog(mContext);
            favorView = new FavorView(mContext);
            okButton = favorView.findViewById(R.id.button_favor_dialog_ok);
            favorView.setData(data);
            dialog.setTitle(title);
            dialog.setCancelable(cancelable);
            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
            dialog.setContentView(favorView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            return dialog;
        }
    }
}

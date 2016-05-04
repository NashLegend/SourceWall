package net.nashlegend.sourcewall.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.model.AceModel;
import net.nashlegend.sourcewall.view.BasketsView;

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
        private BasketsView basketsView;
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

        public Builder setTitle(int title) {
            this.title = mContext.getResources().getString(title);
            return this;
        }

        public FavorDialog create(AceModel data) {
            dialog = new FavorDialog(mContext);
            basketsView = new BasketsView(mContext);
            okButton = basketsView.findViewById(R.id.button_favor_dialog_ok);
            basketsView.setData(data);
            dialog.setTitle(title);
            dialog.setCancelable(cancelable);
            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
            dialog.setContentView(basketsView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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

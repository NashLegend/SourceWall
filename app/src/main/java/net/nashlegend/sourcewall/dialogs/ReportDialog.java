package net.nashlegend.sourcewall.dialogs;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.util.UiUtil;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class ReportDialog extends Dialog {

    public ReportDialog(Context context) {
        super(context);
    }

    public ReportDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder {
        private Context mContext;
        private String title = "";
        private String okString = "";
        private String cancelString = "";
        private String input = "";
        private ReportDialog dialog;
        private ReportReasonListener reasonListener;
        private int resID = R.layout.layout_report;

        public Builder(Context context) {
            mContext = context;
            okString = mContext.getResources().getString(R.string.ok);
            cancelString = mContext.getResources().getString(R.string.cancel);
        }

        public Builder setReasonListener(ReportReasonListener reasonListener) {
            this.reasonListener = reasonListener;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTitle(int title) {
            this.title = mContext.getResources().getString(title);
            return this;
        }

        public ReportDialog create() {
            dialog = new ReportDialog(mContext);
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(resID, null);
            dialog.setContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            dialog.setTitle(title);

            final EditText inputText = (EditText) layout.findViewById(R.id.text_other_reason);
            final RadioButton adButton = (RadioButton) layout.findViewById(R.id.btn_ad);
            final RadioButton portButton = (RadioButton) layout.findViewById(R.id.btn_porn);
            final RadioButton attackButton = (RadioButton) layout.findViewById(R.id.btn_attack);
            final RadioButton copyRightButton = (RadioButton) layout.findViewById(R.id.btn_copy_right);
            final RadioButton otherButton = (RadioButton) layout.findViewById(R.id.btn_other);

            Button okayButton = (Button) layout.findViewById(R.id.button_dialog_input_ok);
            Button nayButton = (Button) layout.findViewById(R.id.button_dialog_input_cancel);
            inputText.setText(input);
            okayButton.setText(okString);
            nayButton.setText(cancelString);

            okayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (reasonListener != null) {
                        String reason = "";
                        if (adButton.isChecked()) {
                            reason = adButton.getText().toString();
                        } else if (portButton.isChecked()) {
                            reason = portButton.getText().toString();
                        } else if (attackButton.isChecked()) {
                            reason = attackButton.getText().toString();
                        } else if (copyRightButton.isChecked()) {
                            reason = copyRightButton.getText().toString();
                        } else if (otherButton.isChecked()) {
                            reason = inputText.getText().toString().trim();
                            if (TextUtils.isEmpty(reason)) {
                                ToastUtil.toastBigSingleton("理由不能为空");
                                return;
                            }
                        }
                        reasonListener.onGetReason(dialog, reason);
                    }
                }
            });

            nayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UiUtil.dismissDialog(dialog);
                }
            });
            return dialog;
        }

    }

    public interface ReportReasonListener {
        void onGetReason(Dialog dialog, String reason);
    }

}
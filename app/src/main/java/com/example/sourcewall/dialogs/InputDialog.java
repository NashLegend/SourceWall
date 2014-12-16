package com.example.sourcewall.dialogs;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import com.example.sourcewall.R;

/**
 * Created by NashLegend on 2014/12/2 0002
 */
public class InputDialog extends Dialog {

    public String InputString = "";
    public String InputString2 = "";

    public InputDialog(Context context) {
        super(context);
    }

    public static class Builder {
        private Context mContext;
        private String title = "";
        private String okString = "";
        private String cancelString = "";
        private String input = "";
        private InputDialog dialog;
        private boolean canceledOnTouchOutside = true;
        private boolean cancelable = true;
        private OnClickListener onClickListener;
        private int resID = R.layout.dialog_input;

        public Builder(Context context) {
            mContext = context;
            okString = mContext.getResources().getString(R.string.ok);
            cancelString = mContext.getResources().getString(R.string.cancel);
        }

        public Builder setOnClickListener(OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean flag) {
            canceledOnTouchOutside = flag;
            return this;
        }

        public Builder setCancelable(boolean flag) {
            cancelable = flag;
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

        public Builder setButtonText(String Okay, String Nay) {
            this.okString = Okay;
            this.cancelString = Nay;
            return this;
        }

        public Builder setInputText(String input) {
            this.input = input;
            return this;
        }

        public Builder setSingleLine() {
            resID = R.layout.dialog_input_simple;
            return this;
        }

        public Builder setTwoLine() {
            resID = R.layout.dialog_input_simple_two;
            return this;
        }

        public Builder setMultiLine() {
            resID = R.layout.dialog_input;
            return this;
        }

        public InputDialog create() {
            dialog = new InputDialog(mContext);
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(resID, null);
            dialog.setContentView(layout, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            dialog.setCancelable(cancelable);
            dialog.setTitle(title);
            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
            //不知为啥在Android5.0上，在xml里面和setContentView里面设置match_parent不管用，只好如下行这样强制设置喽
            //然而后来发现这是LinearLayout的问题，把根view改成RelativeLayout就成了，所以正面这行就不用了
            //dialog.getWindow().setLayout(-1, -2);
            final EditText inputText = (EditText) layout
                    .findViewById(R.id.input_dialog_text);
            final EditText inputText2 = (EditText) layout
                    .findViewById(R.id.input_dialog_text2);
            Button okayButton = (Button) layout
                    .findViewById(R.id.button_dialog_input_ok);
            Button nayButton = (Button) layout
                    .findViewById(R.id.button_dialog_input_cancel);
            inputText.setText(input);
            okayButton.setText(okString);
            nayButton.setText(cancelString);
            okayButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        dialog.InputString = inputText.getText().toString();
                        if (inputText2 != null) {
                            dialog.InputString2 = inputText2.getText().toString();
                        }
                        onClickListener.onClick(dialog, BUTTON_POSITIVE);
                        dialog.dismiss();
                    }
                }
            });

            nayButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        onClickListener.onClick(dialog, BUTTON_NEGATIVE);
                        dialog.dismiss();
                    }
                }
            });
            return dialog;
        }

    }

}
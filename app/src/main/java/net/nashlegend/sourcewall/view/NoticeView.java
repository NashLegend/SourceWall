package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.NoticeAdapter;
import net.nashlegend.sourcewall.connection.ResultObject;
import net.nashlegend.sourcewall.connection.api.UserAPI;
import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.util.Consts;
import net.nashlegend.sourcewall.util.SharedUtil;

/**
 * Created by NashLegend on 2015/2/12 0012
 */
public class NoticeView extends AceView<Notice> implements View.OnClickListener {

    private Notice data;
    private TextView noticeText;
    private ImageButton ignoreButton;
    private NoticeAdapter noticeAdapter;

    public NoticeView(Context context) {
        super(context);
        initViews();
    }

    public NoticeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public NoticeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    private void initViews() {
        if (SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false)) {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background_night));
        } else {
            setBackgroundColor(getContext().getResources().getColor(R.color.page_background));
        }

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_notice_view, this);
        noticeText = (TextView) findViewById(R.id.text_notice);
        ignoreButton = (ImageButton) findViewById(R.id.btn_ignore);
        ignoreButton.setOnClickListener(this);
    }

    public void setAdapter(NoticeAdapter adapter) {
        noticeAdapter = adapter;
    }

    @Override
    public void setData(Notice notice) {
        data = notice;
        if (data != null) {
            noticeText.setText(data.getContent());
        }
    }

    @Override
    public Notice getData() {
        return data;
    }

    private void ignoreNotice() {
        IgnoreTask ignoreTask = new IgnoreTask();
        ignoreTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ignore:
                ignoreNotice();
                break;
        }
    }

    class IgnoreTask extends AsyncTask<Notice, Integer, ResultObject> {

        Notice notice;

        @Override
        protected ResultObject doInBackground(Notice... params) {
            notice = params[0];
            return UserAPI.ignoreOneNotice(notice.getId());
        }

        @Override
        protected void onPostExecute(ResultObject resultObject) {
            if (resultObject.ok) {
                if (noticeAdapter != null && noticeAdapter.getList().remove(notice)) {
                    noticeAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}

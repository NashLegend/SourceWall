package net.nashlegend.sourcewall.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.NoticeAdapter;
import net.nashlegend.sourcewall.model.Notice;
import net.nashlegend.sourcewall.request.RequestObject.CallBack;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.MessageAPI;
import net.nashlegend.sourcewall.util.Mob;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2015/2/12 0012
 */
public class NoticeView extends AceView<Notice> implements View.OnClickListener {

    private Notice data;
    private TextView noticeText;
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
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_notice_view, this);
        noticeText = (TextView) findViewById(R.id.text_notice);
        findViewById(R.id.btn_ignore).setOnClickListener(this);
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

    private void ignoreNotice(final Notice notice) {
        MobclickAgent.onEvent(getContext(), Mob.Event_Ignore_One_Notice);
        MessageAPI.ignoreOneNotice(notice.getId(), new CallBack<ArrayList<Notice>>() {
            @Override
            public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<ArrayList<Notice>> result) {

            }

            @Override
            public void onSuccess(@NonNull ArrayList<Notice> result, @NonNull ResponseObject<ArrayList<Notice>> detailed) {
                if (noticeAdapter != null && noticeAdapter.getList().remove(notice)) {
                    noticeAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ignore:
                ignoreNotice(data);
                break;
        }
    }
}

package com.patrol.module.note;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.R;
import com.patrol.entity.NoteInfo;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteActivity extends SimplePagerListActivity {

    @Override
    public void init() {

        final ArrayList<NoteInfo> datas = new ArrayList<>();

        this.mSimplePagerListDelegate = new SimplePagerListDelegate<NoteInfo>(NoteActivity.this, datas, NoteInfo.class) {
            @Override
            protected SimpleBaseAdapter generateAdapter() {
                return new NoteAdapter(NoteActivity.this, datas);
            }

            @Override
            protected String generateUrl() {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/FetchPatrolNotes")
                        .append("?pageSize=").append(getPageSize())
                        .append("&pageNum=").append(getLoadPageIndex())
                        .append("&userID=").append(getUserIdStr());

                return sb.toString();
            }
        };
        mSimplePagerListDelegate.setPageSize(20);

    }

    final class NoteAdapter extends SimpleBaseAdapter {

        private final LayoutInflater inflater;
        private final List<NoteInfo> dataList;
        private final Matcher lineBreakMatcher;

        public NoteAdapter(Context context, List<NoteInfo> dataList) {
            this.dataList = dataList;
            this.inflater = LayoutInflater.from(context);

            this.lineBreakMatcher = Pattern.compile("(\n)+").matcher("");
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.patrol_note_item, parent, false);
            }

            final NoteInfo info = dataList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.tvSN)).setText(String.valueOf(position + 1) + ". ");
            ((TextView) MmtViewHolder.get(convertView, R.id.tvUserName)).setText(info.UserName);
            ((TextView) MmtViewHolder.get(convertView, R.id.tvDept)).setText(info.Dept);

            String detail = info.Detail;
            if (!TextUtils.isEmpty(detail)) {
                if (detail.contains("当日完成任务量\n\n")) {
                    detail = detail.replace("当日完成任务量\n\n", "");
                }
                if (detail.endsWith("\n")) {
                    detail = detail.substring(0, detail.length() - 1);
                }
            }
            if (TextUtils.isEmpty(detail)) {
                detail = "无任务信息";
            }
            ((TextView) MmtViewHolder.get(convertView, R.id.tvDetail)).setText(detail);

            String remark;
            if (TextUtils.isEmpty(info.Remark)) {
                remark = "无备注信息";
            } else {
                remark = lineBreakMatcher.reset(info.Remark).replaceAll(" ");
            }
            ((TextView) MmtViewHolder.get(convertView, R.id.tvRemark)).setText(remark);

            ((TextView) MmtViewHolder.get(convertView, R.id.tvNoteTime)).setText(info.NoteTime);

            return convertView;
        }
    }
}

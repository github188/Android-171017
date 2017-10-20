package com.mapgis.mmt.module.flowreport.history;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.FileZipUtil;
import com.mapgis.mmt.common.widget.PictureViewActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class EventViewAdapter extends BaseAdapter {
    private final EventViewFragment eventViewFragment;
    private final LayoutInflater inflater;

    public EventViewAdapter(EventViewFragment eventViewFragment) {
        this.eventViewFragment = eventViewFragment;
        this.inflater = LayoutInflater.from(this.eventViewFragment.getActivity());
    }

    @Override
    public int getCount() {
        return this.eventViewFragment.parametersList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return arg0;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup arg2) {
        ViewHolder holder = null;
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.flow_reporter_event_view_item, null);
            holder = new ViewHolder();

            holder.position = position;
            holder.flowNameText = (TextView) contentView.findViewById(R.id.flowNameText);
            holder.caseNameText = (TextView) contentView.findViewById(R.id.caseNameText);
            holder.roadNameText = (TextView) contentView.findViewById(R.id.roadNameText);
            holder.pictureView = (ImageView) contentView.findViewById(R.id.pictureView);
            holder.contentText = (TextView) contentView.findViewById(R.id.contentText);
            holder.timeText = (TextView) contentView.findViewById(R.id.timeText);
            holder.stateText = (TextView) contentView.findViewById(R.id.stateText);
            contentView.setTag(holder);
        } else {
            holder = (ViewHolder) contentView.getTag();
        }

        Bitmap bitmap = BitmapFactory.decodeResource(eventViewFragment.getResources(), R.drawable.no_picture);

        if (this.eventViewFragment.parametersList.get(position).getMediaString() != null) {

            String mediaPathStr = this.eventViewFragment.parametersList.get(position).getMediaString();

            if (!BaseClassUtil.isNullOrEmptyString(mediaPathStr)) {
                List<String> mediaPaths = BaseClassUtil.StringToList(mediaPathStr, ",");

                if (mediaPaths.size() != 0) {
                    String firstPicPath = mediaPaths.get(0);

                    bitmap = FileZipUtil.getBitmapFromFile(new File(firstPicPath), 200, 120);

                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeResource(eventViewFragment.getResources(), R.drawable.no_picture);
                    }
                }
            }

        } else {
            bitmap = BitmapFactory.decodeResource(eventViewFragment.getResources(), R.drawable.no_picture);
        }

        holder.pictureView.setImageBitmap(bitmap);

        ImageView imageView = holder.pictureView;
        final int p = position;
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = EventViewAdapter.this.eventViewFragment.parametersList.get(p).getMediaString();

                if (path == null) {
                    Toast.makeText(EventViewAdapter.this.eventViewFragment.getActivity(), "无图片", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<String> fileList = EventViewAdapter.this.eventViewFragment.getMediaPath(path);

                if (fileList != null && fileList.size() > 0) {
                    Intent intent = new Intent(EventViewAdapter.this.eventViewFragment.getActivity(), PictureViewActivity.class);
                    intent.putStringArrayListExtra("fileList", fileList);
                    intent.putExtra("canDelete", false);
                    EventViewAdapter.this.eventViewFragment.startActivity(intent);
                } else {
                    Toast.makeText(EventViewAdapter.this.eventViewFragment.getActivity(), "无图片", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.flowNameText.setText("类型：巡检");
        holder.caseNameText.setText("事件：" + this.eventViewFragment.parametersList.get(position).getCaseDesc());
        holder.roadNameText.setText("地址：" + this.eventViewFragment.parametersList.get(position).getRoadName());
        holder.contentText.setText("坐标：" + this.eventViewFragment.parametersList.get(position).getPosition());
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
        Date date = new Date(Long.valueOf(this.eventViewFragment.parametersList.get(position).getTime()));
        holder.timeText.setText("时间：" + String.valueOf(format.format(date)));
        holder.stateText.setText((this.eventViewFragment.parametersList.get(position).getState() == 1 ? "（已上报）" : "（未上报）"));
        return contentView;
    }

    class ViewHolder {
        public int position;
        public TextView flowNameText;
        public TextView caseNameText;
        public TextView roadNameText;
        public TextView contentText;
        public TextView timeText;
        public ImageView pictureView;
        public TextView stateText;
    }
}
package com.patrol.module.KeyPoint;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.patrol.entity.KeyPoint;
import com.patrol.module.MyPlanMapMenu;
import com.patrol.module.PatrolUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 自定义适配器
 */
class KeyPointAdapter extends BaseAdapter implements View.OnClickListener, Filterable {
    private List<KeyPoint> points;
    private final LayoutInflater inflater;
    private final Context context;

    private final Object mLock = new Object();
    private ArrayList<KeyPoint> mOriginalValues;
    private PointFilter mFilter;

    public KeyPointAdapter(Context context, ArrayList<KeyPoint> points) {
        this.context = context;
        this.points = points;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return points.size();
    }

    @Override
    public Object getItem(int position) {
        return points.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.my_plan_task_point, parent, false);

        KeyPoint kp = points.get(position);

        String sn = (position + 1) + ".";

        ((TextView) MmtViewHolder.get(convertView, R.id.tvSN)).setText(sn);
        ((TextView) MmtViewHolder.get(convertView, R.id.tvFieldValue)).setText(kp.FieldValue);
        ((TextView) MmtViewHolder.get(convertView, R.id.tvLayerName)).setText(kp.GisLayer);
        ((TextView) MmtViewHolder.get(convertView, R.id.tvIsArrive)).setText(kp.IsArrive == 1 ? "已到位" : "未到位");
        ((TextView) MmtViewHolder.get(convertView, R.id.tvArriveTime)).setText(TextUtils.isEmpty(kp.ArriveTime) ? "-" : kp.ArriveTime);

        String fbState = kp.IsFeedback == 1 ? "已反馈" : "未反馈";

        if (kp.IsFeedback != 1 && !TextUtils.isEmpty(kp.KClass) && kp.KClass.equals("0"))
            fbState = "(该巡线点仅要求到位，不需要反馈)";

        ((TextView) MmtViewHolder.get(convertView, R.id.tvIsFeedback)).setText(fbState);
        ((TextView) MmtViewHolder.get(convertView, R.id.tvFeedbackTime)).setText(TextUtils.isEmpty(kp.FeedbackTime) ? "-" : kp.FeedbackTime);


        TextView tvRemark = MmtViewHolder.get(convertView, R.id.tvRemark);

        if (TextUtils.isEmpty(kp.Remark))
            tvRemark.setVisibility(View.GONE);
        else {
            tvRemark.setVisibility(View.VISIBLE);

            tvRemark.setText(kp.Remark);
        }

        TextView tvLocate = MmtViewHolder.get(convertView, R.id.tvLocate);

        int icon = PatrolUtils.getIco(kp);

        tvLocate.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0, 0);

        int[] views = new int[]{R.id.btnDetail, R.id.btnFeedback, R.id.tvLocate};

        for (int v : views) {
            View view = MmtViewHolder.get(convertView, v);

            view.setTag(kp);
            view.setOnClickListener(this);
        }

        return convertView;
    }

    @Override
    public void onClick(View v) {
        try {
            int vID = v.getId();
            KeyPoint kp = (KeyPoint) v.getTag();
            MyPlanMapMenu menu = (MyPlanMapMenu) ((MapGISFrame) context).getFragment().menu;

            if (vID == R.id.btnDetail) {
                menu.onShowGISDetail(kp);
            } else if (vID == R.id.btnFeedback) {
                menu.onFeedback(kp);
            } else if (vID == R.id.tvLocate) {
                menu.onLocate(kp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public PointFilter getFilter() {
        if (mFilter == null) {
            mFilter = new PointFilter();
        }
        return mFilter;
    }

    class PointFilter extends Filter {

        private GpsXYZ preXY;
        public PointFilter setCurrentPosition(GpsXYZ gpsXYZ) {
            this.preXY = gpsXYZ;
            return this;
        }

        @Override
        protected FilterResults performFiltering(@NonNull CharSequence constraint) {

            String[] constraintList = constraint.toString().split(";");

            FilterResults results = new FilterResults();
            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<>(points);
                }
            }

            if (constraintList.length != 3) {
                ArrayList<KeyPoint> list;
                synchronized (mLock) {
                    list = new ArrayList<>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {

                final String distance = constraintList[0].trim();
                final String status = constraintList[1].trim();
                final String order = constraintList[2].trim();

                ArrayList<KeyPoint> values;
                synchronized (mLock) {
                    values = new ArrayList<>(mOriginalValues);
                }
                final ArrayList<KeyPoint> newValues = new ArrayList<>();

                double radius = 0;
                switch (distance) {
                    case "500米":
                        radius = 500;
                        break;
                    case "1000米":
                        radius = 1000;
                        break;
                    case "2000米":
                        radius = 2000;
                        break;
                    case "5000米":
                        radius = 5000;
                        break;
                }

                for (int i = 0, count = values.size(); i < count; i++) {
                    KeyPoint kp = values.get(i);
                    if (kp.Type == 2) {
                        continue;
                    }

                    boolean isOk = true;
                    switch (status) {
                        case "已到位":
                            isOk = kp.IsArrive == 1 && kp.IsFeedback == 0;
                            break;
                        case "未到位":
                            isOk = kp.IsArrive == 0;
                            break;
                        case "已反馈":
                            isOk = kp.IsFeedback == 1;
                            break;
                    }

                    if (isOk && (radius <= 0 || kp.getDistance(preXY) <= radius)) {
                        newValues.add(kp);
                    }
                }

                Collections.sort(newValues, new Comparator<KeyPoint>() {
                    long leftTime, rightTime;
                    @Override
                    public int compare(KeyPoint lhs, KeyPoint rhs) {
                        try {
                            switch (order) {
                                case "距离优先":
                                    if (lhs.Distance > rhs.Distance) {
                                        return 1;
                                    } else if (lhs.Distance < rhs.Distance) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                case "到位时间":
                                    leftTime = lhs.getArriveTime();
                                    rightTime = rhs.getArriveTime();
                                    if (leftTime > rightTime) {
                                        return -1;
                                    } else if (leftTime < rightTime) {
                                        return 1;
                                    } else {
                                        return 0;
                                    }
                                case "反馈时间":
                                    leftTime = lhs.getFeedbackTime();
                                    rightTime = rhs.getFeedbackTime();
                                    if (leftTime > rightTime) {
                                        return -1;
                                    } else if (leftTime < rightTime) {
                                        return 1;
                                    } else {
                                        return 0;
                                    }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            points = (List<KeyPoint>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

        private final Comparator<KeyPoint> distanceComparator  = new Comparator<KeyPoint>() {
            @Override
            public int compare(KeyPoint lhs, KeyPoint rhs) {
                if (lhs.Distance > rhs.Distance) {
                    return 1;
                } else if (lhs.Distance < rhs.Distance) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
    }
}
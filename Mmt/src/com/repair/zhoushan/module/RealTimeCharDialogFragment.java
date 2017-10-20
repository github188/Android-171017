package com.repair.zhoushan.module;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.mapgis.mmt.R;

public class RealTimeCharDialogFragment extends DialogFragment implements OnChartValueSelectedListener {

    public static final String TAG = "RealTimeCharDialogFragment";

    private LineChart mChart;

    private TextView tvMaxValue;
    private TextView tvMinValue;
    private float maxValue;
    private float minValue;

    public static RealTimeCharDialogFragment newInstance() {
        RealTimeCharDialogFragment fragment = new RealTimeCharDialogFragment();
        fragment.setCancelable(false);
        return fragment;
    }

//    private View contentView;
//    @NonNull
//    @Override ,
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//
////        return super.onCreateDialog(savedInstanceState);
//
//        this.contentView = getActivity().getLayoutInflater().inflate(R.layout.fragment_realtime_chart, null);
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setView(contentView);
//        return builder.create();
//    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nested fragment
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof OnConfirmListener) {
            mOnConfirmListener = (OnConfirmListener) parentFragment;
        }

        // Host activity
        if (mOnConfirmListener == null) {
            Activity hostActivity = getActivity();
            if (hostActivity instanceof OnConfirmListener) {
                mOnConfirmListener = (OnConfirmListener) hostActivity;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realtime_chart, container, false);
        this.mChart = (LineChart) view.findViewById(R.id.line_chart);
        initChart();
        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void initView(View view) {
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        view.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnConfirmListener != null) {
                    mOnConfirmListener.onConfirmClicked(maxValue, minValue);
                }
                dismiss();
            }
        });

        this.tvMaxValue = (TextView) view.findViewById(R.id.tv_max_value);
        this.tvMinValue = (TextView) view.findViewById(R.id.tv_min_value);

    }

    private void initChart() {

        if (mChart == null) {
            return;
        }

        mChart.setOnChartValueSelectedListener(this);

        // enable description text
        mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.GRAY);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.GRAY);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.GRAY);
//        leftAxis.setAxisMaximum(100f);
//        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private boolean isFirstEntry = true;

    public void addEntry(float entryYValue) {

        if (mChart == null) {
            return;
        }

        LineData data = mChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            Entry entry = new Entry(set.getEntryCount(), entryYValue);
            if (isFirstEntry) {
                minValue = maxValue = entry.getY();
                tvMaxValue.setText(String.valueOf(maxValue));
                tvMinValue.setText(String.valueOf(minValue));
                isFirstEntry = false;
            }
            if (entry.getY() > maxValue) {
                maxValue = entry.getY();
                tvMaxValue.setText(String.valueOf(maxValue));
            } else if (entry.getY() < minValue) {
                minValue = entry.getY();
                tvMinValue.setText(String.valueOf(minValue));
            }
            data.addEntry(entry, 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(20);

            mChart.setKeepScreenOn(true);
            mChart.setNoDataText("暂无数据");
            mChart.setScaleXEnabled(true);
            mChart.setScaleYEnabled(false);
            mChart.getDescription().setEnabled(false);
            mChart.getLegend().setEnabled(false);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "浓度");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(1.5f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLUE);
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return set;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    private OnConfirmListener mOnConfirmListener;

    public void setOnConfirmListener(OnConfirmListener mOnConfirmListener) {
        this.mOnConfirmListener = mOnConfirmListener;
    }

    public interface OnConfirmListener {
        void onConfirmClicked(float maxValue, float minValue);
    }
}


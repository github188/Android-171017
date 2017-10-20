package com.mapgis.mmt.common.widget.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

public class ProgressDialogFragment extends DialogFragment {
    private TextView progressDialogTitle;
    private TextView progressDialogTip;
    private TextView progressDialogProgressShow;
    private ProgressBar progressDialogHorizontal;
    private ProgressBar progressDialogCircle;
    private Button okButton;

    private long progress;
    private long max;

    private OnButtonClickListener onButtonClickListener;

    /**
     * 创建的时候，true显示横向进度条，false显示圆形进度条
     */
    private boolean createShowHorizontal = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.progress_dialog_fragment, container, false);
        v.setMinimumWidth(DimenTool.dip2px(getActivity(), 360));

        progressDialogTitle = (TextView) v.findViewById(R.id.progressDialogTitle);
        progressDialogTip = (TextView) v.findViewById(R.id.progressDialogTip);
        progressDialogProgressShow = (TextView) v.findViewById(R.id.progressDialogProgressShow);
        progressDialogHorizontal = (ProgressBar) v.findViewById(R.id.progressDialogHorizontal);
        progressDialogCircle = (ProgressBar) v.findViewById(R.id.progressDialogCircle);

        okButton = (Button) v.findViewById(R.id.btn_ok);
        okButton.setVisibility(View.GONE);

        v.findViewById(R.id.btn_cancel).setVisibility(View.GONE);

        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialogTitle.setText(getArguments().getString("title"));

        progressDialogTip.setText(getArguments().getString("tip"));

        progressDialogHorizontal.setMax(100);
        progressDialogHorizontal.setProgress(0);

        progressDialogCircle.setVisibility(View.GONE);

        if (!createShowHorizontal) {
            progressDialogCircle.setVisibility(View.VISIBLE);
            progressDialogHorizontal.setVisibility(View.GONE);
            progressDialogTip.setVisibility(View.GONE);
        }

        okButton.setText("确认");

        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onButtonClick(v);
                }

                dismiss();
            }
        });
    }

    /**
     * 创建的时候，true显示横向进度条，false显示圆形进度条
     */
    public void createShowHorizontal(boolean isShowHorizontal) {
        createShowHorizontal = isShowHorizontal;
    }

    /**
     * 横向进度框的最大值
     */
    public void setMax(long max) {
        progressDialogHorizontal.setMax((int) max);
        this.max = max;
    }

    /**
     * 横向进度框的当前值
     */
    public void setProgress(long progress) {
        progressDialogHorizontal.setProgress((int) progress);
        this.progress = progress;
        progressText();
    }

    /**
     * 确认按钮的的文本信息
     */
    public void setButtonText(String text) {
        okButton.setText(text);
    }

    public void setButtonVisibility(int visibility) {
        okButton.setVisibility(visibility);
    }

    private void progressText() {
        String currentMB = (int) (progress / (1024 * 1024)) + "MB";
        String maxMB = (int) (max / (1024 * 1024)) + "MB";

        String text = currentMB + "/" + maxMB;

        progressDialogProgressShow.setText(text);
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public interface OnButtonClickListener {
        void onButtonClick(View view);
    }

}

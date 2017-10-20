package com.mapgis.mmt.common.widget.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;

public class OkCancelDialogFragment extends DialogFragment {

    private final String title;

    private View myView;

    private String leftButtonText = "取消", rightButtonText = "确认";

    private OnLeftButtonClickListener onLeftButtonClickListener;
    private OnRightButtonClickListener onRightButtonClickListener;

    private boolean hideLeftButton = false;
    private boolean autoDismiss = true;

    public OkCancelDialogFragment(String title, View myView) {
        this.title = title;
        this.myView = myView;
    }

    public OkCancelDialogFragment(String title) {
        this.title = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ok_cancel_dialog, container, false);

        if (title != null && title.length() > 0) {
            ((TextView) v.findViewById(R.id.tv_ok_cancel_dialog_Tips)).setText(title);
        }

        if (myView != null) {
            ((LinearLayout) v.findViewById(R.id.layout_ok_cancel_dialog_content)).addView(myView);
        } else {
            v.findViewById(R.id.layout_ok_cancel_dialog_content).setVisibility(View.GONE);
        }

        if (hideLeftButton) {
            v.findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        } else {
            Button leftButton = (Button) v.findViewById(R.id.btn_cancel);
            leftButton.setText(leftButtonText);
            leftButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onLeftButtonClickListener != null) {
                        onLeftButtonClickListener.onLeftButtonClick(v);
                    }
                    dismiss();
                }
            });
        }

        Button rightButton = (Button) v.findViewById(R.id.btn_ok);
        rightButton.setText(rightButtonText);
        rightButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (onRightButtonClickListener != null) {
                    onRightButtonClickListener.onRightButtonClick(v);
                }
                if (autoDismiss) {
                    dismiss();
                }
            }
        });

        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return v;
    }

    public void setHideLeftButton() {
        hideLeftButton = true;
    }

    public void setAutoDismiss(boolean autoDismiss) {
        this.autoDismiss = autoDismiss;
    }

    public void setLeftBottonText(String text) {
        leftButtonText = text;
    }

    public void setRightBottonText(String text) {
        rightButtonText = text;
    }

    public void setOnLeftButtonClickListener(OnLeftButtonClickListener onLeftButtonClickListener) {
        this.onLeftButtonClickListener = onLeftButtonClickListener;
    }

    public void setOnRightButtonClickListener(OnRightButtonClickListener onRightButtonClickListener) {
        this.onRightButtonClickListener = onRightButtonClickListener;
    }

    public interface OnLeftButtonClickListener {
        void onLeftButtonClick(View view);
    }

    public interface OnRightButtonClickListener {
        void onRightButtonClick(View view);
    }
}

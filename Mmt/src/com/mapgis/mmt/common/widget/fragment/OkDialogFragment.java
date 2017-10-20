package com.mapgis.mmt.common.widget.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

public class OkDialogFragment extends DialogFragment {

    private final String title;

    private View myView;

    private OnButtonClickListener onButtonClickListener;

    public OkDialogFragment(String title, View myView) {
        this.title = title;
        this.myView = myView;
    }

    public OkDialogFragment(String title) {
        this.title = title;
    }

    private int gravity = Gravity.CENTER_HORIZONTAL;

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ok_cancel_dialog, container, false);

        v.setMinimumWidth(DimenTool.dip2px(getActivity(), 240));

        TextView tvTip = (TextView) v.findViewById(R.id.tv_ok_cancel_dialog_Tips);

        tvTip.setGravity(this.gravity);
        tvTip.setText(this.title);

        if (myView != null) {
            ((LinearLayout) v.findViewById(R.id.layout_ok_cancel_dialog_content)).addView(myView);
        } else {
            v.findViewById(R.id.layout_ok_cancel_dialog_content).setVisibility(View.GONE);
        }

        v.findViewById(R.id.btn_cancel).setVisibility(View.GONE);

        String buttonText = "确认";

        Button btnOK = (Button) v.findViewById(R.id.btn_ok);

        btnOK.setText(buttonText);

        btnOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (onButtonClickListener != null) {
                    onButtonClickListener.onButtonClick(v);
                }
                dismiss();
            }
        });

        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return v;
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public interface OnButtonClickListener {
        void onButtonClick(View view);
    }

}

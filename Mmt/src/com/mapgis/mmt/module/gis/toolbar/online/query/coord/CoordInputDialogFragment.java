package com.mapgis.mmt.module.gis.toolbar.online.query.coord;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisUtil;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

public class CoordInputDialogFragment extends DialogFragment {

    public static final String TAG = "CoordInputDialogFragment";

    private EditText etXCoord;
    private EditText etYCoord;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.view_coord_query, null);
        this.etXCoord = (EditText) view.findViewById(R.id.et_coord_x);
        this.etYCoord = (EditText) view.findViewById(R.id.et_coord_y);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MmtBaseThemeAlertDialog));
        builder.setTitle("请输入坐标")
                .setView(view)
                .setPositiveButton("定位", null)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeSoftKeyboard();
                    }
                })
                .setCancelable(false);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        Button posiBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        posiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleQueryClick();
            }
        });
    }

    private void handleQueryClick() {

        if (onQueryListener == null) {
            return;
        }

        String xStr = etXCoord.getText().toString().trim();
        String yStr = etYCoord.getText().toString().trim();

        if (TextUtils.isEmpty(xStr) || TextUtils.isEmpty(yStr)) {
            Toast.makeText(getContext(), "请输入完整的坐标值", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double x = Double.parseDouble(xStr);
            double y = Double.parseDouble(yStr);

            Rect rect = GisUtil.getEntireRange();
            if (rect != null) {
                if (!GisUtil.isInRect(rect, new Dot(x, y))) {
                    Toast.makeText(getContext(), "坐标值不在地图范围内，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            onQueryListener.query(x, y);
            closeSoftKeyboard();
            dismiss();

        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "输入的坐标值有误，请重新输入", Toast.LENGTH_SHORT).show();
        }

    }

    private void closeSoftKeyboard() {
        View view = getDialog().getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager systemService = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            systemService.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private OnQueryListener onQueryListener;

    public void setOnQueryListener(OnQueryListener onQueryListener) {
        this.onQueryListener = onQueryListener;
    }

    public interface OnQueryListener {
        void query(double xCoord, double yCoord);
    }
}

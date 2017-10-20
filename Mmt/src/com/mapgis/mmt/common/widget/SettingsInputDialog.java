package com.mapgis.mmt.common.widget;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;

import java.util.Arrays;

public class SettingsInputDialog extends DialogFragment {
    private OnOkClickListener onOkClickListener;

    private final String value;
    private final String title;

    public SettingsInputDialog(String title, String value) {
        this.title = title;
        this.value = value;
    }

    public void setOnOkClickListener(OnOkClickListener onOkClickListener) {
        this.onOkClickListener = onOkClickListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        final View view = inflater.inflate(R.layout.system_settings_dialog, null);

        ((TextView) view.findViewById(R.id.dialogTitle)).setText(title);

        ((EditText) view.findViewById(R.id.dialog_editText)).setText(MyApplication.getInstance().getConfigValue(value));

        view.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if (value.equals("empty")) {
            ((TextView) view.findViewById(R.id.dialog_textView)).setText("");
        }

        if (value.equals("mySQL")) {
            ((EditText) view.findViewById(R.id.dialog_editText)).setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        }

        if (value.equals("areaSize")) {
            ((TextView) view.findViewById(R.id.dialog_textView)).setText("(单位：米)");
        }


        view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String valueString = ((EditText) view.findViewById(R.id.dialog_editText)).getText().toString();

                if (value.equals("realtimeLocateInterval") && valueString.endsWith("2542312")) {
                    valueString = valueString.substring(0, valueString.length() - 7);

                    Toast.makeText(getActivity(), "请在登录时设置定位方式为<内测定位>以开启随机坐标模式", Toast.LENGTH_LONG).show();
                }

                MyApplication.getInstance().putConfigValue(value, valueString);

                if (onOkClickListener != null) {
                    onOkClickListener.onOkClick(view, valueString);
                }

                dismiss();
            }
        });
        return view;
    }

    public interface OnOkClickListener {
        void onOkClick(View view, String text);
    }

}

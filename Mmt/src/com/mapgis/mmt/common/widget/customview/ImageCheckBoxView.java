package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;

import java.util.ArrayList;
import java.util.List;

public class ImageCheckBoxView extends LinearLayout implements FeedBackView {

    private ImageView imageView;
    private TextView keyTextView;

    private TextView mustView;// 必填项

    List<CheckBox> checkBoxList = new ArrayList<CheckBox>();

    private String optValues = "";
    private List<String> optList = new ArrayList<String>();
    // private List<Integer> radioBtnIdList = new ArrayList<Integer>();

    public ImageCheckBoxView(String optValues, Context context) {
        this(context, null, optValues);
    }

    public ImageCheckBoxView(Context context, AttributeSet attrs, String optValues) {
        super(context, attrs);
        this.optValues = optValues;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.image_radio_button_view, this, true);

        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLayout);

        imageView = (ImageView) findViewById(R.id.imageButtonViewImage);
        imageView.setMinimumWidth(DimenTool.dip2px(context, 20));

        keyTextView = (TextView) findViewById(R.id.imageButtonViewKey);
        keyTextView.setTextAppearance(context, R.style.default_form_key);
        keyTextView.setText("键");
        keyTextView.setMinWidth(DimenTool.dip2px(context, 40));

        mustView = (TextView) findViewById(R.id.imageButtonViewMust);
        mustView.setVisibility(View.GONE);

        if (!BaseClassUtil.isNullOrEmptyString(optValues)) {
            optList = BaseClassUtil.StringToList(optValues, ",");

            if (optList != null && optList.size() > 1) {

                for (String item : optList) {

                    CheckBox checkBox = new CheckBox(context);
                    checkBox.setId(checkBox.hashCode());
                    checkBox.setText(item);
                    checkBox.setTextAppearance(context, R.style.default_text_medium_1);
                    checkBox.setButtonDrawable(R.drawable.checkbox_state);
                    checkBox.setPadding(DimenTool.dip2px(context, 10), 0, 0, 0);
                    checkBox.setChecked(false);

                    checkBoxList.add(checkBox);
                    mainLayout.addView(checkBox);
                }
            }
        }
    }

    @Override
    public void setImage(int id) {
        imageView.setImageDrawable(getResources().getDrawable(id));
    }

    @Override
    public void setKey(String str) {
        keyTextView.setText(str);
    }

    @Override
    public String getKey() {
        return keyTextView.getText().toString();
    }

    @Override
    public String getValue() {

        String resultStr = "";
        if (checkBoxList.size() > 0) {
            for (CheckBox checkBox : checkBoxList) {
                if (checkBox.isChecked()) {
                    resultStr += (checkBox.getText() + ",");
                }
            }
        }
        if (!BaseClassUtil.isNullOrEmptyString(resultStr)) {
            resultStr = resultStr.substring(0, resultStr.length() - 1);
        }

        return resultStr;
    }

    @Override
    public void setValue(String value) {

        if (!BaseClassUtil.isNullOrEmptyString(value)) {
            List<String> valueList = BaseClassUtil.StringToList(value, ",");

            for (String val : valueList) {
                if (optList.contains(val)) {
                    checkBoxList.get(optList.indexOf(value)).setChecked(true);
                }
            }
        }
    }

    public void isMustDo(boolean isMust) {
        mustView.setVisibility(isMust ? View.VISIBLE : View.GONE);
    }
}

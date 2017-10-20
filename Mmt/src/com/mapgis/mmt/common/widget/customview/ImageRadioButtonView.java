
package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;

import java.util.ArrayList;
import java.util.List;

public class ImageRadioButtonView extends LinearLayout implements FeedBackView {

    private ImageView imageView;
    private TextView keyTextView;

    private TextView mustView;// 必填项

    private String optValues = "";
    private List<String> optList = new ArrayList<String>();
    private List<Integer> radioBtnIdList = new ArrayList<Integer>();

    private RadioGroup radioGroup = null;

    public ImageRadioButtonView(String optValues, Context context) {
        this(context, null, optValues);
    }

    public RadioGroup getRadioGroup() {
        return radioGroup;
    }

    public ImageRadioButtonView(Context context, AttributeSet attrs, String optValues) {
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

//        keyTextView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                if (keyTextView.getLineCount() > 1) {
//                    keyTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//                }
//                return true;
//            }
//        });

        mustView = (TextView) findViewById(R.id.imageButtonViewMust);
        mustView.setVisibility(View.GONE);

        if (!BaseClassUtil.isNullOrEmptyString(optValues)) {
            optList = BaseClassUtil.StringToList(optValues, ",");

            if (optList != null && optList.size() > 1) {

                LinearLayout radioContainer = new LinearLayout(context);
                radioContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
                radioContainer.setOrientation(VERTICAL);
                radioContainer.setPadding(0, 0, DimenTool.dip2px(context, 8), 0);

                radioGroup = new RadioGroup(context);
                radioGroup.setOrientation(RadioGroup.HORIZONTAL);
                RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                layoutParams.gravity =  Gravity.RIGHT;
                radioGroup.setLayoutParams(layoutParams);
                radioGroup.setGravity(Gravity.CENTER_VERTICAL);

                radioContainer.addView(radioGroup);

                for (String item : optList) {
                    RadioButton radioButton = new RadioButton(context);
                    radioButton.setText(item);
                    radioButton.setTextAppearance(context, R.style.default_text_medium_1);
                    radioButton.setChecked(false);

                    radioButton.setId(radioButton.hashCode());
                    radioBtnIdList.add(radioButton.getId());

                    radioGroup.addView(radioButton, new RadioGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
                }

                mainLayout.addView(radioContainer);

                radioGroup.check(radioBtnIdList.get(0));

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (onCheckedChangedListener != null) {

                            String checkedText = ((RadioButton) findViewById(checkedId)).getText().toString();
                            onCheckedChangedListener.onCheckedChanged(checkedText);
                        }
                    }
                });

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
        if (radioGroup != null) {
            return ((RadioButton) findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString();
        }
        return "";
    }

    @Override
    public void setValue(String value) {
        if (optList.contains(value)) {
            ((RadioButton) findViewById(radioBtnIdList.get(optList.indexOf(value)))).setChecked(true);
        }
    }

    public void isMustDo(boolean isMust) {
        mustView.setVisibility(isMust ? View.VISIBLE : View.GONE);
    }

    private OnCheckedChangedListener onCheckedChangedListener;

    public void setOnCheckedChangedListener(OnCheckedChangedListener listener) {
        this.onCheckedChangedListener = listener;
    }

    public interface OnCheckedChangedListener {
        void onCheckedChanged(String checkedValue);
    }

}

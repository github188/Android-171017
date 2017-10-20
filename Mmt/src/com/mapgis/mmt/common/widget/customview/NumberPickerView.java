package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mapgis.mmt.R;

public class NumberPickerView extends LinearLayout {

    private Button btnPlus;
    private Button btnMinus;
    private EditText valueEditText;

    public static final int MIN_NUMBER = 0;
    public static final int MAX_NUMBER = 9999;

    private int minNumber = MIN_NUMBER;
    private int maxNumber = MAX_NUMBER;

    public NumberPickerView(Context context) {
        this(context, null);
    }

    public NumberPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView(attrs, defStyle);
        initListener();
    }

    private void initView(AttributeSet attrs, int defStyle) {

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.NumberPickerView, defStyle, 0);

        if (a.hasValue(R.styleable.NumberPickerView_zh_minQuantity)) {
            this.minNumber = a.getInt(R.styleable.NumberPickerView_zh_minQuantity, 0);
        }

        if (a.hasValue(R.styleable.NumberPickerView_zh_maxQuantity)) {
            this.maxNumber = a.getInt(R.styleable.NumberPickerView_zh_maxQuantity, 9999);
        }

        a.recycle();

        View view = LayoutInflater.from(getContext()).inflate(R.layout.number_picker_view, this, true);
        btnPlus = (Button) view.findViewById(R.id.btn_plus);
        btnMinus = (Button) view.findViewById(R.id.btn_minus);
        valueEditText = (EditText) view.findViewById(R.id.edittext_number);
        valueEditText.setText(String.valueOf(minNumber));
    }

    private void initListener() {

        valueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    String value = editable.toString();
                    if ("".equals(value) || Integer.valueOf(value) < minNumber) {
                        valueEditText.setText(String.valueOf(minNumber));
                    } else if (Integer.valueOf(value) > maxNumber) {
                        valueEditText.setText(String.valueOf(maxNumber));
                    }
                } catch (Exception e) {
                    valueEditText.setText(String.valueOf(minNumber));
                    e.printStackTrace();
                }

                if (onNumberChangedListener != null) {
                    onNumberChangedListener.onNumberChanged(Integer.parseInt(valueEditText.getText().toString()));
                }
            }
        });

        btnPlus.setOnClickListener(new OnClickListener() {

            int intValue;

            @Override
            public void onClick(View v) {
                try {
                    intValue = Integer.parseInt(valueEditText.getText().toString());
                    if (intValue < maxNumber) {
                        valueEditText.setText(String.valueOf(intValue + 1));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });

        btnMinus.setOnClickListener(new OnClickListener() {

            int intValue;

            @Override
            public void onClick(View v) {
                try {
                    intValue = Integer.parseInt(valueEditText.getText().toString());
                    if (intValue > minNumber) {
                        valueEditText.setText(String.valueOf(intValue - 1));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public int getValue() {
        return Integer.parseInt(valueEditText.getText().toString());
    }

    public boolean setValue(int value) {
        if (minNumber <= value && value <= maxNumber) {
            valueEditText.setText(String.valueOf(value));
            return true;
        }
        return false;
    }

    public void setMaxNumber(int value) {
        this.maxNumber = value;
    }

    public void setMinNumber(int value) {
        this.minNumber = value;
    }

    public interface OnNumberChangedListener {
        void onNumberChanged(int value);
    }

    private OnNumberChangedListener onNumberChangedListener;

    public void setOnNumberChangedListener(OnNumberChangedListener onNumberChangedListener) {
        this.onNumberChangedListener = onNumberChangedListener;
    }

}

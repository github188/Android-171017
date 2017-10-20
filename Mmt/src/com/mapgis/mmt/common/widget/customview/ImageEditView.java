package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;

public class ImageEditView extends RelativeLayout implements FeedBackView {

    private ImageView imageView;
    private TextView keyTextView;
    private EditText editText;
    private TextView mustView; // 必填项
    private TextView modifierView; // 修饰符、单位
    private View ivDelete;

    public EditText getEditText() {
        return editText;
    }

    /**
     * 文本的最大长度（中文占用两个长度）
     */
    private int maxLength = 0;

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    static class SavedState extends BaseSavedState {

        int maxLength;

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            this.maxLength = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(maxLength);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);
        state.maxLength = maxLength;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        maxLength = ss.maxLength;
    }

    public ImageEditView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView(context);
    }

    public ImageEditView(Context context) {
        this(context, null);
    }

    public View getIvDelete() {
        return ivDelete;
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.image_edit_view, this, true);

        if (isInEditMode())
            return;

        imageView = (ImageView) findViewById(R.id.imageEditViewImage);
        imageView.setImageResource(R.drawable.flex_flow_address);
        imageView.setMinimumWidth(DimenTool.dip2px(context, 20));

        keyTextView = (TextView) findViewById(R.id.imageEditViewText);
        keyTextView.setTextAppearance(context, R.style.default_form_key);
        keyTextView.setText("键");
        keyTextView.setMinWidth(DimenTool.dip2px(context, 40));

        editText = (EditText) findViewById(R.id.imageEditViewEdit);
        editText.setTextAppearance(context, R.style.default_text_medium_1);
        editText.setHint("请填写信息");
        editText.setHintTextColor(getResources().getColor(R.color.default_text_light_black2));
        editText.setMinWidth(DimenTool.dip2px(context, 180));
        editText.setId(BaseClassUtil.generateViewId());

        ivDelete = findViewById(R.id.ivDelete);
        ivDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                // Control the visibility of the delete button
                boolean isFocusable = editText.isFocusable();
                int visibility = (s.length() == 0 || !isFocusable) ? View.GONE : View.VISIBLE;
                if (ivDelete.getVisibility() != visibility) {
                    ivDelete.setVisibility(visibility);
                }

                if (maxLength <= 0) {
                    return;
                }

                String text = s.toString();
                int length = text.length() + BaseClassUtil.getNonAsciiCount(text);
                if (length > maxLength) {

                    // 初始光标位置
                    int selEndIndex = Selection.getSelectionEnd(editText.getText());
                    // 超出的长度，中文占两个长度
                    int extraSize = length - maxLength;

                    int charCount = text.length();
                    String str = text.substring(--charCount);
                    while (str.length() + BaseClassUtil.getNonAsciiCount(str) < extraSize) {
                        str = text.substring(--charCount);
                    }

                    str = text.substring(0, charCount);
                    editText.setText(str);

                    // 设置新的光标位置
                    if (selEndIndex > str.length()) {
                        selEndIndex = str.length();
                    }
                    Selection.setSelection(editText.getText(), selEndIndex);
                }
            }
        });

        mustView = (TextView) findViewById(R.id.imageEditViewMust);
        mustView.setVisibility(View.INVISIBLE);
        modifierView = (TextView) findViewById(R.id.imageEditViewModifier);
    }

    /**
     * 将所有控件移动到父类顶部
     */
    private void toTop() {
        ((RelativeLayout.LayoutParams) imageView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ((RelativeLayout.LayoutParams) keyTextView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ((RelativeLayout.LayoutParams) editText.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ((RelativeLayout.LayoutParams) mustView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ((RelativeLayout.LayoutParams) modifierView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ((RelativeLayout.LayoutParams) ivDelete.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
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
    public void setValue(String str) {
        editText.setText(str);
    }

    @Override
    public String getValue() {
        return editText.getText().toString();
    }

    @Override
    public String getKey() {
        return keyTextView.getText().toString();
    }

    public TextView getKeyTextView() {
        return keyTextView;
    }

    public void setLines(int lines) {
        editText.setLines(lines);

        if (lines > 2)
            toTop();
    }

    public void setMinLines(int lines) {
        editText.setMinLines(lines);

        if (lines > 2)
            toTop();
    }

    public void setEditable(boolean editable) {
        editText.setFocusable(editable);
    }

    public void setRequired(boolean required) {
        mustView.setVisibility(required ? View.VISIBLE : View.INVISIBLE);
    }

    public void setEditTextGravity(int gravity) {
        editText.setGravity(gravity);
    }

    public void setHint(String hintStr) {
        editText.setHint(hintStr);
    }

    public void setModifier(String modifier) {
        modifier = modifier == null ? "" : modifier.trim();
        String oldModifier = modifierView.getText().toString();
        if (!modifier.equals(oldModifier)) {
            ((RelativeLayout.LayoutParams) modifierView.getLayoutParams()).rightMargin = DimenTool.dip2px(getContext(), 16);
            modifierView.setText(modifier);
        }
    }

    public void setInputType(MmtInputType inputType) {
        if (inputType != null) {
            editText.setInputType(inputType.getInputType());
            editText.setHint("请填写数字");
        }
    }

    public enum MmtInputType {
        NUMBER(InputType.TYPE_CLASS_NUMBER),
        DECIMAL(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL),
        SIGNED_NUMBER(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        private final int inputType;
        MmtInputType(int inputType) {
            this.inputType = inputType;
        }
        public int getInputType() {
            return inputType;
        }
    }

}

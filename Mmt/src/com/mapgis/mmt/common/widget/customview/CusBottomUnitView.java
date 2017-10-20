package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

public class CusBottomUnitView extends BottomUnitView {

    public CusBottomUnitView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CusBottomUnitView(Context context) {
        this(context, null);
    }

    @Override
    protected void initView(Context context) {

        int margin = DimenTool.dip2px(context, 5);

        this.setClickable(true);
        this.setBackgroundResource(R.drawable.shape_blue);
        this.setOrientation(LinearLayout.HORIZONTAL);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        // layoutParams.setMargins(margin, margin, margin, margin);
        this.setGravity(Gravity.CENTER);
        this.setLayoutParams(layoutParams);

        imageView = new ImageView(context);
        LayoutParams iParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        iParams.rightMargin = margin;
        imageView.setLayoutParams(iParams);
        this.addView(imageView);

        textView = new TextView(context);
        textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setTextColor(Color.parseColor("#ffffff"));
        textView.getPaint().setFakeBoldText(true);
        this.addView(textView);

    }
}

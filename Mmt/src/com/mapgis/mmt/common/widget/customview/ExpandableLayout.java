package com.mapgis.mmt.common.widget.customview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class ExpandableLayout extends FrameLayout {
    private static final int IDLE = 0;
    private static final int EXPANDING = 1;
    private static final int COLLAPSING = 2;

    public static final String KEY_SUPER_STATE = "super_state";
    public static final String KEY_EXPANSION = "expansion";

    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;

    private static final int DEFAULT_DURATION = 300;

    private int duration = DEFAULT_DURATION;
    private boolean translateChildren;
    private float expansion;
    private int orientation;
    private int state = IDLE;

    private Interpolator interpolator = new FastOutSlowInInterpolator();
    private ValueAnimator animator;

    private OnExpansionUpdateListener expansionUpdateListener;
    private OnExpansionEndListener expansionEndListener;

    public ExpandableLayout(Context context) {
        super(context);
        init(null);
    }

    public ExpandableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ExpandableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ExpandableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        duration = DEFAULT_DURATION;
        expansion = 0;
        translateChildren = true;
        orientation = VERTICAL;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();

        expansion = isExpanded() ? 1 : 0;

        bundle.putFloat(KEY_EXPANSION, expansion);
        bundle.putParcelable(KEY_SUPER_STATE, superState);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        expansion = bundle.getFloat(KEY_EXPANSION);
        Parcelable superState = bundle.getParcelable(KEY_SUPER_STATE);

        super.onRestoreInstanceState(superState);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int size = orientation == LinearLayout.HORIZONTAL ? width : height;

        setVisibility(expansion == 0 && size == 0 ? GONE : VISIBLE);

        int expansionDelta = size - Math.round(size * expansion);
        if (translateChildren) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (orientation == HORIZONTAL) {
                    child.setTranslationX(-expansionDelta);
                } else {
                    child.setTranslationY(-expansionDelta);
                }
            }
        }

        if (orientation == HORIZONTAL) {
            setMeasuredDimension(width - expansionDelta, height);
        } else {
            setMeasuredDimension(width, height - expansionDelta);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (animator != null) {
            animator.cancel();
        }
        super.onConfigurationChanged(newConfig);
    }

    public boolean isExpanded() {
        return state == EXPANDING || expansion == 1;
    }

    public void toggle() {
        toggle(true);
    }

    public void toggle(boolean animate) {
        if (isExpanded()) {
            collapse(animate);
        } else {
            expand(animate);
        }
    }

    public void expand() {
        expand(true);
    }

    public void expand(boolean animate) {
        setExpanded(true, animate);
    }

    public void collapse() {
        collapse(true);
    }

    public void collapse(boolean animate) {
        setExpanded(false, animate);
    }

    public void setExpansion(float expansion) {
        if (this.expansion == expansion) {
            return;
        }

        setVisibility(expansion == 0 ? GONE : VISIBLE);

        this.expansion = expansion;
        requestLayout();

        if (expansionUpdateListener != null) {
            expansionUpdateListener.onExpansionUpdate(expansion);
        }
    }

    public void setOnExpansionUpdateListener(OnExpansionUpdateListener listener) {
        this.expansionUpdateListener = listener;
    }

    public void setOnExpansionEndListener(OnExpansionEndListener listener) {
        this.expansionEndListener = listener;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private void setExpanded(boolean expand, boolean animate) {
        if (expand && (state == EXPANDING || expansion == 1)) {
            return;
        }

        if (!expand && (state == COLLAPSING || expansion == 0)) {
            return;
        }

        int targetExpansion = expand ? 1 : 0;
        if (animate) {
            animateSize(targetExpansion);
        } else {
            setExpansion(targetExpansion);
        }
    }

    private void animateSize(final int targetExpansion) {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        animator = ValueAnimator.ofFloat(expansion, targetExpansion);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setExpansion((float) valueAnimator.getAnimatedValue());
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                state = targetExpansion == 0 ? COLLAPSING : EXPANDING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                state = IDLE;
                if (expansionEndListener != null) {
                    expansionEndListener.onExpansionEnd(targetExpansion == 1);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                state = IDLE;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        animator.start();
    }

    public interface OnExpansionUpdateListener {
        void onExpansionUpdate(float expansionFraction);
    }

    public interface OnExpansionEndListener {
        void onExpansionEnd(boolean isExpanded);
    }
}

package com.mapgis.mmt.module.navigation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.mapgis.mmt.constant.NavigationMenuRegistry;
import com.mapgis.mmt.entity.MenuItem;

public class NavigationItem implements Parcelable {
    /**
     * 模块描述
     */
    public MenuItem Function;

    /**
     * 模块所占矩形框
     */
    public Rect BackRect;

    /**
     * 是否被点击
     */
    public boolean IsClicked;

    /**
     * 数量显示，有多少未查看数据
     */
    public String Count;

    /**
     * 是否进行过语音提示
     */
    public boolean IsAlert;

    /**
     * 新信息是否进行过处理
     */
    public boolean doneNewMsg = true;

    /**
     * 语音提示的文本信息
     */
    public String SpeakMsg;

    /**
     * 背景颜色
     */
    public int BackColor;
    public int PressColor;
    /**
     * 显示所采用的图片
     */
    public Bitmap ShowBitmap;
    /**
     * 矩阵变换用的Matrix
     */
    public Matrix matrix;

    public NavigationItem() {
    }

    public void alertMsg(String msg) {
        this.SpeakMsg = msg;
        IsAlert = true;
    }

    public Bitmap getBitmap(View view) {
        if (ShowBitmap == null) {
            int icon = NavigationMenuRegistry.getInstance().
                    getMenuInstance((NavigationActivity) view.getContext(), this).getIcons()[0];

            ShowBitmap = BitmapFactory.decodeResource(view.getResources(), icon);
        }

        return ShowBitmap;
    }

    public Matrix getMatrix() {
        if (matrix == null) {

            matrix = new Matrix();

            // 屏幕分辨率过小时，bitmap会超出rect范围，此时将bitmap及text的大小缩小0.6倍
            if (BackRect.centerY() - ShowBitmap.getHeight() < BackRect.top) {
                matrix.postScale(0.6f, 0.6f);
                matrix.postTranslate(BackRect.centerX() - (ShowBitmap.getWidth() / 2 * 0.6f), BackRect.centerY()
                        - (ShowBitmap.getHeight() * 0.6f));
            } else {
                matrix.postTranslate(BackRect.centerX() - ShowBitmap.getWidth() / 2, BackRect.centerY() - ShowBitmap.getHeight());
            }
        }

        return matrix;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(Function, flags);
        out.writeBooleanArray(new boolean[]{doneNewMsg});

    }

    public static final Parcelable.Creator<NavigationItem> CREATOR = new Parcelable.Creator<NavigationItem>() {
        @Override
        public NavigationItem createFromParcel(Parcel in) {
            return new NavigationItem(in);
        }

        @Override
        public NavigationItem[] newArray(int size) {
            return new NavigationItem[size];
        }
    };

    private NavigationItem(Parcel in) {
        Function = in.readParcelable(MenuItem.class.getClassLoader());

        boolean[] b = new boolean[1];
        in.readBooleanArray(b);
        doneNewMsg = b[0];
    }
}

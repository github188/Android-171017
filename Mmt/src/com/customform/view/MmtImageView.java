package com.customform.view;

import android.content.Context;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageFragmentView;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;

/**
 * 图片、拍照
 * Created by zoro at 2017/9/1.
 */
class MmtImageView extends MmtBaseView implements MmtBaseView.ReadonlyHandleable {
    MmtImageView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_photo;
    }

    @Override
    public ImageFragmentView build() {
        ImageFragmentView view = new ImageFragmentView(getActivity());//@maoshoubei

        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        if (control.Validate.equals("1")) {
            view.isMustDo(true);
        }

        PhotoFragment takePhotoFragment = new PhotoFragment.Builder(control.relativePath)
                .setAddEnable(control.addEnable)
                .setSelectEnable(control.canSelect)
                .setValue(control.Value)
                .setMaxValueLength(control.MaxLength)
                .build();

        if (control.addEnable || control.Value.length() != 0) {
            view.addContentFragment(takePhotoFragment);
        }

        return view;
    }
}

package com.customform.view;

import android.content.Context;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.ImageFragmentView;
import com.mapgis.mmt.common.widget.fragment.RecorderFragment;

import java.util.List;

/**
 * 录音
 * Created by zoro at 2017/9/1.
 */
class MmtRecorderView extends MmtBaseView implements MmtBaseView.ReadonlyHandleable {
    MmtRecorderView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_record;
    }

    /**
     * 创建录音类型视图
     */
    public ImageFragmentView build() {
        ImageFragmentView view = new ImageFragmentView(getActivity());
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        if (control.Validate.equals("1")) {
            view.isMustDo(true);
        }

        RecorderFragment fragment = RecorderFragment.newInstance(control.relativePath);
        fragment.setRecoderEnable(control.addEnable);
        fragment.setHideDelBtn(!control.addEnable);

        // 字段长度限制
        fragment.setMaxValueLength(control.MaxLength);

        // 将已有录音初始化到界面上
        if (control.Value.length() != 0) {
            List<String> recorderList = BaseClassUtil.StringToList(control.Value, ",");
            fragment.setRelativeRec(recorderList);
            fragment.downloadFromWeb(control.Value);
        }

        if (control.addEnable || control.Value.length() != 0) {
            view.addContentFragment(fragment);
        }

        return view;
    }
}

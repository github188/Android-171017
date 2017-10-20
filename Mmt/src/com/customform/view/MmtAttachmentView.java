package com.customform.view;

import android.content.Context;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.attach.AttachFileFragment;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.ImageFragmentView;

/**
 * 附件
 * Created by zoro at 2017/9/1.
 */
public class MmtAttachmentView extends MmtBaseView implements MmtBaseView.ReadonlyHandleable {
    public MmtAttachmentView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_attachment;
    }

    /**
     * 创建附件的视图
     */
    public ImageFragmentView build() {
        ImageFragmentView view = new ImageFragmentView(getActivity());
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        if (control.Validate.equals("1")) {
            view.isMustDo(true);
        }

        AttachFileFragment.Builder builder = new AttachFileFragment.Builder(AttachFileFragment.FLAG_DEFAULT, control.relativePath);
        builder.setPathList(control.Value)
                .setAddEnable(control.addEnable)
                .setCanSelected(control.canSelect);

        if (control.addEnable || !BaseClassUtil.isNullOrEmptyString(control.Value)) {
            view.addContentFragment(AttachFileFragment.newInstance(builder));
        }

        return view;
    }
}

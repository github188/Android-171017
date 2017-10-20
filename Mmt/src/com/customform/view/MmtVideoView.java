package com.customform.view;

import android.content.Context;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.ImageFragmentView;
import com.mapgis.mmt.common.widget.fragment.VideoFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频
 * Created by zoro at 2017/9/1.
 */
class MmtVideoView extends MmtBaseView implements MmtBaseView.ReadonlyHandleable{
    MmtVideoView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.capture_video;
    }

    /**
     * 创建视频类型视图
     */
    public ImageFragmentView build() {
        ImageFragmentView view = new ImageFragmentView(getActivity());
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        VideoFragment.Builder builder = new VideoFragment.Builder(control.relativePath, control.relativePath);
        builder.setAddEnable(control.addEnable);
        builder.setSelectEnable(control.canSelect);
        builder.setMaxValueLength(control.MaxLength);
        VideoFragment fragment = builder.build();

        if (!BaseClassUtil.isNullOrEmptyString(control.Value)) {
            List<String> videoRelativeList = new ArrayList<>();
            List<String> photoRelativeList = new ArrayList<>();

            // 这里面包含了视频以及对应的图片缩略图
            // 格式：VID_19940102_784321.mp4,VID_19970203_994323.mp4|IMG_19940102_784321.jpg,IMG_19970203_994323.jpg
            String[] temp = control.Value.split(MyApplication.getInstance().mapGISFrame.getResources().getString(com.mapgis.mmt.R.string.split_flag_video_photo));
            String tempPhotoPath = "";
            videoRelativeList.addAll(BaseClassUtil.StringToList(temp[0], ","));
            if (temp.length == 2) {
                // 兼容之前的数据格式
                photoRelativeList.addAll(BaseClassUtil.StringToList(temp[1], ","));
                tempPhotoPath = temp[1];
            } else if (temp.length == 1) {
                // 拼接处视频缩略图格式
                tempPhotoPath = temp[0].replace(".mp4", ".jpg");
                photoRelativeList.addAll(BaseClassUtil.StringToList(tempPhotoPath, ","));
            }

            fragment.setRelativeVideo(videoRelativeList);
            fragment.setRelativePhoto(photoRelativeList);
        }

        if (control.addEnable || !BaseClassUtil.isNullOrEmptyString(control.Value)) {
            view.addContentFragment(fragment);
        }

        return view;
    }
}

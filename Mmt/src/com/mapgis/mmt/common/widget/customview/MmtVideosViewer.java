package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.toolbox.NetworkImageView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.PictureViewActivity;
import com.mapgis.mmt.config.ServerConnectConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 照片浏览通用控件
 */
public class MmtVideosViewer extends LinearLayout implements View.OnClickListener {
    public MmtVideosViewer(Context context) {
        this(context, null);
    }

    public MmtVideosViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MmtVideosViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    // 这是对应的视频中第一帧的集合
    private ArrayList<String> images;

    // 视频路径集合
    private ArrayList<String> videos;

    /**
     * 在线图片浏览
     *
     * @param images 图片相对路径列表
     */
    public void showByOnline(List<String> images) {
        try {
            if (images == null || images.size() == 0)
                return;

            this.images = new ArrayList<>();

            for (String img : images) {
                String url = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath()
                        + "/OutFiles/UpLoadFiles/" + img;

                url = Uri.encode(url, ":/");

                this.images.add(url);
            }

            for (int i = 0; i < this.images.size(); i += 2) {
                if (i % 2 != 0)
                    continue;

                LinearLayout layout = new LinearLayout(getContext());

                layout.setLayoutParams(new LayoutParams(-1, -2));
                layout.setOrientation(LinearLayout.HORIZONTAL);

                for (int j = i; j < i + 2; j++) {
                    NetworkImageView iv = new NetworkImageView(getContext());

                    LayoutParams params = new LayoutParams(0, DimenTool.dip2px(getContext(), 150), 1);

                    params.setMargins(2, 2, 2, 2);

                    iv.setLayoutParams(params);

                    if (j < this.images.size()) {
                        iv.setDefaultImageResId(R.drawable.no_image);
                        iv.setErrorImageResId(R.drawable.no_image);
                        iv.setImageUrl(this.images.get(j), MyApplication.getInstance().imageLoader);

                        iv.setTag(j);
                        iv.setOnClickListener(this);
                        iv.setVisibility(View.VISIBLE);
                    } else {
                        iv.setVisibility(View.INVISIBLE);
                    }

                    layout.addView(iv);
                }

                this.addView(layout);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 离线图片浏览
     *
     * @param images 图片绝对路径列表
     */
    public void showByOffline(List<String> images) {
        try {
            if (images == null || images.size() == 0)
                return;

            this.images = new ArrayList<>(images);

            for (int i = 0; i < this.images.size(); i += 2) {
                if (i % 2 != 0)
                    continue;

                LinearLayout layout = new LinearLayout(getContext());

                layout.setLayoutParams(new LayoutParams(-1, -2));
                layout.setOrientation(LinearLayout.HORIZONTAL);

                for (int j = i; j < i + 2; j++) {
                    ImageView iv = new ImageView(getContext());

                    LayoutParams params = new LayoutParams(0, DimenTool.dip2px(getContext(), 150), 1);

                    params.setMargins(2, 2, 2, 2);

                    iv.setLayoutParams(params);

                    if (j < this.images.size()) {
                        File file = new File(this.images.get(j));

                        if (file.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(this.images.get(j));

                            iv.setImageBitmap(bitmap);
                        } else {
                            iv.setImageResource(R.drawable.no_image);
                        }

                        iv.setTag(j);
                        iv.setOnClickListener(this);
                        iv.setVisibility(View.VISIBLE);
                    } else {
                        iv.setVisibility(View.INVISIBLE);
                    }

                    layout.addView(iv);
                }

                this.addView(layout);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getContext(), PictureViewActivity.class);

        intent.putStringArrayListExtra("fileList", images);
        intent.putExtra("pos", (int) v.getTag());
        intent.putExtra("canDelete", false);

        getContext().startActivity(intent);
    }
}

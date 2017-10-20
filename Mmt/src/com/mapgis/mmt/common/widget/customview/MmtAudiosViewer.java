package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mapgis.mmt.CacheUtils;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.RecorderPlayActivity;
import com.mapgis.mmt.config.ServerConnectConfig;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 通用录音列表显示控件
 */
public class MmtAudiosViewer extends HorizontalScrollView implements View.OnClickListener {
    public MmtAudiosViewer(Context context) {
        this(context, null);
    }

    public MmtAudiosViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MmtAudiosViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private ArrayList<String> audios;
    private boolean isOnline;

    /**
     * 显示在线录音列表
     *
     * @param audios 录音文件相对路径列表
     */
    public void showByOnline(List<String> audios) {
        this.show(audios, true);
    }

    /**
     * 显示离线录音列表
     *
     * @param audios 录音文件绝对路径列表
     */
    public void showByOffline(List<String> audios) {
        this.show(audios, false);
    }

    /**
     * 显示录音列表
     *
     * @param audios   录音文件路径列表
     * @param isOnline 是否在线文件
     */
    public void show(List<String> audios, boolean isOnline) {
        try {
            this.audios = new ArrayList<>(audios);
            this.isOnline = isOnline;

            LinearLayout layout = new LinearLayout(getContext());

            layout.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));

            for (int i = 0; i < audios.size(); i++) {
                ImageView iv = new ImageView(getContext());

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DimenTool.dip2px(getContext(), 50), DimenTool.dip2px(getContext(), 50));

                params.setMargins(0, 0, 2, 0);

                iv.setLayoutParams(params);
                iv.setImageResource(R.drawable.tape);
                iv.setTag(i);
                iv.setOnClickListener(this);

                layout.addView(iv);
            }

            this.addView(layout);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<String> fileList;
    private int pos;

    @Override
    public void onClick(View v) {
        try {
            this.pos = (int) v.getTag();

            if (isOnline) {
                fileList = new CopyOnWriteArrayList<>();

                for (String audio : this.audios) {
                    String url = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath()
                            + "/OutFiles/UpLoadFiles/" + audio;

                    url = Uri.encode(url, ":/");

                    String key = CacheUtils.hashKeyForDisk(url);

                    if (BaseClassUtil.isNullOrEmptyString(key))
                        key = UUID.randomUUID().toString();

                    String path = Battle360Util.getFixedPath("Temp") + key + audio.substring(audio.lastIndexOf('.'));

                    fileList.add(path);

                    File file = new File(path);

                    if (file.exists()) {
                        continue;
                    }

                    FinalHttp http = new FinalHttp();

                    http.download(url, path, new AjaxCallBack<File>() {
                        @Override
                        public void onSuccess(File file) {
                            fileList.add(file.getAbsolutePath());

                            startRecorderPlayActivity();
                        }

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            fileList.add("");

                            startRecorderPlayActivity();
                        }
                    });
                }
            } else {
                this.fileList = this.audios;
            }

            startRecorderPlayActivity();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private synchronized void startRecorderPlayActivity() {
        if (this.fileList.size() != this.audios.size())
            return;

        Intent intent = new Intent(getContext(), RecorderPlayActivity.class);

        intent.putStringArrayListExtra("fileList", new ArrayList<>(this.fileList));
        intent.putExtra("hideDelBtn", true);
        intent.putExtra("pos", pos);

        getContext().startActivity(intent);
    }
}

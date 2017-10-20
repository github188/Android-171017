package com.repair.reporthistory;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;
import com.mapgis.mmt.common.widget.fragment.RecorderFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.R;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PatrolEventDetailFragment extends Fragment {
    private PhotoFragment takePhotoFragment;
    private RecorderFragment recorderFragment;
    private PatrolEventEntityTrue entity;
    private final String buffPath = ServerConnectConfig.getInstance()
            .getCityServerMobileBufFilePath() + "/OutFiles/UpLoadFiles/";
    private int netImageCount = 0;
    private int netRecordCount = 0;
    private Boolean hasNetImage = false;
    private Boolean hasNetRecord = false;
    private String flag = "list";

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public PatrolEventDetailFragment() {
        super();
    }

    public PatrolEventDetailFragment(PatrolEventEntityTrue entity) {
        this.entity = entity;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        try {
            if (this.entity.ImageUrl != null
                    && this.entity.ImageUrl.length() > 0) {

                for (String url : this.entity.ImageUrl.split(",")) {
                    url = buffPath + url;
                    String path = Battle360Util.getFixedPath("Media", false) +
                            url.substring(url.indexOf("UpLoadFiles")
                                            + "UpLoadFiles".length(),
                                    url.lastIndexOf("/") + 1);
                    String name = url.substring(url.lastIndexOf("/") + 1);
                    String file = path + name;
                    if (!(new File(file)).exists()) {
                        hasNetImage = true;
                        netImageCount++;
                        this.downloadImage(url, path, name);
                    }
                }
            }
            if (this.entity.AudiosUrl != null
                    && this.entity.AudiosUrl.length() > 0) {
                this.entity.AudiosUrl = this.entity.AudiosUrl.replace("wav",
                        "amr");
                for (String url : this.entity.AudiosUrl.split(",")) {
                    url = buffPath + url;
                    String path = Battle360Util.getFixedPath("Record", false)
                            + url.substring(url.indexOf("UpLoadFiles")
                                    + "UpLoadFiles".length(),
                            url.lastIndexOf("/") + 1);
                    String name = url.substring(url.lastIndexOf("/") + 1);
                    String file = path + name;
                    if (!(new File(file)).exists()) {
                        hasNetRecord = true;
                        netRecordCount++;
                        this.downloadRecord(url, path, name);
                    }
                }
            }
        } catch (Exception ex) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.patrol_report_event_deatil_fragment, null);
        ((PatrolReportHistoryActivity) getActivity()).getBaseTextView()
                .setText(
                        ((PatrolReportHistoryActivity) getActivity())
                                .getDetailTitle());
        // 显示定位按钮
        ((BaseActivity) getActivity()).getBaseRightImageView().setVisibility(
                View.VISIBLE);
        ((BaseActivity) getActivity()).getBaseRightImageView()

                .setImageResource(R.drawable.common_location);
        if (entity != null && view != null) {

            if (BaseClassUtil.isNullOrEmptyString(entity.getEventCode())) {
                view.findViewById(R.id.eventCodeLyt).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.eventCodeLyt).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.txtEventCode))
                        .setText(entity.getEventCode());
            }
            ((TextView) view.findViewById(R.id.txtEventState))
                    .setText(entity.getEventState());
            ((TextView) view.findViewById(R.id.bigClassText))
                    .setText(entity.EventType);
            ((TextView) view.findViewById(R.id.smallClassText))
                    .setText(entity.EventClass);
            ((TextView) view.findViewById(R.id.coordinateText))
                    .setText(entity.Position);
            ((TextView) view.findViewById(R.id.addressText))
                    .setText(entity.Address);
            ((TextView) view.findViewById(R.id.descriptionText))
                    .setText(entity.Description);
            ((TextView) view.findViewById(R.id.reportTimeText))
                    .setText(entity.getReportTime().replace('T', ' '));
            ((TextView) view.findViewById(R.id.reportNameText))
                    .setText(entity.ReportName);
            Boolean hasImage = (entity.ImageUrl != null
                    && entity.ImageUrl.length() > 0 && hasNetImage == false);
            Boolean hasRecoird = (entity.AudiosUrl != null
                    && entity.AudiosUrl.length() > 0 && hasNetRecord == false);
            if (hasImage || hasRecoird) {
                FragmentTransaction ft = getFragmentManager()
                        .beginTransaction();
                if (hasImage) {
                    takePhotoFragment = new PhotoFragment.Builder(getAbsolutePath(entity.ImageUrl))
                                .setAddEnable(false).build();
                    List<String> mediaList = str2ArrayList(Battle360Util.getFixedPath("Media"), entity.ImageUrl);
                    takePhotoFragment.setAbsolutePhoto(mediaList);
                    ft.replace(R.id.patrolReportImageFrameLayout, takePhotoFragment);
                    ft.show(takePhotoFragment);
                }
                if (hasRecoird) {

                    recorderFragment = RecorderFragment.newInstance(getAbsolutePath(entity.AudiosUrl));
                    ft.replace(R.id.patrolReportRecordFrameLayout, recorderFragment);
                    ft.show(recorderFragment);
                    //   FileUtil.getSDPath();
                    List<String> recorderList = str2ArrayList(
                            Battle360Util.getFixedPath("Record"),
                            entity.AudiosUrl.replace("wav", "amr"));
                    recorderFragment.setRecoderEnable(false);
                    recorderFragment.setAbsoluteRec(recorderList);
                    recorderFragment.setHideDelBtn(true);
                } else {
                    // recorderFragment.
                }
                ft.commit();
            }
            ((BaseActivity) getActivity()).getBaseRightImageView()
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            BaseMapCallback callback = new ShowMapPointCallback(
                                    getActivity(), entity.Position, entity
                                    .getEventCode(), entity.Address, -1);

                            MyApplication.getInstance().sendToBaseMapHandle(
                                    callback);
                        }
                    });

        }
        return view;
    }

    public ArrayList<String> str2ArrayList(String prefixAbsolutePath,
                                           String relativePath) {
        ArrayList<String> list = new ArrayList<String>();
        if (BaseClassUtil.isNullOrEmptyString(relativePath)) {
            return list;
        }
        String[] paths = relativePath.split(",");
        for (int i = 0; i < paths.length; i++) {
            String allPath = prefixAbsolutePath + paths[i];
            if (new File(allPath).exists()) {
                list.add(allPath);
            }
        }
        return list;
    }

    private void downloadRecord(String url, String path, String name) {
        try {
            //iis默认只支持wav格式,下载时需要下载wav文件
            url = Uri.encode(url.replace("amr", "wav"), ":/");

            FinalHttp fh = new FinalHttp();
            String file = path + name;
            //下载录音时要手动创建目录，否则下载失败，原因未知
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }
            // 调用download方法开始下载
            fh.download(url, file, false, new AjaxCallBack<File>() {
                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {

                    //Log.e("文件下载失败！");
                    Toast.makeText(getActivity(), "录音下载失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoading(long count, long current) {
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onSuccess(File t) {
                    try {
                        if (getActivity() == null
                                || getActivity().isFinishing())
                            return;

                        netRecordCount--;
                        // 所有图片下载完毕
                        if (netRecordCount == 0) {
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            recorderFragment = RecorderFragment.newInstance(getAbsolutePath(entity.AudiosUrl));
                            ft.replace(R.id.patrolReportRecordFrameLayout, recorderFragment);
                            ft.show(recorderFragment);
                            FileUtil.getSDPath();
                            List<String> recorderList = str2ArrayList(Battle360Util.getFixedPath("Record"),
                                    entity.AudiosUrl.replace("wav", "amr"));
                            recorderFragment.setRecoderEnable(false);
                            recorderFragment.setAbsoluteRec(recorderList);
                            recorderFragment.setHideDelBtn(true);
                            ft.commit();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void downloadImage(String url, String path, String name) {
        try {
            url = Uri.encode(url, ":/");

            FinalHttp fh = new FinalHttp();

            String file = path + name;
            // 调用download方法开始下载
            fh.download(url, file, false, new AjaxCallBack<File>() {
                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {
                    //  Log.e("文件下载失败！");
                    Toast.makeText(getActivity(), "图片下载失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoading(long count, long current) {
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onSuccess(File t) {
                    try {
                        if (getActivity() == null
                                || getActivity().isFinishing())
                            return;

                        netImageCount--;
                        // 所有图片下载完毕
                        if (netImageCount == 0) {
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            takePhotoFragment = new PhotoFragment.Builder(getAbsolutePath(entity.ImageUrl))
                                        .setAddEnable(false).build();
                            List<String> mediaList = str2ArrayList(Battle360Util.getFixedPath("Media"), entity.ImageUrl);
                            takePhotoFragment.setAbsolutePhoto(mediaList);
                            ft.replace(R.id.patrolReportImageFrameLayout, takePhotoFragment);
                            ft.show(takePhotoFragment);
                            ft.commit();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getAbsolutePath(String paths) {
        if (paths == null || paths.length() <= 0) {
            return "";
        }
        String[] ps = paths.split(",");
        return ps[0].substring(0, ps[0].lastIndexOf("/") + 1);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (flag.equals("map")) {
            ((BaseActivity) getActivity()).getBaseRightImageView()
                    .setVisibility(View.GONE);
        }
    }
}

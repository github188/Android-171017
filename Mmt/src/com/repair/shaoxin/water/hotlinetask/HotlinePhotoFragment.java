package com.repair.shaoxin.water.hotlinetask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;
import com.mapgis.mmt.config.ServerConnectConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HotlinePhotoFragment extends PhotoFragment {

    private static final String ARG_RELATIVE_PHOTO_PATH = "relativePhoto";

    public static HotlinePhotoFragment newInstance(String relativePath, String relativePhoto) {
        HotlinePhotoFragment fragment = new HotlinePhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RELATIVE_PATH_SEGMENT, relativePath);
        args.putString(ARG_RELATIVE_PHOTO_PATH, relativePhoto);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void downloadFromWeb() {

        String relativePhoto = "";

        Bundle args = getArguments();
        if (args != null) {
            relativePhoto = args.getString(ARG_RELATIVE_PHOTO_PATH);
        }
        if (TextUtils.isEmpty(relativePhoto)) {
            return;
        }

        AsyncTask<String, Void, Rnt> mmtBaseTask = new AsyncTask<String, Void, Rnt>() {

            String picName;

            @Override
            protected Rnt doInBackground(String... params) {
                Rnt result;

                String baseUrl = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/CallCenterREST.svc/DownloadByteResource?path=";

                try {
                    final String localUrl = params[0];
                    picName = localUrl.substring(localUrl.lastIndexOf("/") + 1);
                    if (new File(MyApplication.getInstance().getMediaPathString() + picName).exists()) {
                        result = new Rnt();
                        result.ResultCode = 100;
                        return result;
                    }

                    String resultStr = NetUtil.executeHttpGet(baseUrl + localUrl);
                    result = new Gson().fromJson(resultStr, Rnt.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    result = new Rnt();
                    result.ResultCode = -100;
                    result.ResultMessage = "照片下载失败";
                }
                return result;
            }

            @Override
            protected void onPostExecute(Rnt rnt) {
                if (rnt.ResultCode == -100) {
                    Toast.makeText(getContext(), rnt.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (rnt.ResultCode == 100) {
                    String absolutePath = MyApplication.getInstance().getMediaPathString() + picName;
                    if (!photoAbsolutePaths.contains(absolutePath)) {
                        photoAbsolutePaths.add(absolutePath);
                    }
                    ImageView imageView = createImageView(absolutePath);
                    addImageView(imageView);
                    return;
                }

                if (rnt.ResultCode == 200) {
                    for (int i = 0, length = rnt.DataList.length; i < length; i++) {

                        String absolutePath;
                        if (i == 0) {
                            absolutePath = MyApplication.getInstance().getMediaPathString() + picName;
                        } else {
                            absolutePath = MyApplication.getInstance().getMediaPathString() + i + picName;
                        }
                        File file = new File(absolutePath);

                        if (!photoAbsolutePaths.contains(absolutePath)) {
                            photoAbsolutePaths.add(absolutePath);
                        }
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }

                        BufferedInputStream bis = null;
                        BufferedOutputStream bos = null;
                        try {
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            bis = new BufferedInputStream(new ByteArrayInputStream(rnt.DataList[i]));
                            bos = new BufferedOutputStream(new FileOutputStream(file));

                            byte[] bt = new byte[1024 * 8];
                            int len = bis.read(bt);
                            while (len != -1) {
                                bos.write(bt, 0, len);
                                len = bis.read(bt);
                            }
                            bos.flush();

                            ImageView imageView = createImageView(absolutePath);
                            addImageView(imageView);

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (bis != null) {
                                try {
                                    bis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (bos != null) {
                                try {
                                    bos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        };
        mmtBaseTask.executeOnExecutor(MyApplication.executorService, relativePhoto);
    }

    static class Rnt {
        public int ResultCode;
        public String ResultMessage;
        public byte[][] DataList;
    }
}

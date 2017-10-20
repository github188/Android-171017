package com.mapgis.mmt.module.gis.onliemap.dynamic;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.Document;
import com.zondy.mapgis.map.Map;
import com.zondy.mapgis.util.objects.ObjectIDs;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class EmsDBMapServer extends BaseDynamicMapServer {
    private Document document = new Document();
    private Map map = new Map();
    private String name;

    public EmsDBMapServer(String name) {
        try {
            this.name = name;
            String path = MyApplication.getInstance().getMapFilePath() + name + "/" + name + ".mapx";

            document.open(path);

            map = document.getMaps().getMap(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        BaseClassUtil.loge(this, "完成构造函数[document.getMaps().getMap(0)]");
    }

    @Override
    public Rect getEntireExtent() {
        try {
            return map.getEntireRange();
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        } finally {
            BaseClassUtil.loge(this, "完成getEntireExtent");
        }
    }

    @Override
    public String getName() {
        BaseClassUtil.loge(this, "getName-" + (TextUtils.isEmpty(document.getTitle()) ? "" : document.getTitle()));

        return this.name;
    }

    @Override
    public String getURL() {
        BaseClassUtil.loge(this, "getURL");

        return "";
    }

    private final static Object syncRoot = new ObjectIDs();
    private AtomicInteger index = new AtomicInteger(0);

    @Override
    public synchronized byte[] getVectorImage(long imgWidth, long imgHeight, double dispRectXmin, double dispRectYmin, double dispRectXmax,
                                              double dispRectYmax, String strShowLayers, int imageType) {
        synchronized (syncRoot) {
            int i = index.incrementAndGet();

            BaseClassUtil.loge(this, "开始getVectorImage-" + imgWidth + "-" + i);

            Bitmap bitmap = null;
            ByteArrayOutputStream out = null;

            try {
                bitmap = Bitmap.createBitmap((int) imgWidth, (int) imgHeight, Config.ARGB_8888);

                map.outputToBitmap(
                        new Rect(dispRectXmin, dispRectYmin, dispRectXmax, dispRectYmax), bitmap);

                out = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                out.flush();

                return out.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();

                return null;
            } finally {
                try {
                    if (out != null)
                        out.close();

                    if (bitmap != null)
                        bitmap.recycle();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                BaseClassUtil.loge(this, "完成getVectorImage-" + imgWidth + "-" + i);

                if (i > Integer.MAX_VALUE - 10000)
                    index.set(0);
            }
        }
    }
}
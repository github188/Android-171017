package com.mapgis.mmt.module.gis.onliemap.tile;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.CacheUtils;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.systemsetting.SettingUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;

public class MmtTileMapServer extends BaseTileMapServer {
    private static final long serialVersionUID = 1L;
    private SQLiteDatabase db = null;
    protected String mapName = "";
    String url = "";

    MmtTileMapServer() {
    }

    public MmtTileMapServer(String name) {
        try {
            this.mapName = name;//传入的是瓦片服务名，如：gswp
            this.url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_Tile/REST/TileREST.svc/" + name + "/MapServer";

            setMapServiceInfo(this.url);

            initCacheDB(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void initCacheDB(boolean checkUpdate) {
        Cursor cursor = null;

        try {
            String name = CacheUtils.hashKeyForDisk(url);

            String dir = Battle360Util.getFixedMapGISPath(false) + "/Tiles/";

            if (!new File(dir).exists())
                new File(dir).mkdirs();

            db = SQLiteDatabase.openOrCreateDatabase(dir + name + ".db", null);

            MobileConfig.MapConfigInstance.getMapLayerByName(mapName).cachePath = dir + name + ".db";

            String sql = "select count(*) from sqlite_master where type='table' and name='TileImage' collate nocase";

            cursor = db.rawQuery(sql, null);

            // 表已经存在
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                try {
                    if (checkUpdate && !TextUtils.isEmpty(mapName)) {
                        String json = NetUtil.executeHttpGet(ServerConnectConfig.getInstance().getMobileBusinessURL()
                                + "/BaseREST.svc/GetMapTime?mapFileName=" + mapName + "&type=tiled");

                        if (!TextUtils.isEmpty(json)) {
                            ResultData<String> data = new Gson().fromJson(json, new TypeToken<ResultData<String>>() {
                            }.getType());

                            if (data.ResultCode > 0) {
                                //服务返回的是文件的本地最后修改时间，此处是以Locale.CHINA转换的时间，
                                //所以取出的getTime也是UTC时间戳,即距1970-01-01的毫秒数
                                Date lastDate = BaseClassUtil.parseTime(data.getSingleData());

                                if (lastDate != null) {
                                    cursor.close();

                                    cursor = db.rawQuery("select min(time) from TileImage", null);

                                    if (cursor.moveToFirst()) {
                                        long time = cursor.getLong(0);

                                        if (time > 0 && time < lastDate.getTime()) {
                                            cursor.close();

                                            db.close();

                                            new File(dir + name + ".db-journal").delete();
                                            new File(dir + name + ".db").delete();

                                            initCacheDB(false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                sql = "CREATE TABLE TileImage (level INTEGER NOT NULL,row INTEGER NOT NULL,col INTEGER NOT NULL,"
                        + "time INTEGER,data BLOB,PRIMARY KEY (level, row, col))";

                db.execSQL(sql);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
    }

    @Override
    public byte[] getTileImage(long row, long col, long level) {
        if (TextUtils.isEmpty(this.url)) {
            return null;
        }

        long now = System.currentTimeMillis();
        byte[] buffer = null;
        boolean isCache = false;

        try {
            try {
                if (this.db != null) {
                    Cursor cursor = db.rawQuery("select data from TileImage where (level=" + level + " and row=" + row + " and col=" + col
                            + ") or (level=-1 and row =-1 and col =-1) limit 0,1", null);

                    if (cursor.moveToFirst()) {
                        buffer = cursor.getBlob(0);
                    }

                    cursor.close();
                }
            } catch (Exception ex) {
                if (ex instanceof SQLiteDiskIOException)//防止缓存db文件已经被人为删除，比如清空缓存操作
                    this.db = null;

                ex.printStackTrace();
            }

            if (buffer != null && buffer.length > 512) {
                isCache = true;
            } else {
                String url = this.url + "/Tile/" + level + "/" + row + "/" + col;

                buffer = NetUtil.executeHttpGetBytes(30, url);

                if (buffer != null && buffer.length > 512 && this.db != null) {
                    try {
                        ContentValues values = new ContentValues();

                        values.put("level", level);
                        values.put("row", row);
                        values.put("col", col);
                        values.put("time", now);
                        values.put("data", buffer);

                        db.insert("TileImage", null, values);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String msg = "[" + (level + "/" + row + "/" + col) + "]" + "[" + (System.currentTimeMillis() - now) + "ms]";

        if (buffer != null)
            msg += "[" + (buffer.length > 1024 ? ((buffer.length / 1024) + "KB") : (buffer.length + "B")) + "]";
        else
            msg += "[NOT GET TILE]";

        msg += isCache ? "[CACHE]" : "[SERVER]";

        BaseClassUtil.logd(this, msg);

        buffer = addWaterMarkOnBitmap(buffer, "L" + level, " R" + row, " C" + col);

        return buffer;
    }

    private byte[] addWaterMarkOnBitmap(byte[] data, String... args) {
        byte[] buffer;

        try {
            if (!MyApplication.getInstance().getConfigValue(SettingUtil.Config.SHOW_TILE_GRID, false))
                return data;

            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inMutable = true;

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

            int w = bitmap.getWidth();

            Canvas canvas = new Canvas(bitmap);

            Paint p = new Paint();

            p.setColor(Color.RED);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);

            canvas.drawRect(0, 0, w, w, p);

            p.reset();

            Typeface font = Typeface.create("宋体", Typeface.NORMAL);

            p.setColor(Color.RED);
            p.setTypeface(font);

            p.setTextSize(30);

            p.setAntiAlias(true);

            for (int i = 0; i < 3; i++) {
                String text = args[i];

                Rect rect = new Rect();

                p.getTextBounds(text, 0, text.length(), rect);

                switch (i) {
                    case 0:
                        canvas.drawText(text, (w - rect.width()) / 2, (w - 3 * rect.height()) / 2 - 15, p);
                        break;
                    case 1:
                        canvas.drawText(text, (w - rect.width()) / 2, (w - rect.height()) / 2, p);
                        break;
                    case 2:
                        canvas.drawText(text, (w - rect.width()) / 2, (w + rect.height()) / 2 + 15, p);
                        break;
                }
            }

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            buffer = outputStream.toByteArray();

            outputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();

            buffer = data;
        }

        return buffer;
    }
}

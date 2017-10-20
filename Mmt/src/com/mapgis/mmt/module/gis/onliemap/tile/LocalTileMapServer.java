package com.mapgis.mmt.module.gis.onliemap.tile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;

import java.io.File;
import java.util.Date;

public class LocalTileMapServer extends BaseTileMapServer {
    private static final long serialVersionUID = 1L;
    private SQLiteDatabase db = null;

    public LocalTileMapServer(String name) {
        try {
            String path = MyApplication.getInstance().getMapFilePath() + name + "/" + name + ".xml";

            setMapServiceInfo(path);

            path = path.substring(0, path.length() - 4) + ".db";

            if (new File(path).exists()) {
                db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public byte[] getTileImage(long row, long col, long level) {
        if (this.db == null)
            return null;

        long now = new Date().getTime();
        byte[] buffer = null;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("select tileData from TileImage where (level=" + level + " and row=" + row + " and col=" + col
                    + ") or (level=-1 and row =-1 and col =-1) limit 0,1", null);

            if (cursor.moveToFirst()) {
                buffer = cursor.getBlob(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        String msg = "[" + (level + "/" + row + "/" + col) + "]" + "[" + (new Date().getTime() - now) + "ms]";

        if (buffer != null)
            msg += "[" + (buffer.length > 1024 ? ((buffer.length / 1024) + "KB") : (buffer.length + "B")) + "]";
        else
            msg += "[NOT GET TILE]";

        msg += "[CACHE]";

        BaseClassUtil.logd(this, msg);

        return buffer;
    }
}

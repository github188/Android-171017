package com.mapgis.mmt.common.util;

import android.os.Environment;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MapLayerConfig;
import com.mapgis.mmt.config.MobileConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lyunfan on 16/11/4.
 */
public class Battle360Util {

    public static final String newMapGISName = "CityMobile";
    //最后不带／
    public static String newMapGISPath = "";

    //无论是civFiles、mmtDB 还是multMediaFiles，原先都默认在内置存储卡
    //Tiles既不备份也不还原,但在MapGIS保留一个空的Tiles
    private static final String[] civFiles = new String[]{"Clib", "Config", "Fonts", "Slib", "Vlib"};
    private static final String mmtDB = "Data";
    private static final String[] multMediaFiles = new String[]{"crash", "Media", "Record", "Temp", "UserImage"};

    private static final String tiles = "Tiles";

    private static final String internalMapGISPath = Environment.getExternalStorageDirectory() + "/MapGIS";


    public enum GlobalPath {
        Map("Map"),
        Media("Media"),
        Record("Record"),
        UserImage("UserImage"),
        Temp("Temp"),
        Crash("crash"),
        Data("Data"),
        Conf("Conf");
        final String path;

        GlobalPath(String path) {
            this.path = path;
        }
    }

    /**
     * 返回离线地图的Map文件夹，最后带有／
     *
     * @return
     */
    public static String doWork() {

        //只备份，MapGis文件夹在还原时创建（创建在内置存储卡）
        String mapPath = renameMapGIS();

        //从citymobile和mapgis下查找db所在目录，处理citymobile下db被删除，但Mapgis下还有db的情况
        String mapPathContainDB = mapPath;
        if (!FileUtil.isExistInDir(mapPathContainDB, ".db")) {
            mapPathContainDB = findMapFilePath();
        }
        if (!FileUtil.isExistInDir(mapPathContainDB, ".db")) {
            mapPathContainDB = mapPath;
        }

        if (!mapPath.contains(newMapGISName)) {

            return mapPathContainDB;
        }
        newMapGISPath = new File(mapPath).getParentFile().getAbsolutePath();

        bakCIVFile();

        //避免可能还原不成功，重复还原3次
        int i = 0;
        for (i = 0; i < 3; i++) {
            boolean isSuccees = restoreCIVs();
            if (isSuccees) {
                break;
            }
        }
        if (i == 3) {
            MyApplication.getInstance().showMessageWithHandle("基础数据缺失");
        }

        return mapPathContainDB;
    }


    /**
     * 查找离线地图存放路径,不同系统的SD卡指向的路径可能不同
     */
    public static String findMapFilePath() {
        String relativePath = "/MapGIS/Map/";

        File innerDir = Environment.getExternalStorageDirectory();
        try {
            StringBuffer sb = new StringBuffer();

            sb.append("开始检测路径\n");

            sb.append(innerDir.getAbsolutePath() + "\n");

            File rootDir = innerDir.getParentFile();

            sb.append(rootDir.getAbsolutePath() + "\n");

            File storageDir = rootDir.getParentFile();

            sb.append(storageDir.getAbsolutePath() + "\n");

            sb.append("开始循环查找01\n");

            if (storageDir.canRead()) {
                File[] storageFiles = storageDir.listFiles();
                for (File file : storageFiles) {
                    String absolutePath = file.getAbsolutePath() + relativePath;

                    if (new File(absolutePath).exists()) { // /MapGIS/map/
                        // 文件夹存在，并且此文件夹下存在 .mapx
                        // 后缀名的 文件

                        sb.append(absolutePath + "----存在\n");

                        if (FileUtil.isExistInDir(absolutePath, ".db")) {
                            return absolutePath;
                        }
                    } else {
                        sb.append(absolutePath + "----不存在\n");
                    }
                }
            }

            sb.append("开始循环查找02\n");

            if (rootDir.canRead()) {
                File[] files = rootDir.listFiles();
                for (File file : files) {
                    String absolutePath = file.getAbsolutePath() + relativePath;

                    if (new File(absolutePath).exists()) {
                        sb.append(absolutePath + "----存在\n");

                        return absolutePath;
                    } else {
                        sb.append(absolutePath + "----不存在\n");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return innerDir.getAbsolutePath() + relativePath;
    }

    /**
     * 重命名mapgis文件夹
     * 返回新目录下离线地图Map路径
     *
     * @return 路径
     */
    private static String renameMapGIS() {

        String newMapGISPath = findNewMapGISFilePath();
        //已经重命名过
        if (!TextUtils.isEmpty(newMapGISPath)) {
            return newMapGISPath + "Map/";
        }
        //开始重命名,以有地图的MapGIS为标准
        //不管有没有db文件都会重命名
        String mapPath = findMapFilePath();
        File mapFile = new File(mapPath);
        if (!mapFile.exists() && !mapFile.mkdirs()) {
            return mapPath;
        }
        File mapGisFile = mapFile.getParentFile();
        newMapGISPath = mapGisFile.getParentFile().getAbsolutePath() + "/" + newMapGISName;
        File newMapGISFile = new File(newMapGISPath);
        boolean isRenamed = mapGisFile.renameTo(newMapGISFile);
        if (!isRenamed) {
            return mapPath;
        }
        return newMapGISPath + "/Map/";
    }

    private static void bakCIVFile() {

        //主线程备份部分
        List<String> mainBaks = new ArrayList<>();
        mainBaks.addAll(Arrays.asList(civFiles));
        mainBaks.add(mmtDB);

        bakFile(mainBaks);

        //子线程备份部分

        MyApplication.getInstance().submitExecutorService(new Runnable() {
            @Override
            public void run() {
                bakFile(Arrays.asList(multMediaFiles));
            }
        });

    }

    private static void bakFile(List<String> files) {
        for (String civ : files) {
            String newMapGISCIVPath = newMapGISPath + "/" + civ;
            File newMapGISCIVFile = new File(newMapGISCIVPath);
            if (newMapGISCIVFile.exists()) {
                continue;
            }

            String mapGISCIVPath = internalMapGISPath + "/" + civ;
            File mapGISCIVFile = new File(mapGISCIVPath);
            if (!mapGISCIVFile.exists()) {
                continue;
            }

            try {
                FileUtil.copyFile(mapGISCIVFile, newMapGISCIVFile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 拷贝 clib等文件到内置存储卡的MapGIS文件夹下
     *
     * @return 结果
     */
    private static boolean restoreCIVs() {
        File internalMapGISFile = new File(internalMapGISPath);
        if (!internalMapGISFile.exists() && !internalMapGISFile.mkdirs()) {
            return false;
        }

        for (String civ : civFiles) {

            String newMapGISCIVPath = newMapGISPath + "/" + civ;
            File newMapGISCIVFile = new File(newMapGISCIVPath);
            if (!newMapGISCIVFile.exists()) {
                continue;
            }

            String mapGISCIVPath = internalMapGISPath + "/" + civ;
            File mapGISCIVFile = new File(mapGISCIVPath);
            if (!mapGISCIVFile.exists()) {
                boolean isSuccees = mapGISCIVFile.mkdirs();
                if (!isSuccees) {
                    return false;
                }
            }

            try {
                for (File file : newMapGISCIVFile.listFiles()) {
                    String fileName = file.getName();
                    String internalFilePath = mapGISCIVPath + "/" + fileName;
                    File internalcivFile = new File(internalFilePath);
                    if (!internalcivFile.exists()) {
                        boolean isSuccees = FileUtil.copyFile(file, internalcivFile);
                        if (!isSuccees) {
                            return false;
                        }
                        continue;
                    }
                    long newTimes = file.lastModified();
                    long oldTimes = internalcivFile.lastModified();
                    if (newTimes > oldTimes) {

                        boolean isSuccees = FileUtil.copyFile(file, internalcivFile);
                        if (!isSuccees) {
                            return false;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }

        File tilesFile = new File(internalMapGISPath + "/" + tiles);
        if (!tilesFile.exists()) {
            tilesFile.mkdirs();
        }

        return true;
    }

    public static void clearTileCache() {
        try {
            List<String> cachePaths = getTileDbCachePaths();

            for (String path : cachePaths) {
                new File(path).delete();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*获取瓦片缓存文件名称*/
    private static List<String> getTileDbCachePaths() {
        List<String> cacheList = new ArrayList<>();

        if (!new File(newMapGISPath + "/Tiles/").exists()) {
            return cacheList;
        }

        List<MapLayerConfig> layers = MobileConfig.MapConfigInstance.Layers;

        if (layers == null || layers.size() == 0)
            return cacheList;

        for (MapLayerConfig config : layers) {
            if (!config.Type.equals(MapConfig.MOBILE_TILED) || TextUtils.isEmpty(config.cachePath)
                    || !new File(config.cachePath).exists()) {
                continue;
            }

            String path = config.cachePath;

            if (path.endsWith(".db")) {
                String journalPath = path.replace(".db", ".db-journal");

                if (new File(journalPath).exists())
                    cacheList.add(journalPath);
            }

            cacheList.add(path);
        }

        return cacheList;
    }

    /**
     * 在线瓦片的缓存大小
     * 忽略MapGIS/tiles下面的瓦片缓存文件
     */
    public static long getTileCacheSize() {
        long size = 0L;

        try {
            List<String> cachePaths = getTileDbCachePaths();

            for (String path : cachePaths) {
                size += new File(path).length();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return size;
    }

    public static long getMediaCacheSize() {
        String mediaPath = getFixedPath(GlobalPath.Media, true);
        String recordPath = getFixedPath(GlobalPath.Record, true);
        long cacheSize = 0L;
        File mediaFile = new File(mediaPath);
        File recordFile = new File(recordPath);

        if (mediaFile.exists()) {
            cacheSize += mediaFile.length();
        }
        if (recordFile.exists()) {
            cacheSize += recordFile.length();
        }
        return cacheSize;
    }

    public static String getFixedMapGISPath(boolean containSuffix) {
        if (!TextUtils.isEmpty(newMapGISPath)) {

            return containSuffix ? newMapGISPath + "/" : newMapGISPath;
        }
        return containSuffix ? internalMapGISPath + "/" : internalMapGISPath;
    }

    public static String getFixedPath(GlobalPath globalPath) {
        return getFixedPath(globalPath.path);
    }

    public static String getFixedPath(GlobalPath globalPath, boolean containSuffix) {
        return getFixedPath(globalPath.path, containSuffix);
    }

    /**
     * 获取修正后的多媒体路径
     *
     * @param name 名称
     * @return 路径
     */
    public static String getFixedPath(String name) {
        return getFixedPath(name, true);
    }

    public static String getFixedPath(String name, boolean containSuffix) {
        if (!TextUtils.isEmpty(newMapGISPath)) {
            String newMapGISPathTemp = newMapGISPath + "/" + name;
            return containSuffix ? newMapGISPathTemp + "/" : newMapGISPathTemp;
        }

        String internalMapGISPathTemp = internalMapGISPath + "/" + name;
        return containSuffix ? internalMapGISPathTemp + "/" : internalMapGISPathTemp;
    }

    private static String findNewMapGISFilePath() {
        String relativePath = "/" + newMapGISName + "/";
        return findNewFilePath(relativePath);
    }

//    private static String findNewMapFilePath() {
//        String relativePath = "/" + newMapGISName + "/" + "Map/";
//        return findNewFilePath(relativePath);
//    }

    private static String findNewFilePath(String relativePath) {

        File innerDir = Environment.getExternalStorageDirectory();

        String newMapGISPath = innerDir.getAbsolutePath() + relativePath;
        if (new File(newMapGISPath).exists()) {
            return newMapGISPath;
        }

        File rootDir = innerDir.getParentFile();

        File storageDir = rootDir.getParentFile();

        if (storageDir.canRead()) {
            File[] storageFiles = storageDir.listFiles();
            for (File file : storageFiles) {
                String absolutePath = file.getAbsolutePath() + relativePath;

                if (new File(absolutePath).exists()) {
                    return absolutePath;
                }
            }
        }


        if (rootDir.canRead()) {
            File[] files = rootDir.listFiles();
            for (File file : files) {
                String absolutePath = file.getAbsolutePath() + relativePath;

                if (new File(absolutePath).exists()) {

                    return absolutePath;
                }
            }
        }


        return "";
    }

}

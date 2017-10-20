package com.mapgis.mmt.module.gis.toolbar.accident2.util;

import com.mapgis.mmt.common.util.Battle360Util;

import java.io.File;
import java.io.IOException;

/**
 * Created by Comclay on 2017/5/23.
 */

public class BrokenUtil {
    private final static String MODULE_NAME = "爆管分析";

    // 现场截图的路径
    public static File getShotFile() throws IOException {
        String path = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Temp, true)
                + MODULE_NAME + File.separator;
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        path += System.currentTimeMillis() + ".png";
        File file = new File(path);
        file.createNewFile();
        return file;
    }
}

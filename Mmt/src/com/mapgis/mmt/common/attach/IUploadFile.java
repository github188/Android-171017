package com.mapgis.mmt.common.attach;

/**
 * Created by Comclay on 2016/12/19.
 * 上传文件的接口
 */

public interface IUploadFile {
    /**
     * 上传文件时存在数据库中的值
     * @return  string with ',' separator
     */
    String getDatabaseValue();

    /**
     * 上传文件的本地绝对路径
     * @return  string with ',' separator
     */
    String getLocalAbsolutePaths();

    /**
     * 存储在服务器上的相对路径
     * @return  string with ',' separator
     */
    String getServerRelativePaths();

    String ARG_FLAG = "flag";
    String ARG_RELATIVE_PATH_SEGMENT = "relative_path_segment";
    String ARG_FILE_PATHS = "file_paths";
    String ARG_ADD_ENABLE = "add_enable";
    String ARG_SELECT_ENABLE = "select_enable";
    String ARG_MAX_VALUE_LENGTH = "max_value_length";
}

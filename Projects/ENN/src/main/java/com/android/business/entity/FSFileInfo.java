package com.android.business.entity;

/**
 * fs文件实体类
 */
public class FSFileInfo extends DataInfo {
    private String domid;
    private String filename;
    private String guid;
    private String mainid;
    private String subid;
    private String modulename;
    private String uploaderdomainid;
    private String uploader;
    private String retGuid;

    public FSFileInfo(String domid, String filename, String guid) {
        this.domid = domid;
        this.filename = filename;
        this.guid = guid;
    }

    public FSFileInfo() {
    }

    public String getDomid() {
        return domid;
    }

    public void setDomid(String domid) {
        this.domid = domid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getMainid() {
        return mainid;
    }

    public void setMainid(String mainid) {
        this.mainid = mainid;
    }

    public String getSubid() {
        return subid;
    }

    public void setSubid(String subid) {
        this.subid = subid;
    }

    public String getModulename() {
        return modulename;
    }

    public void setModulename(String modulename) {
        this.modulename = modulename;
    }

    public String getRetGuid() {
        return retGuid;
    }

    public void setRetGuid(String retGuid) {
        this.retGuid = retGuid;
    }

    public String getUploaderdomainid() {
        return uploaderdomainid;
    }

    public void setUploaderdomainid(String uploaderdomainid) {
        this.uploaderdomainid = uploaderdomainid;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }
}

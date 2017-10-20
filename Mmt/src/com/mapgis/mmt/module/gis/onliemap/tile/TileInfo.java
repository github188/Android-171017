package com.mapgis.mmt.module.gis.onliemap.tile;

import com.mapgis.mmt.module.gis.onliemap.SpatialReference;
import com.zondy.mapgis.geometry.Dot;

import java.util.List;

public class TileInfo {
    public int rows;
    public int cols;
    public double dpi;
    String format;
    double compressionQuality;
    public Dot origin;
    SpatialReference spatialReference;
    public List<Lod> lods;

    @Override
    public String toString() {
        return "TileInfo [rows=" + this.rows + ", cols=" + this.cols + ", compressionQuality=" + this.compressionQuality + ", dpi="
                + this.dpi + ", format=" + this.format + ", lods=" + this.lods + ", origin=" + this.origin + ", rows=" + this.rows
                + ", spatialReference=" + this.spatialReference + "]";
    }

    @Override
    public int hashCode() {
        int j = 1;
        j = 31 * j + this.cols;

        long l = Double.doubleToLongBits(this.compressionQuality);
        j = 31 * j + (int) (l ^ l >>> 32);
        l = Double.doubleToLongBits(this.dpi);
        j = 31 * j + (int) (l ^ l >>> 32);
        j = 31 * j + (this.format == null ? 0 : this.format.hashCode());
        j = 31 * j + (this.lods == null ? 0 : this.lods.hashCode());
        j = 31 * j + (this.origin == null ? 0 : this.origin.hashCode());
        j = 31 * j + this.rows;
        j = 31 * j + (this.spatialReference == null ? 0 : this.spatialReference.hashCode());
        return j;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TileInfo tileInfo = (TileInfo) obj;
        if (this.cols != tileInfo.cols) {
            return false;
        }
        if (Double.doubleToLongBits(this.compressionQuality) != Double.doubleToLongBits(tileInfo.compressionQuality)) {
            return false;
        }
        if (Double.doubleToLongBits(this.dpi) != Double.doubleToLongBits(tileInfo.dpi)) {
            return false;
        }
        if (this.format == null) {
            if (tileInfo.format != null) {
                return false;
            }
        } else if (!this.format.equals(tileInfo.format)) {
            return false;
        }
        if (this.lods == null) {
            if (tileInfo.lods != null) {
                return false;
            }
        } else if (!this.lods.equals(tileInfo.lods)) {
            return false;
        }
        if (this.origin == null) {
            if (tileInfo.origin != null) {
                return false;
            }
        } else if (!this.origin.equals(tileInfo.origin)) {
            return false;
        }
        if (this.rows != tileInfo.rows) {
            return false;
        }
        if (this.spatialReference == null) {
            if (tileInfo.spatialReference != null) {
                return false;
            }
        } else if (!this.spatialReference.equals(tileInfo.spatialReference)) {
            return false;
        }
        return true;
    }
}

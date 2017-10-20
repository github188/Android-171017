package com.mapgis.mmt.module.gis.onliemap;

import java.io.Serializable;

/***
 * 空间坐标系类
 * 
 * @author LiuMing
 * 
 */
public class SpatialReference implements Serializable {
	private static final long serialVersionUID = 1L;
	private int wkid;
	private String wkt;

	public int getWkid() {
		return wkid;
	}

	public void setWkid(int wkid) {
		this.wkid = wkid;
	}

	public double getXOrigin() {
		return XOrigin;
	}

	public void setXOrigin(double xOrigin) {
		XOrigin = xOrigin;
	}

	public double getYOrigin() {
		return YOrigin;
	}

	public void setYOrigin(double yOrigin) {
		YOrigin = yOrigin;
	}

	public double getXYScale() {
		return XYScale;
	}

	public void setXYScale(double xYScale) {
		XYScale = xYScale;
	}

	public double getZOrigin() {
		return ZOrigin;
	}

	public void setZOrigin(double zOrigin) {
		ZOrigin = zOrigin;
	}

	public double getZScale() {
		return ZScale;
	}

	public void setZScale(double zScale) {
		ZScale = zScale;
	}

	public double getMOrigin() {
		return MOrigin;
	}

	public void setMOrigin(double mOrigin) {
		MOrigin = mOrigin;
	}

	public double getMScale() {
		return MScale;
	}

	public void setMScale(double mScale) {
		MScale = mScale;
	}

	public double getXYTolerance() {
		return XYTolerance;
	}

	public void setXYTolerance(double xYTolerance) {
		XYTolerance = xYTolerance;
	}

	public double getZTolerance() {
		return ZTolerance;
	}

	public void setZTolerance(double zTolerance) {
		ZTolerance = zTolerance;
	}

	public double getMTolerance() {
		return MTolerance;
	}

	public void setMTolerance(double mTolerance) {
		MTolerance = mTolerance;
	}

	public boolean isHighPrecision() {
		return HighPrecision;
	}

	public void setHighPrecision(boolean highPrecision) {
		HighPrecision = highPrecision;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	private double XOrigin;
	private double YOrigin;
	private double XYScale;
	private double ZOrigin;
	private double ZScale;
	private double MOrigin;
	private double MScale;
	private double XYTolerance;
	private double ZTolerance;
	private double MTolerance;
	private boolean HighPrecision;

	public String getWkt() {
		return wkt;
	}

	public void setWkt(String wkt) {
		this.wkt = wkt;
	}

	public SpatialReference(int wkid) {
		this.wkid = wkid;
	}

	public int getID() {
		return this.wkid;
	}

	public enum Type {
	}
}
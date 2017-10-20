package com.mapgis.mmt.module.gis.onliemap.tile;

public class Lod {
	public void setLevel(int level) {
		this.level = level;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	private int level;
	private double resolution;
	private double scale;

	public Lod() {
	}

	public Lod(int level, double resolution) {
		this.level = level;
		this.resolution = resolution;
		this.scale = 0;
	}

	public int getLevel() {
		return this.level;
	}

	public double getResolution() {
		return this.resolution;
	}

	public double getScale() {
		return this.scale;
	}

	@Override
	public String toString() {
		return "LOD [level=" + this.level + ", resolution=" + this.resolution + ", scale=" + this.scale + "]";
	}

	@Override
	public int hashCode() {
		int i = 31;
		int j = 1;
		j = i * j + this.level;

		long bitOpr = Double.doubleToLongBits(this.resolution);
		j = i * j + (int) (bitOpr ^ bitOpr >>> 32);
		bitOpr = Double.doubleToLongBits(this.scale);
		j = i * j + (int) (bitOpr ^ bitOpr >>> 32);
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
		Lod lod = (Lod) obj;
		if (this.level != lod.level) {
			return false;
		}
		if (Double.doubleToLongBits(this.resolution) != Double.doubleToLongBits(lod.resolution)) {
			return false;
		}
		return Double.doubleToLongBits(this.scale) == Double.doubleToLongBits(lod.scale);
	}
}

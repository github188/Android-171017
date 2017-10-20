package com.mapgis.mmt.module.gis.toolbar.analyzer;

public class LocatorGeocodeResult {
	public static LocatorGeocodeResult geoResult;

	public Candidate[] candidates;

	class Candidate {
		public String address;

		public Location location;

		public int score;

		public Attributes[] attributes;

		public Geometries geometries;

		@Override
		public String toString() {
			return address;
		}
	}

	class Location {
		public double x;
		public double y;
	}

	class Attributes {
		public String Key;
		public String Value;
	}

	class Geometries {
		public SpatialReference spatialReference;
		public double[][][] paths;
	}

	class SpatialReference {
		public int wkid;
	}

}

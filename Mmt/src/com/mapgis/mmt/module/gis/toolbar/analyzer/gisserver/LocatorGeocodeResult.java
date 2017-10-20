package com.mapgis.mmt.module.gis.toolbar.analyzer.gisserver;

import com.zondy.mapgis.geometry.Dot;

public class LocatorGeocodeResult {

    public Candidate[] candidates;

    public class Candidate {
        public String address;

        public Location location;

        public int score;

        public Attributes[] attributes;

        public Geometries geometries;

        @Override
        public String toString() {
            return address;
        }

        public Dot toDot() {
            return new Dot(location.x
                    , location.y);
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

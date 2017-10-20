package com.mapgis.mmt.module.gps.entity;

import android.location.LocationManager;

/**
 * Created by Comclay on 2017/2/27.
 * 封装卫星信息的实体类
 */

public class SatelliteBean {
    /* These package private values are modified by the GpsStatus class */
    boolean mValid;
    boolean mHasEphemeris;
    boolean mHasAlmanac;
    boolean mUsedInFix;
    int mPrn;
    float mSnr;
    float mElevation;
    float mAzimuth;

    public SatelliteBean(int prn) {
        mPrn = prn;
    }

    /**
     * Used by {@link LocationManager#getGpsStatus} to copy LocationManager's
     * cached GpsStatus instance to the client's copy.
     */
    void setStatus(SatelliteBean satellite) {
        if (satellite == null) {
            mValid = false;
        } else {
            mValid = satellite.mValid;
            mHasEphemeris = satellite.mHasEphemeris;
            mHasAlmanac = satellite.mHasAlmanac;
            mUsedInFix = satellite.mUsedInFix;
            mSnr = satellite.mSnr;
            mElevation = satellite.mElevation;
            mAzimuth = satellite.mAzimuth;
        }
    }

    /**
     * Returns the PRN (pseudo-random number) for the satellite.
     *
     * @return PRN number
     */
    public int getPrn() {
        return mPrn;
    }

    /**
     * Returns the signal to noise ratio for the satellite.
     *
     * @return the signal to noise ratio
     */
    public float getSnr() {
        return mSnr;
    }

    /**
     * Returns the elevation of the satellite in degrees.
     * The elevation can vary between 0 and 90.
     *
     * @return the elevation in degrees
     */
    public float getElevation() {
        return mElevation;
    }

    /**
     * Returns the azimuth of the satellite in degrees.
     * The azimuth can vary between 0 and 360.
     *
     * @return the azimuth in degrees
     */
    public float getAzimuth() {
        return mAzimuth;
    }

    /**
     * Returns true if the GPS engine has ephemeris data for the satellite.
     *
     * @return true if the satellite has ephemeris data
     */
    public boolean hasEphemeris() {
        return mHasEphemeris;
    }

    /**
     * Returns true if the GPS engine has almanac data for the satellite.
     *
     * @return true if the satellite has almanac data
     */
    public boolean hasAlmanac() {
        return mHasAlmanac;
    }

    /**
     * Returns true if the satellite was used by the GPS engine when
     * calculating the most recent GPS fix.
     *
     * @return true if the satellite was used to compute the most recent fix.
     */
    public boolean usedInFix() {
        return mUsedInFix;
    }

    public void setSnr(float mSnr) {
        this.mSnr = mSnr;
    }
}

package com.mapgis.mmt.module.gps.entity;

import android.util.SparseArray;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by Comclay on 2017/2/27.
 *
 */

public class GpsStatusBean {
    private static final int NUM_SATELLITES = 255;
    private final SparseArray<SatelliteBean> mSatellites = new SparseArray<>();

    private final class SatelliteIterator implements Iterator<SatelliteBean> {

        private final SparseArray<SatelliteBean> mSatellites;
        private final int mSatellitesCount;

        private int mIndex = 0;

        SatelliteIterator(SparseArray<SatelliteBean> satellites) {
            mSatellites = satellites;
            mSatellitesCount = satellites.size();
        }

        public boolean hasNext() {
            for (; mIndex < mSatellitesCount; ++mIndex) {
                SatelliteBean satellite = mSatellites.valueAt(mIndex);
                if (satellite.mValid) {
                    return true;
                }
            }
            return false;
        }

        public SatelliteBean next() {
            while (mIndex < mSatellitesCount) {
                SatelliteBean satellite = mSatellites.valueAt(mIndex);
                ++mIndex;
                if (satellite.mValid) {
                    return satellite;
                }
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private Iterable<SatelliteBean> mSatelliteList = new Iterable<SatelliteBean>() {
        public Iterator<SatelliteBean> iterator() {
            return new SatelliteIterator(mSatellites);
        }
    };

    /**
     * Used for receiving notifications when GPS status has changed.
     */
    public interface Listener {
        void onGpsStatusBeanChanged(int event);
    }

    public interface NmeaListener {
        void onNmeaReceived(long timestamp, String nmea);
    }

    synchronized void setStatus(int svCount, int[] prns, float[] snrs,
                                float[] elevations, float[] azimuths, int ephemerisMask,
                                int almanacMask, int usedInFixMask) {
        clearSatellites();
        for (int i = 0; i < svCount; i++) {
            int prn = prns[i];
            int prnShift = (1 << (prn - 1));
            if (prn > 0 && prn <= NUM_SATELLITES) {
                SatelliteBean satellite = mSatellites.get(prn);
                if (satellite == null) {
                    satellite = new SatelliteBean(prn);
                    mSatellites.put(prn, satellite);
                }

                satellite.mValid = true;
                satellite.mSnr = snrs[i];
                satellite.mElevation = elevations[i];
                satellite.mAzimuth = azimuths[i];
                satellite.mHasEphemeris = ((ephemerisMask & prnShift) != 0);
                satellite.mHasAlmanac = ((almanacMask & prnShift) != 0);
                satellite.mUsedInFix = ((usedInFixMask & prnShift) != 0);
            }
        }
    }

    void setStatus(GpsStatusBean status) {
        clearSatellites();

        SparseArray<SatelliteBean> otherSatellites = status.mSatellites;
        int otherSatellitesCount = otherSatellites.size();
        int satelliteIndex = 0;
        // merge both sparse arrays, note that we have already invalidated the elements in the
        // receiver array
        for (int i = 0; i < otherSatellitesCount; ++i) {
            SatelliteBean otherSatellite = otherSatellites.valueAt(i);
            int otherSatellitePrn = otherSatellite.getPrn();

            int satellitesCount = mSatellites.size();
            while (satelliteIndex < satellitesCount
                    && mSatellites.valueAt(satelliteIndex).getPrn() < otherSatellitePrn) {
                ++satelliteIndex;
            }

            if (satelliteIndex < mSatellites.size()) {
                SatelliteBean satellite = mSatellites.valueAt(satelliteIndex);
                if (satellite.getPrn() == otherSatellitePrn) {
                    satellite.setStatus(otherSatellite);
                } else {
                    satellite = new SatelliteBean(otherSatellitePrn);
                    satellite.setStatus(otherSatellite);
                    mSatellites.put(otherSatellitePrn, satellite);
                }
            } else {
                SatelliteBean satellite = new SatelliteBean(otherSatellitePrn);
                satellite.setStatus(otherSatellite);
                mSatellites.append(otherSatellitePrn, satellite);
            }
        }
    }

    public Iterable<SatelliteBean> getSatellites() {
        return mSatelliteList;
    }

    private void clearSatellites() {
        int satellitesCount = mSatellites.size();
        for (int i = 0; i < satellitesCount; i++) {
            SatelliteBean satellite = mSatellites.valueAt(i);
            satellite.mValid = false;
        }
    }
}

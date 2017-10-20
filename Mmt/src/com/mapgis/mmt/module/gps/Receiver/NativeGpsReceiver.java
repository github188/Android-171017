package com.mapgis.mmt.module.gps.Receiver;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;

/**
 * 设备本身GPS定位
 */
public class NativeGpsReceiver extends GpsReceiver {
    @Override
    public String start(CoordinateConvertor coordinateConvertor) {
        this.coordinateConvertor = coordinateConvertor;
        String result = "";

        LocationManager locationManager = (LocationManager) MyApplication.getInstance().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            result += "请在'位置和安全设置'中开启GPS卫星定位;";
        }

        // if
        // (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        // {
        // result += "请在'位置和安全设置'中开启无线网络定位;";
        // }

        Location lastNetworkLocation = null;

        if (MyApplication.getInstance().getConfigValue("OpenNetworkGPS").equals("1")) {
            lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, new MyLocationListner());
            }
        }

        Location lastGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (isBetterLocation(lastGpsLocation, lastNetworkLocation)) {
            lastLocation = lastGpsLocation;
        } else {
            lastLocation = lastNetworkLocation;
        }

        // if (locationManager.getProvider(LocationManager.GPS_PROVIDER) !=
        // null) {
        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        // 1000, 0, new MyLocationListner());
        // }
        //
        // if (locationManager.getProvider("spoof") != null) {
        // locationManager.requestLocationUpdates("spoof", 888, 0, new
        // MyLocationListner());
        // }

        for (String provider : locationManager.getAllProviders()) {
            if (provider.equals(LocationManager.NETWORK_PROVIDER) || provider.equals(LocationManager.GPS_PROVIDER)) {
                continue;
            }

            locationManager.requestLocationUpdates(provider, 1000, 1, new MyLocationListner());
        }

        return result;
    }

    public void stop() {
    }

    private static final int CHECK_INTERVAL = 1000 * 30;

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        if (location == null) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > CHECK_INTERVAL;
        boolean isSignificantlyOlder = timeDelta < -CHECK_INTERVAL;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location,
        // use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must
            // be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private class MyLocationListner implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            try {
                if (lastLocation == null || isBetterLocation(location, lastLocation)) {
                    //Get the speed if it is available, in meters/second over ground.
                    location.setSpeed((float) (location.getSpeed() * 3.6));

                    setLastLocation(location);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // 后3个方法此处不做处理
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

}

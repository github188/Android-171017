// IGisServer.aidl
package com.aidl;

interface IGisServer {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    String getMapName();

    void setMapName(String mapName);

    String getEntireExtent(String mapName);

    byte[] getVectorImage(String mapName,long imgWidth, long imgHeight,
            double dispRectXmin, double dispRectYmin, double dispRectXmax,double dispRectYmax,
            String strShowLayers, int imageType);
}

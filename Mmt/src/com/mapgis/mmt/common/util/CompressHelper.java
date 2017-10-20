package com.mapgis.mmt.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * util for compress/decompress data
 *
 * @author zoro
 * @version 1.0
 * @created 2013-02-07 10:14 AM
 */
public final class CompressHelper {

    //压缩端
    public static byte[] compress2Bytes(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // 此处的第二个参数必须要设置成true，这样才能成功与服务器端对应压缩与解压缩（服务器端对应的是php的解压缩）
            // 具体的原因是参照网上说的一个协议。参照地址以后会补发上来
            Deflater def = new Deflater(Deflater.BEST_COMPRESSION, true);
            DeflaterOutputStream dos = new DeflaterOutputStream(bos, def);

            byte[] buf = new byte[1024];

            int readCount;
            while ((readCount = bis.read(buf, 0, buf.length)) > 0) {
                dos.write(buf, 0, readCount);
            }

            dos.close();
            bos.close();
            bis.close();

            return bos.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }

    //解压缩
    public static byte[] decompress2Bytes(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // 相对应的解压缩的参数也要设置未true
            Inflater inf = new Inflater(true);
            InflaterInputStream iis = new InflaterInputStream(bis, inf);

            int readCount;
            byte[] buf = new byte[1024];

            while ((readCount = iis.read(buf, 0, buf.length)) > 0) {
                bos.write(buf, 0, readCount);
            }

            iis.close();
            bis.close();
            bos.close();

            return bos.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decompressByDeflate(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // 相对应的解压缩的参数也要设置未true
            Inflater inf = new Inflater(true);
            InflaterInputStream iis = new InflaterInputStream(bis, inf);

            int readCount;
            byte[] buf = new byte[1024];

            while ((readCount = iis.read(buf, 0, buf.length)) > 0) {
                bos.write(buf, 0, readCount);
            }

            iis.close();
            bis.close();
            bos.close();

            return bos.toString("utf-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "";
    }

    public static String decompressByGZip(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            GZIPInputStream iis = new GZIPInputStream(bis);

            int readCount;
            byte[] buf = new byte[1024];

            while ((readCount = iis.read(buf, 0, buf.length)) > 0) {
                bos.write(buf, 0, readCount);
            }

            iis.close();
            bis.close();
            bos.close();

            return bos.toString("utf-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "";
    }
}  
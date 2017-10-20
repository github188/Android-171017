package com.mapgis.mmt.net;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestJsonParse extends HttpRequest {

    private static final long serialVersionUID = 1L;

    static final JsonFactory jsonFactory = new JsonFactory();

    public static final String executeFromMapToString(String url, Map<String, String> paramters) throws Exception {

        String str = paramters != null ? paramters.get("$ReturnResultString$") : null;

        if (str == null) {

            ArrayList<String> paramList = new ArrayList<>();
            boolean normal = mapToList(url, addFJson(paramters), paramList);

            if (normal) {
                str = executeHttpGet(url, paramList.toArray(new String[paramList.size()]));
            } else {
                str = executeFormHttpPost(url, paramList);
            }

            if (str == null) {
                str = "";
            }

            if (str.startsWith("\"") && str.endsWith("\"") && str.length() > 1 && (str.charAt(1) == '[' || str.charAt(1) == '{')) {
                str = str.substring(1, str.length() - 1);
            }

            str = str.replace("\\", "");

            if (paramters != null) {
                paramters.put("$ReturnResultString$", str);
            }
        }

        return str;
    }

    public static final JsonParser executeFromMap(String url, Map<String, String> paramters) throws Exception {

        String str = paramters != null ? paramters.get("$ReturnResultString$") : null;

        if (str == null) {

            ArrayList<String> paramList = new ArrayList<>();
            boolean normal = mapToList(url, addFJson(paramters), paramList);

            if (normal) {
                str = executeHttpGet(url, paramList.toArray(new String[paramList.size()]));
            } else {
                str = executeFormHttpPost(url, paramList);
            }

            if (str == null) {
                str = "";
            }

            if (str.startsWith("\"") && str.endsWith("\"") && str.length() > 1 && (str.charAt(1) == '[' || str.charAt(1) == '{')) {
                str = str.substring(1, str.length() - 1);
            }

            str = str.replace("\\", "");

            if (paramters != null) {
                paramters.put("$ReturnResultString$", str);
            }
        }

        JsonParser jsonParser = jsonFactory.createJsonParser(str);
        jsonParser.nextToken();

        if ((str.length() > 14) && (str.substring(0, 15).indexOf("\"error\"") > -1)) {
            // throw MapGISServiceException.fromJson(localJsonParser);
        }

        return jsonParser;
    }

    public static final JsonParser fromFile(String url, File file, String fileHeader) throws Exception {
        byte[] arrayOfByte1 = "---------------------------".getBytes();
        byte[] currentTimeByteArray = Long.toString(System.currentTimeMillis()).getBytes();
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();

            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + new String(arrayOfByte1)
                    + new String(currentTimeByteArray));

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(45);
            byteArrayOutputStream.write(45);
            byteArrayOutputStream.write(arrayOfByte1);
            byteArrayOutputStream.write(currentTimeByteArray);
            byteArrayOutputStream.write(13);
            byteArrayOutputStream.write(10);

            byteArrayOutputStream.write("Content-Disposition: form-data; name=\"attachment\"; filename=\"".getBytes());
            byteArrayOutputStream.write(file.getName().getBytes());
            byteArrayOutputStream.write("\"\r\n".getBytes());
            byteArrayOutputStream.write("Content-Type: ".getBytes());
            byteArrayOutputStream.write(fileHeader == null ? "Content-Type: application/octet-stream".getBytes() : fileHeader.getBytes());
            byteArrayOutputStream.write(13);
            byteArrayOutputStream.write(10);

            byteArrayOutputStream.write(13);
            byteArrayOutputStream.write(10);
            byteArrayOutputStream.write(fileToByte(file));
            byteArrayOutputStream.write(13);
            byteArrayOutputStream.write(10);

            byteArrayOutputStream.write(45);
            byteArrayOutputStream.write(45);
            byteArrayOutputStream.write(arrayOfByte1);
            byteArrayOutputStream.write(currentTimeByteArray);
            byteArrayOutputStream.write(13);
            byteArrayOutputStream.write(10);

            byteArrayOutputStream.write("Content-Disposition: form-data; name=\"f\"\r\n".getBytes());
            byteArrayOutputStream.write("\r\njson\r\n".getBytes());

            byteArrayOutputStream.write(45);
            byteArrayOutputStream.write(45);
            byteArrayOutputStream.write(arrayOfByte1);
            byteArrayOutputStream.write(currentTimeByteArray);
            byteArrayOutputStream.write(45);
            byteArrayOutputStream.write(45);

            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            byte[] arrayOfByte3 = byteArrayOutputStream.toByteArray();
            httpURLConnection.setFixedLengthStreamingMode(arrayOfByte3.length);
            OutputStream localOutputStream = httpURLConnection.getOutputStream();
            localOutputStream.write(arrayOfByte3);
            localOutputStream.flush();
            localOutputStream.close();

            int i = httpURLConnection.getResponseCode();
            if (i != 200) {
                throw new Exception("Received the response code " + i + " from the URL " + url);
            }

            InputStream inputStream = httpURLConnection.getInputStream();

            JsonParser jsonParser = jsonFactory.createJsonParser(inputStream);
            jsonParser.nextToken();

            // JsonParser localJsonParser2 = jsonParser;
            return jsonParser;
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    private static final byte[] fileToByte(File paramFile) throws Exception {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(paramFile);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] arrayOfByte1 = new byte[1024];
            int i;
            while ((i = fileInputStream.read(arrayOfByte1)) != -1) {
                byteArrayOutputStream.write(arrayOfByte1, 0, i);
            }
            byteArrayOutputStream.close();
            byte[] fileByte = byteArrayOutputStream.toByteArray();
            return fileByte;
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    public static JsonFactory getJsonFactory() {
        return jsonFactory;
    }

    private static final Map<String, String> addFJson(Map<String, String> paramMap) {
        if (paramMap == null) {
            paramMap = new HashMap<String, String>();
        }
        if (!paramMap.containsKey("f")) {
            paramMap.put("f", "json");
        }
        return paramMap;
    }
}

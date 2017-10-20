package com.mapgis.mmt.module.bluetooth;

import android.util.Log;

import java.math.BigInteger;
import java.text.DecimalFormat;

/**
 * 16进制值与 String/Byte之间的转换
 *
 * @author liugs
 * @email liuguangshuai@gmail.com
 * @data 2011-10-16
 */
public class CHexConver {

    public static String unit = "PPM";
    public static boolean isUnit = false;

    public static String getUnit(String code) {
        int i = Integer.parseInt(code);
        switch (i) {
            case 0:
                unit = "暂无";
                break;
            case 1:
                unit = "μmol/mol";
                break;
            case 2:
                unit = "mg/m3";
                break;
            case 4:
                unit = "PPM";
                break;
            case 8:
                unit = "KPA";
                break;
            case 16:
                unit = "%LEL";
                break;
            case 32:
                unit = "VOL";
                break;
            case 64:
                unit = "MPA";
                break;
            default:
                break;
        }
        isUnit = true;
        return unit;
    }

    /**
     * 计算校验和后截取后2位
     *
     * @param array
     * @return
     */
    public static String GetAndBytes(byte[] array) {
        int crc = 0x0000;
        for (int i = 0; i < array.length; i++) {
            crc += (array[i] & 0xff);
        }
        String tempstr = Integer.toHexString(crc).toUpperCase();
        //System.out.println(tempstr);
        //System.out.println(crc);
        if (tempstr.length() >= 3) {
            return tempstr.substring(1, 3);
        } else {
            return tempstr;
        }

    }

    /**
     * 把字符串去空格后转换成byte数组。如"37 5a"转成[0x37][0x5A]
     *
     * @param h
     * @return
     */
    public static byte[] hex2byte(String h) {

        h = h.replaceAll(" ", "");
        byte[] ret = new byte[h.length() / 2];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = Integer.decode("#" + h.substring(2 * i, 2 * i + 2)).byteValue();
        }
        return ret;
    }

    /**
     * 把字符串去空格后转换成byte数组。如"37 5a"转成[0x37][0x5A]
     *
     * @param s
     * @return
     */
    public static byte[] string2bytes(String s) {
        String ss = s.replace(" ", "");
        int string_len = ss.length();
        int len = string_len / 2;
        if ((string_len + 1) % 2 == 1) {
            ss = "0" + ss;
            string_len++;
            len++;
        }
        byte[] a = new byte[len];
        int cur = 0;
        for (int i = 0; i < len; i++) {
            cur = Integer.parseInt(ss.substring(2 * i, 2 * i + 2), 16);

            a[i] = (byte) cur;
        }
        return a;
    }


    /**
     * 16进制数组转化成字符串(大写字母)，比如[0x03][0x3f]转化成"33F"
     *
     * @param b
     * @return
     */
    public static String hex2HexString(byte[] b) {
        int len = b.length;
        int[] x = new int[len];
        String[] y = new String[len];
        StringBuilder str = new StringBuilder();
        // 转换成Int数组,然后转换成String数组
        for (int j = 0; j < len; j++) {
            x[j] = b[j] & 0xff;
            y[j] = Integer.toHexString(x[j]);
            while (y[j].length() < 2) {
                y[j] = "0" + y[j];
            }
            str.append(y[j]);
        }
        //如果是以"0"开头，则弃掉"0"
        while (str.indexOf("0") == 0) {
            str = str.delete(0, 1);
        }
        return new String(str).toUpperCase();//toUpperCase()方法  转化成大写
    }

    /**
     * 16进制数组转化成调试用字符串(大写字母)，比如[0x03][0x3f]转化成"03 3F"
     *
     * @param b
     * @return
     */
    public static String hex2DebugHexString(byte[] b) {
        int len = b.length;
        int[] x = new int[len];
        String[] y = new String[len];
        StringBuilder str = new StringBuilder();
        // 转换成Int数组,然后转换成String数组
        int j = 0;
        for (; j < len; j++) {
            x[j] = b[j] & 0xff;
            y[j] = Integer.toHexString(x[j]);
            while (y[j].length() < 2) {
                y[j] = "0" + y[j];
            }
            str.append(y[j]);
            str.append(" ");
        }
        return new String(str).toUpperCase();//toUpperCase()方法  转化成大写
    }

    /**
     * 字符串转换成十六进制字符串
     *
     * @param str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     *
     * @param hexStr Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * String的字符串转换成unicode的String
     *
     * @param strText 全角字符串
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String strToUnicode(String strText)
            throws Exception {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++) {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else // 低位在前面补00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * unicode的String转换成String的字符串
     *
     * @param hex 16进制值字符串 （一个unicode为2byte）
     * @return String 全角字符串
     */
    public static String unicodeToString(String hex) {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++) {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转
            String s2 = s.substring(4);
            // 将16进制的string转为int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }

    /**
     * 计算校验和<br>
     * <p>对“消息头＋会话头＋事务头＋操作信息”按32位异或，对异或结果取反后的值为校验和。
     *
     * @param msg
     * @return
     */
    private String calcCheckSum(String msg) {
        byte[] arr = msg.getBytes();
        byte[] res = new byte[4];

        for (int i = 0; i < arr.length; i += 4) {
            res[0] ^= arr[i];
            res[1] ^= arr[i + 1];
            res[2] ^= arr[i + 2];
            res[3] ^= arr[i + 3];
        }

        res[0] = (byte) ~res[0];
        res[1] = (byte) ~res[1];
        res[2] = (byte) ~res[2];
        res[3] = (byte) ~res[3];

        String resStr = "";

        for (int i = 0; i < 4; i++) {
            resStr = resStr + byte2hex(res[i]);
        }

        return resStr;
    }

    /**
     * 将单字节转成16进制<br>
     *
     * @param b
     * @return
     */
    private String byte2hex(byte b) {
        StringBuffer buf = new StringBuffer();
        char[] hexChars = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);

        return buf.toString();
    }


    private static String hexStr = "0123456789ABCDEF";
    private static String[] binaryArray =
            {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111",
                    "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};

    /**
     * @param bArray
     * @return 转换为二进制字符串
     */
    public static String bytes2BinaryStr(byte[] bArray) {

        String outStr = "";
        int pos = 0;
        for (byte b : bArray) {
            //高四位
            pos = (b & 0xF0) >> 4;
            outStr += binaryArray[pos];
            //低四位
            pos = b & 0x0F;
            outStr += binaryArray[pos];
        }
        return outStr;

    }

    /**
     * @param bytes
     * @return 将二进制转换为十六进制字符输出
     */
    public static String BinaryToHexString(byte[] bytes) {

        String result = "";
        String hex = "";
        for (int i = 0; i < bytes.length; i++) {
            //字节高4位
            hex = String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4));
            //字节低4位
            hex += String.valueOf(hexStr.charAt(bytes[i] & 0x0F));
            result += hex + " ";
        }
        return result;
    }

    /**
     * @param hexString
     * @return 将十六进制转换为字节数组
     */
    public static byte[] HexStringToBinary(String hexString) {
        //hexString的长度对2取整，作为bytes的长度
        int len = hexString.length() / 2;
        byte[] bytes = new byte[len];
        byte high = 0;//字节高四位
        byte low = 0;//字节低四位

        for (int i = 0; i < len; i++) {
            //右移四位得到高位
            high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
            low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
            bytes[i] = (byte) (high | low);//高地位做或运算
        }
        return bytes;
    }

    /**
     * 将16进制字符串转换为二进制字符串
     *
     * @param str
     * @return 二进制字符串
     */
    public static String string2BinaryStr(String str) {

        return bytes2BinaryStr(HexStringToBinary(str));

    }

    /**
     * 00000000 协议格式高报+低报+OL+保护+欠压+传感器故障+泵堵+泵状态
     *
     * @param str
     * @return 返回状态
     */
    public static String bxTranStatus(String str) {
        str = string2BinaryStr(str);
        String temp = "正常";
        String[] arr = new String[str.length()];
        for (int i = 0; i < str.length(); i++) {
            switch (i) {
                case 0:
                    if ("1".endsWith(String.valueOf(str.charAt(i)))) {
                        temp = "";
                        arr[i] = "高报";
                    }
                    break;
                case 1:
                    if ("1".endsWith(String.valueOf(str.charAt(i)))) {
                        temp = "";
                        arr[i] = "低报";
                    }
                    break;
                case 2:
                    if ("1".endsWith(String.valueOf(str.charAt(i)))) {
                        temp = "";
                        arr[i] = "超量程";
                    }
                    break;
                case 3:
                    if ("1".endsWith(String.valueOf(str.charAt(i)))) {
                        temp = "";
                        arr[i] = "保护";
                    }
                    break;
                case 4:
                    if ("1".endsWith(String.valueOf(str.charAt(i)))) {
                        temp = "";
                        arr[i] = "欠压";
                    }
                    break;
                case 5:
                    if ("1".endsWith(String.valueOf(str.charAt(i)))) {
                        temp = "";
                        arr[i] = "传感器故障";
                    }
                    break;
                case 6:
                    if ("1".endsWith(String.valueOf(str.charAt(i)))) {
                        temp = "";
                        arr[i] = "泵堵";
                    }
                    break;
                case 7:
                    if ("1".endsWith(String.valueOf(str.charAt(i)))) {

                        arr[i] = "泵开启";
                    } else {

                        arr[i] = "泵关闭";
                    }
                    break;
                default:
                    break;
            }

        }
        for (String e : arr) {
            if (e != null) {
                temp += e;
            }

        }
        System.out.println(temp);

        return temp;

    }

    /**
     * 单独的浓度解析
     */
    public static String bxTranConcentration(String str) {
        try {
            String[] str1 = str.split(" ");
            if (str1[14].endsWith("A1")) {
                return Long.parseLong(str1[22] + str1[23], 16) + " " + unit;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("解析协议错误", e.toString());
        }
        return "";
    }

    /**
     * 解析协议
     */
    public static String bxTran(String str) {

        try {
            String[] str1 = str.split(" ");

            // 浓度
            if (str1[14].endsWith("A1")) {
                String bianhao = str1[2] + " " + str1[3] + " " + str1[4] + " " + str1[5] + " " + str1[6] + " " + str1[7] + " " + str1[8] + " " +
                        str1[9] + " " + str1[10] + " " + str1[11] + " " + str1[12] + " " + str1[13];
                String shijian = str1[16] + " " + str1[17] + " " + str1[18] + " " + str1[19] + " " + str1[20] + " " + str1[21];
                String[] s = shijian.split(" ");
                String nian = new BigInteger(s[0], 16).toString();
                String nian1 = buwei(nian);
                String yue = new BigInteger(s[1], 16).toString();
                String yue1 = buwei(yue);
                String ri = new BigInteger(s[2], 16).toString();
                String ri1 = buwei(ri);
                String shi = new BigInteger(s[3], 16).toString();
                String shi1 = buwei(shi);
                String fen = new BigInteger(s[4], 16).toString();
                String fen1 = buwei(fen);
                String miao = new BigInteger(s[5], 16).toString();
                String miao1 = buwei(miao);
                String datatime = nian1 + "年" + yue1 + "月" + ri1 + "日" + shi1 + "时" + fen1 + "分" + miao1 + "秒";

                String nongdu1;

                nongdu1 = Long.parseLong(str1[22] + str1[23], 16) + " " + unit;


                String zhuangtai = str1[24];
                String dianchidianya = str1[25] + " " + str1[26];
                String[] w = dianchidianya.split(" ");
                String dianchidianya1 = w[0] + w[1];
                double dianchidianya2 = Math.round(Integer.parseInt(new BigInteger(dianchidianya1, 16).toString()) * 10) / 10000.0D;
                String dianchidianya3 = dianchidianya2 + "伏";
                String jiaoyan = new BigInteger(str1[27], 16).toString();
                String result1 = "编号：" + hexStr2Str(bianhao.replace(" ", "")) + "\n" +
                        "时间：" + datatime + "\n" +
                        "浓度：" + nongdu1 + "\n" +
                        "状态：" + bxTranStatus(zhuangtai) + "(" + string2BinaryStr(zhuangtai) + ")\n" +
                        "电池电压：" + dianchidianya3 + "\n" +
                        "校验：" + jiaoyan;

                return result1;
            }

            // 量程
            if (str1[14].endsWith("A2")) {
                String No = str1[2] + " " + str1[3] + " " + str1[4] + " " + str1[5] + " " + str1[6] + " " + str1[7] + " " + str1[8] + " " +
                        str1[9] + " " + str1[10] + " " + str1[11] + " " + str1[12] + " " + str1[13];
                String HighAlert = new BigInteger(str1[16] + str1[17], 16).toString();
                String LowAlert = new BigInteger(str1[18] + str1[19], 16).toString();
                String MarkedPoint = new BigInteger(str1[20] + str1[21], 16).toString();
                String Range = new BigInteger(str1[22] + str1[23], 16).toString();
                String Unit = new BigInteger(str1[24], 16).toString();
                String Rem = new BigInteger(str1[25], 16).toString();
                String Check = new BigInteger(str1[26], 16).toString();
                String result = "编号：" + hexStr2Str(No.replace(" ", "")) + "\n" +
                        "高报：" + HighAlert + "\n" +
                        "低报：" + LowAlert + "\n" +
                        "标定点：" + MarkedPoint + "\n" +
                        "量程：" + Range + "\n" +
                        "单位：" + Unit + "\n" +
                        "备用：" + getUnit(Rem) + "\n" +
                        "校验：" + Check;
                return result;
            }

            if (str1[14].endsWith("A3")) {
                String No = str1[2] + " " + str1[3] + " " + str1[4] + " " + str1[5] + " " + str1[6] + " " + str1[7] + " " + str1[8] + " " +
                        str1[9] + " " + str1[10] + " " + str1[11] + " " + str1[12] + " " + str1[13];
                String hardVer = new BigInteger(str1[16], 16).toString();
                String temp = new BigInteger(str1[17], 16).toString();
                hardVer = hardVer + "." + temp;
                temp = new BigInteger(str1[18], 16).toString();
                hardVer = hardVer + "." + temp;
                temp = new BigInteger(str1[19], 16).toString();
                hardVer = hardVer + "." + temp;
                String softVer = new BigInteger(str1[20], 16).toString();
                temp = new BigInteger(str1[21], 16).toString();
                softVer = softVer + "." + temp;
                temp = new BigInteger(str1[22], 16).toString();
                softVer = softVer + "." + temp;
                temp = new BigInteger(str1[23], 16).toString();
                softVer = softVer + "." + temp;
                String sendRate = new BigInteger(str1[24], 16).toString();
                String saveRate = new BigInteger(str1[25], 16).toString();
                String Check = new BigInteger(str1[26], 16).toString();
                String result = "编号：" + hexStr2Str(No.replace(" ", "")) + "\n" +
                        "硬件版本：" + hardVer + "\n" +
                        "软件版本：" + softVer + "\n" +
                        "发送间隔：" + sendRate + "秒\n" +
                        "存储间隔：" + saveRate + "秒\n" +
                        "校验：" + Check;
                return result;
            }

            if (str1[14].endsWith("A4")) {
                String No = str1[2] + " " + str1[3] + " " + str1[4] + " " + str1[5] + " " + str1[6] + " " + str1[7] + " " + str1[8] + " " +
                        str1[9] + " " + str1[10] + " " + str1[11] + " " + str1[12] + " " + str1[13];
                String nian = new BigInteger(str1[16], 16).toString();
                String nian1 = buwei(nian);
                String yue = new BigInteger(str1[17], 16).toString();
                String yue1 = buwei(yue);
                String ri = new BigInteger(str1[18], 16).toString();
                String ri1 = buwei(ri);
                String shi = new BigInteger(str1[19], 16).toString();
                String shi1 = buwei(shi);
                String fen = new BigInteger(str1[20], 16).toString();
                String fen1 = buwei(fen);
                String miao = new BigInteger(str1[21], 16).toString();
                String miao1 = buwei(miao);
                String time = nian1 + "年" + yue1 + "月" + ri1 + "日" + shi1 + "时" + fen1 + "分" + miao1 + "秒";
                int count = (new BigInteger(str1[15], 16).intValue() - 6) / 5;
                String itemValue = "无数据存储";
                String temp = null;
                if (count >= 1) {
                    temp = new BigInteger(str1[22] + str1[23], 16).toString();
                    temp = temp + " 状态：" + bxTranStatus(str1[24]) + "\n";
                    itemValue = "第一组值：" + temp;
                }
                if (count >= 2) {
                    temp = new BigInteger(str1[27] + str1[28], 16).toString();
                    temp = temp + " 状态：" + bxTranStatus(str1[29]) + "\n";
                    itemValue = itemValue + "第二组值：" + temp;
                }
                if (count >= 3) {
                    temp = new BigInteger(str1[32] + str1[33], 16).toString();
                    temp = temp + " 状态：" + bxTranStatus(str1[34]) + "\n";
                    itemValue = itemValue + "第三组值：" + temp;
                }
                if (count >= 4) {
                    temp = new BigInteger(str1[37] + str1[38], 16).toString();
                    temp = temp + " 状态：" + bxTranStatus(str1[39]) + "\n";
                    itemValue = itemValue + "第四组值：" + temp;
                }
                if (count >= 5) {
                    temp = new BigInteger(str1[42] + str1[43], 16).toString();
                    temp = temp + " 状态：" + bxTranStatus(str1[44]) + "\n";
                    itemValue = itemValue + "第五组值：" + temp;
                }
                if (count >= 6) {
                    temp = new BigInteger(str1[47] + str1[48], 16).toString();
                    temp = temp + " 状态：" + bxTranStatus(str1[49]) + "\n";
                    itemValue = itemValue + "第六组值：" + temp;
                }
                if (count >= 7) {
                    temp = new BigInteger(str1[52] + str1[53], 16).toString();
                    temp = temp + " 状态：" + bxTranStatus(str1[54]) + "\n";
                    itemValue = itemValue + "第七组值：" + temp;
                }
                if (count >= 8) {
                    temp = new BigInteger(str1[57] + str1[58], 16).toString();
                    temp = temp + " 状态：" + bxTranStatus(str1[59]) + "\n";
                    itemValue = itemValue + "第八组值：" + temp;
                }
                String result = "编号：" + hexStr2Str(No.replace(" ", "")) + "\n" +
                        "时间：" + time + "\n" +
                        itemValue;
                return result;
            }

            if (str1[14].endsWith("AA")) {
                String sendRate = new BigInteger(str1[16], 16).toString();
                String saveRate = new BigInteger(str1[17], 16).toString();
                String result = "设置时间间隔结果反馈：\n发送间隔：" +
                        sendRate + "秒\n" +
                        "存储间隔：" + saveRate + "秒";
                return result;
            }
            if (str1[14].endsWith("AB")) {
                String snNo = str1[16] + " " + str1[17] + " " + str1[18] + " " + str1[19] + " " + str1[20] + " " + str1[21] + " " + str1[22] + " " + str1[23] + " " +
                        str1[24] + " " + str1[25] + " " + str1[26] + " " + str1[27];
                String result = "设置SN码结果反馈：\n" + hexStr2Str(snNo.replace(" ", ""));
                return result;
            }
            if (str1[14].endsWith("AC")) {
                String gjState = new BigInteger(str1[16], 16).toString();
                String djState = new BigInteger(str1[17], 16).toString();
                String hxState = new BigInteger(str1[18], 16).toString();
                String result = "关机结果反馈：\n关机标志：" + gjState + "\n" + "待机标志："
                        + djState + "\n" + "唤醒标志：" + hxState + "";
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("解析协议错误", e.toString());
        }
        return str;
    }

    public static String buwei(String nian) {
        if (nian.length() == 1) {
            DecimalFormat df = new DecimalFormat("00");
            String str2 = df.format(Integer.parseInt(nian));
            return str2;
        }
        return nian;
    }

}

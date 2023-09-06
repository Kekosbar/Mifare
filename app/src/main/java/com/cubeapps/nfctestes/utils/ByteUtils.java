package com.cubeapps.nfctestes.utils;

import java.math.BigInteger;

/**
 * @author 数据转换工具
 */
public class ByteUtils {
    //-------------------------------------------------------
    // 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    static public int isOdd(int num) {
        return num & 0x1;
    }


    //-------------------------------------------------------
    static public byte HexToByte(String inHex)//Hex字符串转byte
    {
        return (byte) Integer.parseInt(inHex, 16);
    }

    //-------------------------------------------------------
    static public String Byte2Hex(Byte inByte)//1字节转2个Hex字符
    {
        return String.format("%02x", inByte).toUpperCase();
    }

    //-------------------------------------------------------
    static public String ByteArrToHex(byte[] inBytArr)//字节数组转转hex字符串
    {
        if (inBytArr != null) {
            StringBuilder strBuilder = new StringBuilder();
            int j = inBytArr.length;
            for (int i = 0; i < j; i++) {
                strBuilder.append(Byte2Hex(inBytArr[i]));
                strBuilder.append(" ");
            }
            return strBuilder.toString();
        } else {
            return "null";
        }
    }

    public static String bytesToDecimal(byte[] array) {
        long result = 0;
        for (int i = array.length - 1; i >= 0; --i) {
            result <<= 8;
            result |= array[i] & 0x0FF;
        }
        return complate10digits(Long.toString(result));
    }

    private static String complate10digits(String str) {
    	while(str.length() < 10) {
            str = "0" + str;
        }
        return str;
    }

    //-------------------------------------------------------
    //转hex字符串转字节数组
    static public byte[] HexToByteArr(String inHex)//hex字符串转字节数组
    {
        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen) == 1) {//奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {//偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = HexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    public static byte[] getHignAndLowByte(int st) {
        st = st < 0 ? ~st + 1 : st;
        byte[] retByte = new byte[2];
        retByte[0] = (byte) ((st & 0xFF00) >> 8);
        retByte[1] = (byte) ((st & 0x00FF));
        return retByte;
    }

    public static int asc_hex(byte[] asc, byte[] hex, int asclen) {
        String ss = new String(asc);
        int string_len = ss.length();
        int len = asclen; // int len = asclen/2;
        if (string_len % 2 == 1) {// if(asclen%2 ==1){
            ss = "0" + ss;
            len++;
        }
        for (int i = 0; i < len; i++) {
            hex[i] = (byte) Integer.parseInt(ss.substring(2 * i, 2 * i + 2), 16);
        }
        return 0;
    }

    public static int hex_asc(byte[] hex, byte[] asc, int blen) {
        for (int i = 0; i < blen; i++) {
            byte temp = (byte) (hex[i] & 0xf0);
            if (temp < 0) {
                temp = (byte) (hex[i] & 0x70);
                temp = (byte) ((byte) (temp >> 4) + 0x08);
            } else
                temp = (byte) (hex[i] >> 4);

            if ((temp >= 0) && (temp <= 9))
                asc[i * 2 + 0] = (byte) ((byte) temp + '0');
            else if ((temp >= 10) && (temp <= 15))
                asc[i * 2 + 0] = (byte) ((byte) temp + 'A' - 10);
            else
                asc[i * 2 + 0] = '0';

            temp = (byte) (hex[i] & 0x0f);
            if ((temp >= 0) && (temp <= 9))
                asc[i * 2 + 1] = (byte) ((byte) temp + '0');
            else if ((temp >= 10) && (temp <= 15))
                asc[i * 2 + 1] = (byte) ((byte) temp + 'A' - 10);
            else
                asc[i * 2 + 1] = '0';
        }
        return 0;
    }

    public static String invertBits(String binary) {
        String inverted = "";
        for(char s : binary.toCharArray()) {
            inverted += s == '1' ? "0" : "1";
        }
        return inverted;
    }

    public static String convertByteToStringBinary(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

    public static String convertBinaryToHex(String binary) {
        String result = "";
        for(int i=0; i<binary.length(); i+=8) {
            String s;
            if(i+8 >= binary.length())
                s = new BigInteger(binary.substring(i, binary.length()), 2).toString(16);
            else
                s = new BigInteger(binary.substring(i, i+8), 2).toString(16);
            result += s.length() >= 2 ? s : "0" + s;
        }
        return result.toUpperCase();
    }
}
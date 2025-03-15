package com.zhouyu.pet_science.tools.encoding;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Author Doge
 * @Description 加解密工具类
 * @Date 2020/12/15
 */
public class CryptoUtil {
    /**
     * 字符串转为 32 位 MD5
     *
     * @param s
     * @return
     */
    public static String hashMD5(String s) {
        return EncodingTool.Md5.md5Hex(s);
    }

    /**
     * 计算文件 MD5 值
     *
     * @param file
     * @return
     */
    public static String hashMD5(File file) {
        try {
            return EncodingTool.Md5.md5Hex(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

//    /**
//     * Base 64 解码字符串
//     *
//     * @param s
//     * @return
//     */
//    public static String base64Decode(String s) {
//        return Base64Decoder.decodeStr(s);
//    }
//
//    /**
//     * Base 64 解码 bytes
//     *
//     * @param s
//     * @return
//     */
//    public static byte[] base64DecodeToBytes(String s) {
//        return Base64Decoder.decode(s);
//    }
//
//    /**
//     * Base 64 编码 bytes
//     *
//     * @param bytes
//     * @return
//     */
//    public static String base64Encode(byte[] bytes) {
//        return Base64Encoder.encode(bytes);
//    }

    /**
     * AES 加密，返回 bytes
     *
     * @param data
     * @param mode
     * @param key
     * @param iv
     * @return
     */
    public static byte[] aesEncrypt(byte[] data, String mode, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/" + mode + "/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            if (iv != null) cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            else cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * RSA 加密，返回 bytes
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] rsaEncrypt(byte[] data, byte[] key) {
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key));
            Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * bytes 转 16 进制串
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /**
     * zlib 压缩 bytes
     *
     * @param input
     * @return
     * @throws Exception
     */
    public static byte[] compress(byte[] input) {
        Deflater deflater = new Deflater();
        deflater.setInput(input);

        // 压缩数据
        deflater.finish();
        byte[] compressedBytes = new byte[input.length];
        int compressedLength = deflater.deflate(compressedBytes);

        // 拷贝有效数据
        return Arrays.copyOf(compressedBytes, compressedLength);
    }

    /**
     * zlib 解压缩 bytes
     *
     * @param input
     * @return
     */
    public static byte[] decompress(byte[] input) {
        Inflater inflater = new Inflater();
        inflater.setInput(input);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }

            inflater.end();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }
}

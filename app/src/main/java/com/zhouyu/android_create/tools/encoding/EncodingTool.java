package com.zhouyu.android_create.tools.encoding;

import android.annotation.SuppressLint;
import android.util.Base64;

import com.zhouyu.android_create.tools.encoding.other.Hex;
import com.zhouyu.android_create.tools.encoding.other.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncodingTool {
    /**
     * 将字符串中的Unicode编码转换为UTF-8编码
     */
    public static String decodeUnicode(String theString){
        char aChar;
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len);
        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }

                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

    /**
     * RSA公钥加密
     *
     * @param str 加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String RSAEncode( String str, String publicKey ) throws Exception{
        //base64编码的公钥
        byte[] decoded = Base64.decode(publicKey,Base64.DEFAULT);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.encodeToString(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)),Base64.DEFAULT);
    }

    /**
     * Md5、SHA256加密
     */
    private static byte[] digest(MessageDigest messageDigest, InputStream data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }
    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException var2) {
            throw new IllegalArgumentException(var2);
        }
    }
    private static MessageDigest updateDigest(MessageDigest digest, InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];

        for(int read = inputStream.read(buffer, 0, 1024); read > -1; read = inputStream.read(buffer, 0, 1024)) {
            digest.update(buffer, 0, read);
        }
        return digest;
    }
    public static class Md5{
        private static MessageDigest getMd5Digest() {
            return getDigest("MD5");
        }

        public static byte[] md5(byte[] data) {
            if(data == null){
                return null;
            }
            return getMd5Digest().digest(data);
        }

        public static byte[] md5(InputStream data) throws IOException {
            return digest(getMd5Digest(), data);
        }

        public static byte[] md5(String data) {
            return md5(StringUtils.getBytesUtf8(data));
        }

        public static String md5Hex(String data) {
            return Hex.encodeHexString(md5(data));
        }

        public static String md5Hex(byte[] data) {
            return Hex.encodeHexString(md5(data));
        }

        public static String md5Hex(InputStream data) throws IOException {
            return Hex.encodeHexString(md5(data));
        }
    }


    public static class SHA256{
        private static MessageDigest getSha256Digest() {
            return getDigest("SHA-256");
        }

        public static byte[] sha256(byte[] data) {
            return getSha256Digest().digest(data);
        }

        public static byte[] sha256(InputStream data) throws IOException {
            return digest(getSha256Digest(), data);
        }

        public static byte[] sha256(String data) {
            return sha256(StringUtils.getBytesUtf8(data));
        }

        public static String sha256Hex(byte[] data) {
            return Hex.encodeHexString(sha256(data));
        }

        public static String sha256Hex(InputStream data) throws IOException {
            return Hex.encodeHexString(sha256(data));
        }

        public static String sha256Hex(String data) {
            return Hex.encodeHexString(sha256(data));
        }
    }

    /**
     * AES加密
     */
    public static String AESEncode(String sKey,String sSrc, String ivParameter) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] raw = sKey.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encrypted,Base64.DEFAULT);//此处使用BASE64做转码。
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    /**
     * AES解密
     */
    public static String AESDecrypt(String sKey,String sSrc,String ivParameter){
        try {
            byte[] raw = sKey.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] base64 = Base64.decode(sSrc, Base64.DEFAULT);
            byte[] original = cipher.doFinal(base64);
            return new String(original);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    /**
     * AES 加密
     *
     * @param secretKey 加密密码，长度：16 或 32 个字符
     * @param data      待加密内容
     * @return 返回Base64转码后的加密数据
     */
    public static String AESEncode2(String secretKey, String data) {
        try {
            // 创建AES秘钥
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            // 创建密码器
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            // 初始化加密器
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptByte = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // 将加密以后的数据进行 Base64 编码
            return Base64.encodeToString(encryptByte,Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * AES 解密
     *
     * @param secretKey  解密的密钥，长度：16 或 32 个字符
     * @param base64Data 加密的密文 Base64 字符串
     */
    public static String AESDecrypt2(String secretKey, String base64Data) {
        try {
            byte[] data = Base64.decode(base64Data,Base64.DEFAULT);
            // 创建AES秘钥
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            // 创建密码器
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            // 初始化解密器
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            // 执行解密操作
            byte[] result = cipher.doFinal(data);
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String SHA1(String input){
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }


    public static final class KW_Encrypt{

        /* renamed from: a  reason: collision with root package name */
//        public static final byte[] f4894a = "ylzsxkwm".getBytes();

//        /* renamed from: kuwo.b  reason: collision with root package name */
//        public static final int f4895b = f4894a.length;
//
//        /* renamed from: c  reason: collision with root package name */
//        public static final byte[] f4896c = "kwks&@69".getBytes();

        /* renamed from: kuwo.d  reason: collision with root package name */
        private static final long[] f4897d = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, 268435456, 536870912, 1073741824, 2147483648L, 4294967296L, 8589934592L, 17179869184L, 34359738368L, 68719476736L, 137438953472L, 274877906944L, 549755813888L, 1099511627776L, 2199023255552L, 4398046511104L, 8796093022208L, 17592186044416L, 35184372088832L, 70368744177664L, 140737488355328L, 281474976710656L, 562949953421312L, 1125899906842624L, 2251799813685248L, 4503599627370496L, 9007199254740992L, 18014398509481984L, 36028797018963968L, 72057594037927936L, 144115188075855872L, 288230376151711744L, 576460752303423488L, 1152921504606846976L, 2305843009213693952L, 4611686018427387904L, Long.MIN_VALUE};

        /* renamed from: e  reason: collision with root package name */
        private static final int[] f4898e = {57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7, 56, 48, 40, 32, 24, 16, 8, 0, 58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6};

        /* renamed from: f  reason: collision with root package name */
        private static final int[] f4899f = {31, 0, 1, 2, 3, 4, -1, -1, 3, 4, 5, 6, 7, 8, -1, -1, 7, 8, 9, 10, 11, 12, -1, -1, 11, 12, 13, 14, 15, 16, -1, -1, 15, 16, 17, 18, 19, 20, -1, -1, 19, 20, 21, 22, 23, 24, -1, -1, 23, 24, 25, 26, 27, 28, -1, -1, 27, 28, 29, 30, 31, 30, -1, -1};

        /* renamed from: g  reason: collision with root package name */
        private static final char[][] f4900g = {new char[]{14, 4, 3, 15, 2, '\r', 5, 3, '\r', 14, 6, '\t', 11, 2, 0, 5, 4, 1, '\n', '\f', 15, 6, '\t', '\n', 1, '\b', '\f', 7, '\b', 11, 7, 0, 0, 15, '\n', 5, 14, 4, '\t', '\n', 7, '\b', '\f', 3, '\r', 1, 3, 6, 15, '\f', 6, 11, 2, '\t', 5, 0, 4, 2, 11, 14, 1, 7, '\b', '\r'}, new char[]{15, 0, '\t', 5, 6, '\n', '\f', '\t', '\b', 7, 2, '\f', 3, '\r', 5, 2, 1, 14, 7, '\b', 11, 4, 0, 3, 14, 11, '\r', 6, 4, 1, '\n', 15, 3, '\r', '\f', 11, 15, 3, 6, 0, 4, '\n', 1, 7, '\b', 4, 11, 14, '\r', '\b', 0, 6, 2, 15, '\t', 5, 7, 1, '\n', '\f', 14, 2, 5, '\t'}, new char[]{'\n', '\r', 1, 11, 6, '\b', 11, 5, '\t', 4, '\f', 2, 15, 3, 2, 14, 0, 6, '\r', 1, 3, 15, 4, '\n', 14, '\t', 7, '\f', 5, 0, '\b', 7, '\r', 1, 2, 4, 3, 6, '\f', 11, 0, '\r', 5, 14, 6, '\b', 15, 2, 7, '\n', '\b', 15, 4, '\t', 11, 5, '\t', 0, 14, 3, '\n', 7, 1, '\f'}, new char[]{7, '\n', 1, 15, 0, '\f', 11, 5, 14, '\t', '\b', 3, '\t', 7, 4, '\b', '\r', 6, 2, 1, 6, 11, '\f', 2, 3, 0, 5, 14, '\n', '\r', 15, 4, '\r', 3, 4, '\t', 6, '\n', 1, '\f', 11, 0, 2, 5, 0, '\r', 14, 2, '\b', 15, 7, 4, 15, 1, '\n', 7, 5, 6, '\f', 11, 3, '\b', '\t', 14}, new char[]{2, 4, '\b', 15, 7, '\n', '\r', 6, 4, 1, 3, '\f', 11, 7, 14, 0, '\f', 2, 5, '\t', '\n', '\r', 0, 3, 1, 11, 15, 5, 6, '\b', '\t', 14, 14, 11, 5, 6, 4, 1, 3, '\n', 2, '\f', 15, 0, '\r', 2, '\b', 5, 11, '\b', 0, 15, 7, 14, '\t', 4, '\f', 7, '\n', '\t', 1, '\r', 6, 3}, new char[]{'\f', '\t', 0, 7, '\t', 2, 14, 1, '\n', 15, 3, 4, 6, '\f', 5, 11, 1, 14, '\r', 0, 2, '\b', 7, '\r', 15, 5, 4, '\n', '\b', 3, 11, 6, '\n', 4, 6, 11, 7, '\t', 0, 6, 4, 2, '\r', 1, '\t', 15, 3, '\b', 15, 3, 1, 14, '\f', 5, 11, 0, 2, '\f', 14, 7, 5, '\n', '\b', '\r'}, new char[]{4, 1, 3, '\n', 15, '\f', 5, 0, 2, 11, '\t', 6, '\b', 7, 6, '\t', 11, 4, '\f', 15, 0, 3, '\n', 5, 14, '\r', 7, '\b', '\r', 14, 1, 2, '\r', 6, 14, '\t', 4, 1, 2, 14, 11, '\r', 5, 0, 1, '\n', '\b', 3, 0, 11, 3, 5, '\t', 4, 15, 2, 7, '\b', '\f', 15, '\n', 7, 6, '\f'}, new char[]{'\r', 7, '\n', 0, 6, '\t', 5, 15, '\b', 4, 3, '\n', 11, 14, '\f', 5, 2, 11, '\t', 6, 15, '\f', 0, 3, 4, 1, 14, '\r', 1, 2, 7, '\b', 1, 2, '\f', 15, '\n', 4, 0, 3, '\r', 14, 6, '\t', 7, '\b', '\t', 6, 15, 1, 5, '\f', 3, '\n', 14, 5, '\b', 7, 11, 0, 4, '\r', 2, 11}};

        /* renamed from: h  reason: collision with root package name */
        private static final int[] f4901h = {15, 6, 19, 20, 28, 11, 27, 16, 0, 14, 22, 25, 4, 17, 30, 9, 1, 7, 23, 13, 31, 26, 2, 8, 18, 12, 29, 5, 21, 10, 3, 24};

        /* renamed from: i  reason: collision with root package name */
        private static final int[] f4902i = {39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29, 36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25, 32, 0, 40, 8, 48, 16, 56, 24};
        private static final int[] j = {56, 48, 40, 32, 24, 16, 8, 0, 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 60, 52, 44, 36, 28, 20, 12, 4, 27, 19, 11, 3};
        private static final int[] k = {13, 16, 10, 23, 0, 4, -1, -1, 2, 27, 14, 5, 20, 9, -1, -1, 22, 18, 11, 3, 25, 7, -1, -1, 15, 6, 26, 19, 12, 1, -1, -1, 40, 51, 30, 36, 46, 54, -1, -1, 29, 39, 50, 44, 32, 47, -1, -1, 43, 48, 38, 55, 33, 52, -1, -1, 45, 41, 49, 35, 28, 31, -1, -1};
        private static final int[] l = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
        private static final long[] m = {0, 1048577, 3145731};
        private static final int n = 0;
        private static final int o = 1;
        private static long p = 0;
        private static long q;
        private static long r;
        private static int[] s = new int[2];
        private static byte[] t = new byte[8];
        private static int u;
        private static int v;
        private static int w;

        private static long a(int[] iArr, int i2, long j2) {
            long j3 = 0;
            for (int i3 = 0; i3 < i2; i3++) {
                if (iArr[i3] >= 0 && (f4897d[iArr[i3]] & j2) != 0) {
                    j3 |= f4897d[i3];
                }
            }
            return j3;
        }

        private static void a(long j2, long[] jArr, int i2) {
            long a2 = a(j, 56, j2);
            for (int i3 = 0; i3 < 16; i3++) {
                a2 = ((a2 & (~m[l[i3]])) >> l[i3]) | ((m[l[i3]] & a2) << (28 - l[i3]));
                jArr[i3] = a(k, 64, a2);
            }
            if (i2 == 1) {
                for (int i4 = 0; i4 < 8; i4++) {
                    long j3 = jArr[i4];
                    int i5 = 15 - i4;
                    jArr[i4] = jArr[i5];
                    jArr[i5] = j3;
                }
            }
        }

        private static long a(long[] jArr, long j2) {
            p = a(f4898e, 64, j2);
            s[0] = (int) (p & 4294967295L);
            s[1] = (int) ((p & -4294967296L) >> 32);
            for (int i2 = 0; i2 < 16; i2++) {
                r = (long) s[1];
                r = a(f4899f, 64, r);
                r ^= jArr[i2];
                for (int i3 = 0; i3 < 8; i3++) {
                    t[i3] = (byte) ((int) (255 & (r >> (i3 * 8))));
                }
                u = 0;
                int i4 = 7;
                while (true) {
                    w = i4;
                    if (w < 0) {
                        break;
                    }
                    u <<= 4;
                    u |= f4900g[w][t[w]];
                    i4 = w - 1;
                }
                r = (long) u;
                r = a(f4901h, 32, r);
                q = (long) s[0];
                s[0] = s[1];
                s[1] = (int) (q ^ r);
            }
            v = s[0];
            s[0] = s[1];
            s[1] = v;
            p = (((long) s[0]) & 4294967295L) | ((((long) s[1]) << 32) & -4294967296L);
            p = a(f4902i, 64, p);
            return p;
        }

        public static synchronized byte[] a(byte[] bArr, int i2, byte[] bArr2, int i3) {
            byte[] bArr3;
//        synchronized (d.class) {
            long j2 = 0;
            for (int i4 = 0; i4 < 8; i4++) {
                j2 |= ((long) bArr2[i4]) << (i4 * 8);
            }
            int i5 = i2 / 8;
            long[] jArr = new long[16];
            for (int i6 = 0; i6 < 16; i6++) {
                jArr[i6] = 0;
            }
            long[] jArr2 = new long[i5];
            for (int i7 = 0; i7 < i5; i7++) {
                for (int i8 = 0; i8 < 8; i8++) {
                    jArr2[i7] = (((long) bArr[(i7 * 8) + i8]) << (i8 * 8)) | jArr2[i7];
                }
            }
            long[] jArr3 = new long[((((i5 + 1) * 8) + 1) / 8)];
            a(j2, jArr, 0);
            for (int i9 = 0; i9 < i5; i9++) {
                jArr3[i9] = a(jArr, jArr2[i9]);
            }
            int i10 = i2 % 8;
            int i11 = i5 * 8;
            int i12 = i2 - i11;
            byte[] bArr4 = new byte[i12];
            System.arraycopy(bArr, i11, bArr4, 0, i12);
            long j3 = 0;
            for (int i13 = 0; i13 < i10; i13++) {
                j3 |= ((long) bArr4[i13]) << (i13 * 8);
            }
            jArr3[i5] = a(jArr, j3);
            bArr3 = new byte[(jArr3.length * 8)];
            int i14 = 0;
            int i15 = 0;
            while (i14 < jArr3.length) {
                int i16 = i15;
                for (int i17 = 0; i17 < 8; i17++) {
                    bArr3[i16] = (byte) ((int) (255 & (jArr3[i14] >> (i17 * 8))));
                    i16++;
                }
                i14++;
                i15 = i16;
            }
//        }
            return bArr3;
        }

        public static synchronized byte[] b(byte[] bArr, int i2, byte[] bArr2, int i3) {
            byte[] bArr3;
            synchronized (KW_Encrypt.class) {
                long j2 = 0;
                for (int i4 = 0; i4 < 8; i4++) {
                    j2 |= ((long) bArr2[i4]) << (i4 * 8);
                }
                int i5 = i2 / 8;
                long[] jArr = new long[16];
                for (int i6 = 0; i6 < 16; i6++) {
                    jArr[i6] = 0;
                }
                long[] jArr2 = new long[i5];
                for (int i7 = 0; i7 < i5; i7++) {
                    for (int i8 = 0; i8 < 8; i8++) {
                        jArr2[i7] = (((long) (bArr[(i7 * 8) + i8] & 255)) << (i8 * 8)) | jArr2[i7];
                    }
                }
                long[] jArr3 = new long[((((i5 + 1) * 8) + 1) / 8)];
                a(j2, jArr, 0);
                for (int i9 = 0; i9 < i5; i9++) {
                    jArr3[i9] = a(jArr, jArr2[i9]);
                }
                int i10 = i2 % 8;
                int i11 = i5 * 8;
                int i12 = i2 - i11;
                byte[] bArr4 = new byte[i12];
                System.arraycopy(bArr, i11, bArr4, 0, i12);
                long j3 = 0;
                for (int i13 = 0; i13 < i10; i13++) {
                    j3 |= ((long) (bArr4[i13] & 255)) << (i13 * 8);
                }
                jArr3[i5] = a(jArr, j3);
                bArr3 = new byte[(jArr3.length * 8)];
                int i14 = 0;
                int i15 = 0;
                while (i14 < jArr3.length) {
                    int i16 = i15;
                    for (int i17 = 0; i17 < 8; i17++) {
                        bArr3[i16] = (byte) ((int) (255 & (jArr3[i14] >> (i17 * 8))));
                        i16++;
                    }
                    i14++;
                    i15 = i16;
                }
            }
            return bArr3;
        }

        public static synchronized byte[] a(byte[] bArr, byte[] bArr2) {
            long j2;
            byte[] bArr3;
            synchronized (KW_Encrypt.class) {
                int length = bArr.length;
                int length2 = bArr2.length;
                long[] jArr = new long[16];
                int i2 = 0;
                while (true) {
                    j2 = 0;
                    if (i2 >= 16) {
                        break;
                    }
                    jArr[i2] = 0;
                    i2++;
                }
                long j3 = 0;
                for (int i3 = 0; i3 < 8; i3++) {
                    j3 |= ((long) bArr2[i3]) << (i3 * 8);
                }
                a(j3, jArr, 0);
                int i4 = length / 8;
                long[] jArr2 = new long[i4];
                for (int i5 = 0; i5 < i4; i5++) {
                    for (int i6 = 0; i6 < 8; i6++) {
                        jArr2[i5] = jArr2[i5] | (((long) (bArr[(i5 * 8) + i6] & 255)) << (i6 * 8));
                    }
                }
                long[] jArr3 = new long[((((i4 + 1) * 8) + 1) / 8)];
                for (int i7 = 0; i7 < i4; i7++) {
                    jArr3[i7] = a(jArr, jArr2[i7]);
                }
                int i8 = length % 8;
                int i9 = i4 * 8;
                int i10 = length - i9;
                byte[] bArr4 = new byte[i10];
                System.arraycopy(bArr, i9, bArr4, 0, i10);
                for (int i11 = 0; i11 < i8; i11++) {
                    j2 |= ((long) (bArr4[i11] & 255)) << (i11 * 8);
                }
                jArr3[i4] = a(jArr, j2);
                bArr3 = new byte[(jArr3.length * 8)];
                int i12 = 0;
                int i13 = 0;
                while (i12 < jArr3.length) {
                    int i14 = i13;
                    for (int i15 = 0; i15 < 8; i15++) {
                        bArr3[i14] = (byte) ((int) (255 & (jArr3[i12] >> (i15 * 8))));
                        i14++;
                    }
                    i12++;
                    i13 = i14;
                }
            }
            return bArr3;
        }

        public static synchronized byte[] b(byte[] bArr, byte[] bArr2) {
            byte[] bArr3;
            synchronized (KW_Encrypt.class) {
                int length = bArr.length;
                int length2 = bArr2.length;
                long[] jArr = new long[16];
                long j2 = 0;
                for (int i2 = 0; i2 < 8; i2++) {
                    j2 |= ((long) bArr2[i2]) << (i2 * 8);
                }
                for (int i3 = 0; i3 < 16; i3++) {
                    jArr[i3] = 0;
                }
                a(j2, jArr, 1);
                int i4 = length / 8;
                long[] jArr2 = new long[i4];
                for (int i5 = 0; i5 < i4; i5++) {
                    for (int i6 = 0; i6 < 8; i6++) {
                        jArr2[i5] = jArr2[i5] | (((long) (bArr[(i5 * 8) + i6] & 255)) << (i6 * 8));
                    }
                }
                long[] jArr3 = new long[i4];
                for (int i7 = 0; i7 < i4; i7++) {
                    jArr3[i7] = a(jArr, jArr2[i7]);
                }
                bArr3 = new byte[(i4 * 8)];
                for (int i8 = 0; i8 < i4; i8++) {
                    for (int i9 = 0; i9 < 8; i9++) {
                        bArr3[(i8 * 8) + i9] = (byte) ((int) (255 & (jArr3[i8] >> (i9 * 8))));
                    }
                }
            }
            return bArr3;
        }
    }
}

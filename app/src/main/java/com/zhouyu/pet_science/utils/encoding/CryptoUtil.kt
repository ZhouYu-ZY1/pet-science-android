package com.zhouyu.pet_science.utils.encoding

import com.zhouyu.pet_science.utils.encoding.EncodingTool.Md5
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.zip.Deflater
import java.util.zip.Inflater
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * @Author Doge
 * @Description 加解密工具类
 * @Date 2020/12/15
 */
object CryptoUtil {
    /**
     * 字符串转为 32 位 MD5
     *
     * @param s
     * @return
     */
    fun hashMD5(s: String?): String? {
        return Md5.md5Hex(s)
    }

    /**
     * 计算文件 MD5 值
     *
     * @param file
     * @return
     */
    fun hashMD5(file: File?): String? {
        return try {
            Md5.md5Hex(FileInputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            ""
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
    fun aesEncrypt(data: ByteArray?, mode: String, key: ByteArray?, iv: ByteArray?): ByteArray? {
        return try {
            val cipher = Cipher.getInstance("AES/$mode/PKCS5Padding")
            val secretKeySpec = SecretKeySpec(key, "AES")
            if (iv != null) cipher.init(
                Cipher.ENCRYPT_MODE,
                secretKeySpec,
                IvParameterSpec(iv)
            ) else cipher.init(
                Cipher.ENCRYPT_MODE, secretKeySpec
            )
            cipher.doFinal(data)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * RSA 加密，返回 bytes
     *
     * @param data
     * @param key
     * @return
     */
    fun rsaEncrypt(data: ByteArray?, key: ByteArray?): ByteArray? {
        return try {
            val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(key))
            val cipher = Cipher.getInstance("RSA/ECB/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            cipher.doFinal(data)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * bytes 转 16 进制串
     *
     * @param bytes
     * @return
     */
    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) sb.append(String.format("%02x", b))
        return sb.toString()
    }

    /**
     * zlib 压缩 bytes
     *
     * @param input
     * @return
     * @throws Exception
     */
    fun compress(input: ByteArray): ByteArray {
        val deflater = Deflater()
        deflater.setInput(input)

        // 压缩数据
        deflater.finish()
        val compressedBytes = ByteArray(input.size)
        val compressedLength = deflater.deflate(compressedBytes)

        // 拷贝有效数据
        return compressedBytes.copyOf(compressedLength)
    }

    /**
     * zlib 解压缩 bytes
     *
     * @param input
     * @return
     */
    fun decompress(input: ByteArray?): ByteArray {
        val inflater = Inflater()
        inflater.setInput(input)
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        try {
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            inflater.end()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputStream.toByteArray()
    }
}

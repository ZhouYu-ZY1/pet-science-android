package com.zhouyu.pet_science.utils.encoding

import android.annotation.SuppressLint
import android.util.Base64
import com.zhouyu.pet_science.utils.encoding.other.Hex.Companion.encodeHexString
import com.zhouyu.pet_science.utils.encoding.other.StringUtils.getBytesUtf8
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncodingTool {
    /**
     * 将字符串中的Unicode编码转换为UTF-8编码
     */
    fun decodeUnicode(theString: String): String {
        var aChar: Char
        val len = theString.length
        val outBuffer = StringBuilder(len)
        var x = 0
        while (x < len) {
            aChar = theString[x++]
            if (aChar == '\\') {
                aChar = theString[x++]
                if (aChar == 'u') {
                    var value = 0
                    for (i in 0..3) {
                        aChar = theString[x++]
                        value = when (aChar) {
                            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (value shl 4) + aChar.code - '0'.code
                            'a', 'b', 'c', 'd', 'e', 'f' -> (value shl 4) + 10 + aChar.code - 'a'.code
                            'A', 'B', 'C', 'D', 'E', 'F' -> (value shl 4) + 10 + aChar.code - 'A'.code
                            else -> throw IllegalArgumentException(
                                "Malformed   \\uxxxx   encoding."
                            )
                        }
                    }
                    outBuffer.append(value.toChar())
                } else {
                    if (aChar == 't') aChar = '\t' else if (aChar == 'r') aChar =
                        '\r' else if (aChar == 'n') aChar = '\n' else if (aChar == 'f') aChar = '\u000c'
                    outBuffer.append(aChar)
                }
            } else outBuffer.append(aChar)
        }
        return outBuffer.toString()
    }

    /**
     * RSA公钥加密
     *
     * @param str 加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    @Throws(Exception::class)
    fun RSAEncode(str: String, publicKey: String?): String {
        //base64编码的公钥
        val decoded = Base64.decode(publicKey, Base64.DEFAULT)
        val pubKey = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(decoded)) as RSAPublicKey
        //RSA加密
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, pubKey)
        return Base64.encodeToString(
            cipher.doFinal(str.toByteArray(StandardCharsets.UTF_8)),
            Base64.DEFAULT
        )
    }

    /**
     * Md5、SHA256加密
     */
    @Throws(IOException::class)
    private fun digest(messageDigest: MessageDigest, data: InputStream): ByteArray {
        return updateDigest(messageDigest, data).digest()
    }

    private fun getDigest(algorithm: String): MessageDigest {
        return try {
            MessageDigest.getInstance(algorithm)
        } catch (var2: NoSuchAlgorithmException) {
            throw IllegalArgumentException(var2)
        }
    }

    @Throws(IOException::class)
    private fun updateDigest(digest: MessageDigest, inputStream: InputStream): MessageDigest {
        val buffer = ByteArray(1024)
        var read = inputStream.read(buffer, 0, 1024)
        while (read > -1) {
            digest.update(buffer, 0, read)
            read = inputStream.read(buffer, 0, 1024)
        }
        return digest
    }

    /**
     * AES加密
     */
    fun AESEncode(sKey: String, sSrc: String, ivParameter: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val raw = sKey.toByteArray()
            val skeySpec = SecretKeySpec(raw, "AES")
            val iv = IvParameterSpec(ivParameter.toByteArray()) //使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
            val encrypted = cipher.doFinal(sSrc.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.DEFAULT) //此处使用BASE64做转码。
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * AES解密
     */
    fun AESDecrypt(sKey: String, sSrc: String?, ivParameter: String): String {
        return try {
            val raw = sKey.toByteArray()
            val skeySpec = SecretKeySpec(raw, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = IvParameterSpec(ivParameter.toByteArray())
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val base64 = Base64.decode(sSrc, Base64.DEFAULT)
            val original = cipher.doFinal(base64)
            String(original)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * AES 加密
     *
     * @param secretKey 加密密码，长度：16 或 32 个字符
     * @param data      待加密内容
     * @return 返回Base64转码后的加密数据
     */
    fun AESEncode2(secretKey: String, data: String): String {
        try {
            // 创建AES秘钥
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), "AES")
            // 创建密码器
            @SuppressLint("GetInstance") val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            // 初始化加密器
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val encryptByte = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            // 将加密以后的数据进行 Base64 编码
            return Base64.encodeToString(encryptByte, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * AES 解密
     *
     * @param secretKey  解密的密钥，长度：16 或 32 个字符
     * @param base64Data 加密的密文 Base64 字符串
     */
    fun AESDecrypt2(secretKey: String, base64Data: String?): String {
        try {
            val data = Base64.decode(base64Data, Base64.DEFAULT)
            // 创建AES秘钥
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), "AES")
            // 创建密码器
            @SuppressLint("GetInstance") val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            // 初始化解密器
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            // 执行解密操作
            val result = cipher.doFinal(data)
            return String(result, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun SHA1(input: String): String {
        return try {
            val mDigest = MessageDigest.getInstance("SHA1")
            val result = mDigest.digest(input.toByteArray())
            val sb = StringBuilder()
            for (b in result) {
                sb.append(((b.toInt() and 0xff) + 0x100).toString(16).substring(1))
            }
            sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    object Md5 {
        private val md5Digest: MessageDigest
            private get() = getDigest("MD5")

        fun md5(data: ByteArray?): ByteArray? {
            return if (data == null) {
                null
            } else md5Digest.digest(data)
        }

        @Throws(IOException::class)
        fun md5(data: InputStream): ByteArray {
            return digest(md5Digest, data)
        }

        fun md5(data: String?): ByteArray? {
            return md5(getBytesUtf8(data))
        }

        fun md5Hex(data: String?): String {
            return encodeHexString(md5(data))
        }

        fun md5Hex(data: ByteArray?): String {
            return encodeHexString(md5(data))
        }

        @Throws(IOException::class)
        fun md5Hex(data: InputStream): String {
            return encodeHexString(md5(data))
        }
    }

    object SHA256 {
        private val sha256Digest: MessageDigest
            private get() = getDigest("SHA-256")

        fun sha256(data: ByteArray?): ByteArray {
            return sha256Digest.digest(data)
        }

        @Throws(IOException::class)
        fun sha256(data: InputStream): ByteArray {
            return digest(sha256Digest, data)
        }

        fun sha256(data: String?): ByteArray {
            return sha256(getBytesUtf8(data))
        }

        fun sha256Hex(data: ByteArray?): String {
            return encodeHexString(sha256(data))
        }

        @Throws(IOException::class)
        fun sha256Hex(data: InputStream): String {
            return encodeHexString(sha256(data))
        }

        fun sha256Hex(data: String?): String {
            return encodeHexString(sha256(data))
        }
    }

    object KW_Encrypt {
        /* renamed from: a  reason: collision with root package name */ //        public static final byte[] f4894a = "ylzsxkwm".getBytes();
        //        /* renamed from: kuwo.b  reason: collision with root package name */
        //        public static final int f4895b = f4894a.length;
        //
        //        /* renamed from: c  reason: collision with root package name */
        //        public static final byte[] f4896c = "kwks&@69".getBytes();
        /* renamed from: kuwo.d  reason: collision with root package name */
        private val f4897d = longArrayOf(
            1,
            2,
            4,
            8,
            16,
            32,
            64,
            128,
            256,
            512,
            1024,
            2048,
            4096,
            8192,
            16384,
            32768,
            65536,
            131072,
            262144,
            524288,
            1048576,
            2097152,
            4194304,
            8388608,
            16777216,
            33554432,
            67108864,
            134217728,
            268435456,
            536870912,
            1073741824,
            2147483648L,
            4294967296L,
            8589934592L,
            17179869184L,
            34359738368L,
            68719476736L,
            137438953472L,
            274877906944L,
            549755813888L,
            1099511627776L,
            2199023255552L,
            4398046511104L,
            8796093022208L,
            17592186044416L,
            35184372088832L,
            70368744177664L,
            140737488355328L,
            281474976710656L,
            562949953421312L,
            1125899906842624L,
            2251799813685248L,
            4503599627370496L,
            9007199254740992L,
            18014398509481984L,
            36028797018963968L,
            72057594037927936L,
            144115188075855872L,
            288230376151711744L,
            576460752303423488L,
            1152921504606846976L,
            2305843009213693952L,
            4611686018427387904L,
            Long.MIN_VALUE
        )

        /* renamed from: e  reason: collision with root package name */
        private val f4898e = intArrayOf(
            57,
            49,
            41,
            33,
            25,
            17,
            9,
            1,
            59,
            51,
            43,
            35,
            27,
            19,
            11,
            3,
            61,
            53,
            45,
            37,
            29,
            21,
            13,
            5,
            63,
            55,
            47,
            39,
            31,
            23,
            15,
            7,
            56,
            48,
            40,
            32,
            24,
            16,
            8,
            0,
            58,
            50,
            42,
            34,
            26,
            18,
            10,
            2,
            60,
            52,
            44,
            36,
            28,
            20,
            12,
            4,
            62,
            54,
            46,
            38,
            30,
            22,
            14,
            6
        )

        /* renamed from: f  reason: collision with root package name */
        private val f4899f = intArrayOf(
            31,
            0,
            1,
            2,
            3,
            4,
            -1,
            -1,
            3,
            4,
            5,
            6,
            7,
            8,
            -1,
            -1,
            7,
            8,
            9,
            10,
            11,
            12,
            -1,
            -1,
            11,
            12,
            13,
            14,
            15,
            16,
            -1,
            -1,
            15,
            16,
            17,
            18,
            19,
            20,
            -1,
            -1,
            19,
            20,
            21,
            22,
            23,
            24,
            -1,
            -1,
            23,
            24,
            25,
            26,
            27,
            28,
            -1,
            -1,
            27,
            28,
            29,
            30,
            31,
            30,
            -1,
            -1
        )

        /* renamed from: g  reason: collision with root package name */
        private val f4900g = arrayOf(
            charArrayOf(
                14.toChar(),
                4.toChar(),
                3.toChar(),
                15.toChar(),
                2.toChar(),
                '\r',
                5.toChar(),
                3.toChar(),
                '\r',
                14.toChar(),
                6.toChar(),
                '\t',
                11.toChar(),
                2.toChar(),
                0.toChar(),
                5.toChar(),
                4.toChar(),
                1.toChar(),
                '\n',
                '\u000c',
                15.toChar(),
                6.toChar(),
                '\t',
                '\n',
                1.toChar(),
                '\b',
                '\u000c',
                7.toChar(),
                '\b',
                11.toChar(),
                7.toChar(),
                0.toChar(),
                0.toChar(),
                15.toChar(),
                '\n',
                5.toChar(),
                14.toChar(),
                4.toChar(),
                '\t',
                '\n',
                7.toChar(),
                '\b',
                '\u000c',
                3.toChar(),
                '\r',
                1.toChar(),
                3.toChar(),
                6.toChar(),
                15.toChar(),
                '\u000c',
                6.toChar(),
                11.toChar(),
                2.toChar(),
                '\t',
                5.toChar(),
                0.toChar(),
                4.toChar(),
                2.toChar(),
                11.toChar(),
                14.toChar(),
                1.toChar(),
                7.toChar(),
                '\b',
                '\r'
            ),
            charArrayOf(
                15.toChar(),
                0.toChar(),
                '\t',
                5.toChar(),
                6.toChar(),
                '\n',
                '\u000c',
                '\t',
                '\b',
                7.toChar(),
                2.toChar(),
                '\u000c',
                3.toChar(),
                '\r',
                5.toChar(),
                2.toChar(),
                1.toChar(),
                14.toChar(),
                7.toChar(),
                '\b',
                11.toChar(),
                4.toChar(),
                0.toChar(),
                3.toChar(),
                14.toChar(),
                11.toChar(),
                '\r',
                6.toChar(),
                4.toChar(),
                1.toChar(),
                '\n',
                15.toChar(),
                3.toChar(),
                '\r',
                '\u000c',
                11.toChar(),
                15.toChar(),
                3.toChar(),
                6.toChar(),
                0.toChar(),
                4.toChar(),
                '\n',
                1.toChar(),
                7.toChar(),
                '\b',
                4.toChar(),
                11.toChar(),
                14.toChar(),
                '\r',
                '\b',
                0.toChar(),
                6.toChar(),
                2.toChar(),
                15.toChar(),
                '\t',
                5.toChar(),
                7.toChar(),
                1.toChar(),
                '\n',
                '\u000c',
                14.toChar(),
                2.toChar(),
                5.toChar(),
                '\t'
            ),
            charArrayOf(
                '\n',
                '\r',
                1.toChar(),
                11.toChar(),
                6.toChar(),
                '\b',
                11.toChar(),
                5.toChar(),
                '\t',
                4.toChar(),
                '\u000c',
                2.toChar(),
                15.toChar(),
                3.toChar(),
                2.toChar(),
                14.toChar(),
                0.toChar(),
                6.toChar(),
                '\r',
                1.toChar(),
                3.toChar(),
                15.toChar(),
                4.toChar(),
                '\n',
                14.toChar(),
                '\t',
                7.toChar(),
                '\u000c',
                5.toChar(),
                0.toChar(),
                '\b',
                7.toChar(),
                '\r',
                1.toChar(),
                2.toChar(),
                4.toChar(),
                3.toChar(),
                6.toChar(),
                '\u000c',
                11.toChar(),
                0.toChar(),
                '\r',
                5.toChar(),
                14.toChar(),
                6.toChar(),
                '\b',
                15.toChar(),
                2.toChar(),
                7.toChar(),
                '\n',
                '\b',
                15.toChar(),
                4.toChar(),
                '\t',
                11.toChar(),
                5.toChar(),
                '\t',
                0.toChar(),
                14.toChar(),
                3.toChar(),
                '\n',
                7.toChar(),
                1.toChar(),
                '\u000c'
            ),
            charArrayOf(
                7.toChar(),
                '\n',
                1.toChar(),
                15.toChar(),
                0.toChar(),
                '\u000c',
                11.toChar(),
                5.toChar(),
                14.toChar(),
                '\t',
                '\b',
                3.toChar(),
                '\t',
                7.toChar(),
                4.toChar(),
                '\b',
                '\r',
                6.toChar(),
                2.toChar(),
                1.toChar(),
                6.toChar(),
                11.toChar(),
                '\u000c',
                2.toChar(),
                3.toChar(),
                0.toChar(),
                5.toChar(),
                14.toChar(),
                '\n',
                '\r',
                15.toChar(),
                4.toChar(),
                '\r',
                3.toChar(),
                4.toChar(),
                '\t',
                6.toChar(),
                '\n',
                1.toChar(),
                '\u000c',
                11.toChar(),
                0.toChar(),
                2.toChar(),
                5.toChar(),
                0.toChar(),
                '\r',
                14.toChar(),
                2.toChar(),
                '\b',
                15.toChar(),
                7.toChar(),
                4.toChar(),
                15.toChar(),
                1.toChar(),
                '\n',
                7.toChar(),
                5.toChar(),
                6.toChar(),
                '\u000c',
                11.toChar(),
                3.toChar(),
                '\b',
                '\t',
                14.toChar()
            ),
            charArrayOf(
                2.toChar(),
                4.toChar(),
                '\b',
                15.toChar(),
                7.toChar(),
                '\n',
                '\r',
                6.toChar(),
                4.toChar(),
                1.toChar(),
                3.toChar(),
                '\u000c',
                11.toChar(),
                7.toChar(),
                14.toChar(),
                0.toChar(),
                '\u000c',
                2.toChar(),
                5.toChar(),
                '\t',
                '\n',
                '\r',
                0.toChar(),
                3.toChar(),
                1.toChar(),
                11.toChar(),
                15.toChar(),
                5.toChar(),
                6.toChar(),
                '\b',
                '\t',
                14.toChar(),
                14.toChar(),
                11.toChar(),
                5.toChar(),
                6.toChar(),
                4.toChar(),
                1.toChar(),
                3.toChar(),
                '\n',
                2.toChar(),
                '\u000c',
                15.toChar(),
                0.toChar(),
                '\r',
                2.toChar(),
                '\b',
                5.toChar(),
                11.toChar(),
                '\b',
                0.toChar(),
                15.toChar(),
                7.toChar(),
                14.toChar(),
                '\t',
                4.toChar(),
                '\u000c',
                7.toChar(),
                '\n',
                '\t',
                1.toChar(),
                '\r',
                6.toChar(),
                3.toChar()
            ),
            charArrayOf(
                '\u000c',
                '\t',
                0.toChar(),
                7.toChar(),
                '\t',
                2.toChar(),
                14.toChar(),
                1.toChar(),
                '\n',
                15.toChar(),
                3.toChar(),
                4.toChar(),
                6.toChar(),
                '\u000c',
                5.toChar(),
                11.toChar(),
                1.toChar(),
                14.toChar(),
                '\r',
                0.toChar(),
                2.toChar(),
                '\b',
                7.toChar(),
                '\r',
                15.toChar(),
                5.toChar(),
                4.toChar(),
                '\n',
                '\b',
                3.toChar(),
                11.toChar(),
                6.toChar(),
                '\n',
                4.toChar(),
                6.toChar(),
                11.toChar(),
                7.toChar(),
                '\t',
                0.toChar(),
                6.toChar(),
                4.toChar(),
                2.toChar(),
                '\r',
                1.toChar(),
                '\t',
                15.toChar(),
                3.toChar(),
                '\b',
                15.toChar(),
                3.toChar(),
                1.toChar(),
                14.toChar(),
                '\u000c',
                5.toChar(),
                11.toChar(),
                0.toChar(),
                2.toChar(),
                '\u000c',
                14.toChar(),
                7.toChar(),
                5.toChar(),
                '\n',
                '\b',
                '\r'
            ),
            charArrayOf(
                4.toChar(),
                1.toChar(),
                3.toChar(),
                '\n',
                15.toChar(),
                '\u000c',
                5.toChar(),
                0.toChar(),
                2.toChar(),
                11.toChar(),
                '\t',
                6.toChar(),
                '\b',
                7.toChar(),
                6.toChar(),
                '\t',
                11.toChar(),
                4.toChar(),
                '\u000c',
                15.toChar(),
                0.toChar(),
                3.toChar(),
                '\n',
                5.toChar(),
                14.toChar(),
                '\r',
                7.toChar(),
                '\b',
                '\r',
                14.toChar(),
                1.toChar(),
                2.toChar(),
                '\r',
                6.toChar(),
                14.toChar(),
                '\t',
                4.toChar(),
                1.toChar(),
                2.toChar(),
                14.toChar(),
                11.toChar(),
                '\r',
                5.toChar(),
                0.toChar(),
                1.toChar(),
                '\n',
                '\b',
                3.toChar(),
                0.toChar(),
                11.toChar(),
                3.toChar(),
                5.toChar(),
                '\t',
                4.toChar(),
                15.toChar(),
                2.toChar(),
                7.toChar(),
                '\b',
                '\u000c',
                15.toChar(),
                '\n',
                7.toChar(),
                6.toChar(),
                '\u000c'
            ),
            charArrayOf(
                '\r',
                7.toChar(),
                '\n',
                0.toChar(),
                6.toChar(),
                '\t',
                5.toChar(),
                15.toChar(),
                '\b',
                4.toChar(),
                3.toChar(),
                '\n',
                11.toChar(),
                14.toChar(),
                '\u000c',
                5.toChar(),
                2.toChar(),
                11.toChar(),
                '\t',
                6.toChar(),
                15.toChar(),
                '\u000c',
                0.toChar(),
                3.toChar(),
                4.toChar(),
                1.toChar(),
                14.toChar(),
                '\r',
                1.toChar(),
                2.toChar(),
                7.toChar(),
                '\b',
                1.toChar(),
                2.toChar(),
                '\u000c',
                15.toChar(),
                '\n',
                4.toChar(),
                0.toChar(),
                3.toChar(),
                '\r',
                14.toChar(),
                6.toChar(),
                '\t',
                7.toChar(),
                '\b',
                '\t',
                6.toChar(),
                15.toChar(),
                1.toChar(),
                5.toChar(),
                '\u000c',
                3.toChar(),
                '\n',
                14.toChar(),
                5.toChar(),
                '\b',
                7.toChar(),
                11.toChar(),
                0.toChar(),
                4.toChar(),
                '\r',
                2.toChar(),
                11.toChar()
            )
        )

        /* renamed from: h  reason: collision with root package name */
        private val f4901h = intArrayOf(
            15,
            6,
            19,
            20,
            28,
            11,
            27,
            16,
            0,
            14,
            22,
            25,
            4,
            17,
            30,
            9,
            1,
            7,
            23,
            13,
            31,
            26,
            2,
            8,
            18,
            12,
            29,
            5,
            21,
            10,
            3,
            24
        )

        /* renamed from: i  reason: collision with root package name */
        private val f4902i = intArrayOf(
            39,
            7,
            47,
            15,
            55,
            23,
            63,
            31,
            38,
            6,
            46,
            14,
            54,
            22,
            62,
            30,
            37,
            5,
            45,
            13,
            53,
            21,
            61,
            29,
            36,
            4,
            44,
            12,
            52,
            20,
            60,
            28,
            35,
            3,
            43,
            11,
            51,
            19,
            59,
            27,
            34,
            2,
            42,
            10,
            50,
            18,
            58,
            26,
            33,
            1,
            41,
            9,
            49,
            17,
            57,
            25,
            32,
            0,
            40,
            8,
            48,
            16,
            56,
            24
        )
        private val j = intArrayOf(
            56,
            48,
            40,
            32,
            24,
            16,
            8,
            0,
            57,
            49,
            41,
            33,
            25,
            17,
            9,
            1,
            58,
            50,
            42,
            34,
            26,
            18,
            10,
            2,
            59,
            51,
            43,
            35,
            62,
            54,
            46,
            38,
            30,
            22,
            14,
            6,
            61,
            53,
            45,
            37,
            29,
            21,
            13,
            5,
            60,
            52,
            44,
            36,
            28,
            20,
            12,
            4,
            27,
            19,
            11,
            3
        )
        private val k = intArrayOf(
            13,
            16,
            10,
            23,
            0,
            4,
            -1,
            -1,
            2,
            27,
            14,
            5,
            20,
            9,
            -1,
            -1,
            22,
            18,
            11,
            3,
            25,
            7,
            -1,
            -1,
            15,
            6,
            26,
            19,
            12,
            1,
            -1,
            -1,
            40,
            51,
            30,
            36,
            46,
            54,
            -1,
            -1,
            29,
            39,
            50,
            44,
            32,
            47,
            -1,
            -1,
            43,
            48,
            38,
            55,
            33,
            52,
            -1,
            -1,
            45,
            41,
            49,
            35,
            28,
            31,
            -1,
            -1
        )
        private val l = intArrayOf(1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1)
        private val m = longArrayOf(0, 1048577, 3145731)
        private const val n = 0
        private const val o = 1
        private var p: Long = 0
        private var q: Long = 0
        private var r: Long = 0
        private val s = IntArray(2)
        private val t = ByteArray(8)
        private var u = 0
        private var v = 0
        private var w = 0
        private fun a(iArr: IntArray, i2: Int, j2: Long): Long {
            var j3: Long = 0
            for (i3 in 0 until i2) {
                if (iArr[i3] >= 0 && f4897d[iArr[i3]] and j2 != 0L) {
                    j3 = j3 or f4897d[i3]
                }
            }
            return j3
        }

        private fun a(j2: Long, jArr: LongArray, i2: Int) {
            var a2 = a(j, 56, j2)
            for (i3 in 0..15) {
                a2 = a2 and m[l[i3]].inv() shr l[i3] or (m[l[i3]] and a2 shl 28 - l[i3])
                jArr[i3] = a(k, 64, a2)
            }
            if (i2 == 1) {
                for (i4 in 0..7) {
                    val j3 = jArr[i4]
                    val i5 = 15 - i4
                    jArr[i4] = jArr[i5]
                    jArr[i5] = j3
                }
            }
        }

        private fun a(jArr: LongArray, j2: Long): Long {
            p = a(f4898e, 64, j2)
            s[0] = (p and 4294967295L).toInt()
            s[1] = (p and -4294967296L shr 32).toInt()
            for (i2 in 0..15) {
                r = s[1].toLong()
                r = a(f4899f, 64, r)
                r = r xor jArr[i2]
                for (i3 in 0..7) {
                    t[i3] = (255L and (r shr i3 * 8)).toInt().toByte()
                }
                u = 0
                var i4 = 7
                while (true) {
                    w = i4
                    if (w < 0) {
                        break
                    }
                    u = u shl 4
                    u = u or f4900g[w][t[w].toInt()].code
                    i4 = w - 1
                }
                r = u.toLong()
                r = a(f4901h, 32, r)
                q = s[0].toLong()
                s[0] = s[1]
                s[1] = (q xor r).toInt()
            }
            v = s[0]
            s[0] = s[1]
            s[1] = v
            p = s[0].toLong() and 4294967295L or (s[1].toLong() shl 32 and -4294967296L)
            p = a(f4902i, 64, p)
            return p
        }

        @Synchronized
        fun a(bArr: ByteArray, i2: Int, bArr2: ByteArray, i3: Int): ByteArray {
            val bArr3: ByteArray
            //        synchronized (d.class) {
            var j2: Long = 0
            for (i4 in 0..7) {
                j2 = j2 or (bArr2[i4].toLong() shl i4 * 8)
            }
            val i5 = i2 / 8
            val jArr = LongArray(16)
            for (i6 in 0..15) {
                jArr[i6] = 0
            }
            val jArr2 = LongArray(i5)
            for (i7 in 0 until i5) {
                for (i8 in 0..7) {
                    jArr2[i7] = bArr[i7 * 8 + i8].toLong() shl i8 * 8 or jArr2[i7]
                }
            }
            val jArr3 = LongArray(((i5 + 1) * 8 + 1) / 8)
            a(j2, jArr, 0)
            for (i9 in 0 until i5) {
                jArr3[i9] = a(jArr, jArr2[i9])
            }
            val i10 = i2 % 8
            val i11 = i5 * 8
            val i12 = i2 - i11
            val bArr4 = ByteArray(i12)
            System.arraycopy(bArr, i11, bArr4, 0, i12)
            var j3: Long = 0
            for (i13 in 0 until i10) {
                j3 = j3 or (bArr4[i13].toLong() shl i13 * 8)
            }
            jArr3[i5] = a(jArr, j3)
            bArr3 = ByteArray(jArr3.size * 8)
            var i14 = 0
            var i15 = 0
            while (i14 < jArr3.size) {
                var i16 = i15
                for (i17 in 0..7) {
                    bArr3[i16] = (255L and (jArr3[i14] shr i17 * 8)).toInt().toByte()
                    i16++
                }
                i14++
                i15 = i16
            }
            //        }
            return bArr3
        }

        @Synchronized
        fun b(bArr: ByteArray, i2: Int, bArr2: ByteArray, i3: Int): ByteArray {
            var bArr3: ByteArray
            synchronized(KW_Encrypt::class.java) {
                var j2: Long = 0
                for (i4 in 0..7) {
                    j2 = j2 or (bArr2[i4].toLong() shl i4 * 8)
                }
                val i5 = i2 / 8
                val jArr = LongArray(16)
                for (i6 in 0..15) {
                    jArr[i6] = 0
                }
                val jArr2 = LongArray(i5)
                for (i7 in 0 until i5) {
                    for (i8 in 0..7) {
                        jArr2[i7] =
                            (bArr[i7 * 8 + i8].toInt() and 255).toLong() shl i8 * 8 or jArr2[i7]
                    }
                }
                val jArr3 = LongArray(((i5 + 1) * 8 + 1) / 8)
                a(j2, jArr, 0)
                for (i9 in 0 until i5) {
                    jArr3[i9] = a(jArr, jArr2[i9])
                }
                val i10 = i2 % 8
                val i11 = i5 * 8
                val i12 = i2 - i11
                val bArr4 = ByteArray(i12)
                System.arraycopy(bArr, i11, bArr4, 0, i12)
                var j3: Long = 0
                for (i13 in 0 until i10) {
                    j3 = j3 or ((bArr4[i13].toInt() and 255).toLong() shl i13 * 8)
                }
                jArr3[i5] = a(jArr, j3)
                bArr3 = ByteArray(jArr3.size * 8)
                var i14 = 0
                var i15 = 0
                while (i14 < jArr3.size) {
                    var i16 = i15
                    for (i17 in 0..7) {
                        bArr3[i16] = (255L and (jArr3[i14] shr i17 * 8)).toInt().toByte()
                        i16++
                    }
                    i14++
                    i15 = i16
                }
            }
            return bArr3
        }

        @Synchronized
        fun a(bArr: ByteArray, bArr2: ByteArray): ByteArray {
            var j2: Long
            var bArr3: ByteArray
            synchronized(KW_Encrypt::class.java) {
                val length = bArr.size
                val length2 = bArr2.size
                val jArr = LongArray(16)
                var i2 = 0
                while (true) {
                    j2 = 0
                    if (i2 >= 16) {
                        break
                    }
                    jArr[i2] = 0
                    i2++
                }
                var j3: Long = 0
                for (i3 in 0..7) {
                    j3 = j3 or (bArr2[i3].toLong() shl i3 * 8)
                }
                a(j3, jArr, 0)
                val i4 = length / 8
                val jArr2 = LongArray(i4)
                for (i5 in 0 until i4) {
                    for (i6 in 0..7) {
                        jArr2[i5] =
                            jArr2[i5] or ((bArr[i5 * 8 + i6].toInt() and 255).toLong() shl i6 * 8)
                    }
                }
                val jArr3 = LongArray(((i4 + 1) * 8 + 1) / 8)
                for (i7 in 0 until i4) {
                    jArr3[i7] = a(jArr, jArr2[i7])
                }
                val i8 = length % 8
                val i9 = i4 * 8
                val i10 = length - i9
                val bArr4 = ByteArray(i10)
                System.arraycopy(bArr, i9, bArr4, 0, i10)
                for (i11 in 0 until i8) {
                    j2 = j2 or ((bArr4[i11].toInt() and 255).toLong() shl i11 * 8)
                }
                jArr3[i4] = a(jArr, j2)
                bArr3 = ByteArray(jArr3.size * 8)
                var i12 = 0
                var i13 = 0
                while (i12 < jArr3.size) {
                    var i14 = i13
                    for (i15 in 0..7) {
                        bArr3[i14] = (255L and (jArr3[i12] shr i15 * 8)).toInt().toByte()
                        i14++
                    }
                    i12++
                    i13 = i14
                }
            }
            return bArr3
        }

        @Synchronized
        fun b(bArr: ByteArray, bArr2: ByteArray): ByteArray {
            var bArr3: ByteArray
            synchronized(KW_Encrypt::class.java) {
                val length = bArr.size
                val length2 = bArr2.size
                val jArr = LongArray(16)
                var j2: Long = 0
                for (i2 in 0..7) {
                    j2 = j2 or (bArr2[i2].toLong() shl i2 * 8)
                }
                for (i3 in 0..15) {
                    jArr[i3] = 0
                }
                a(j2, jArr, 1)
                val i4 = length / 8
                val jArr2 = LongArray(i4)
                for (i5 in 0 until i4) {
                    for (i6 in 0..7) {
                        jArr2[i5] =
                            jArr2[i5] or ((bArr[i5 * 8 + i6].toInt() and 255).toLong() shl i6 * 8)
                    }
                }
                val jArr3 = LongArray(i4)
                for (i7 in 0 until i4) {
                    jArr3[i7] = a(jArr, jArr2[i7])
                }
                bArr3 = ByteArray(i4 * 8)
                for (i8 in 0 until i4) {
                    for (i9 in 0..7) {
                        bArr3[i8 * 8 + i9] = (255L and (jArr3[i8] shr i9 * 8)).toInt().toByte()
                    }
                }
            }
            return bArr3
        }
    }
}

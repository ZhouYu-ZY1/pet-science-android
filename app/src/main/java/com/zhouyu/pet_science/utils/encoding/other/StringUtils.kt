package com.zhouyu.pet_science.utils.encoding.other

import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object StringUtils {
    fun equals(cs1: CharSequence?, cs2: CharSequence?): Boolean {
        return if (cs1 === cs2) {
            true
        } else if (cs1 != null && cs2 != null) {
            if (cs1 is String && cs2 is String) {
                cs1 == cs2
            } else {
                cs1.length == cs2.length && CharSequenceUtils.regionMatches(
                    cs1,
                    false,
                    0,
                    cs2,
                    0,
                    cs1.length
                )
            }
        } else {
            false
        }
    }

    private fun getByteBuffer(string: String?, charset: Charset): ByteBuffer? {
        return if (string == null) null else ByteBuffer.wrap(string.toByteArray(charset))
    }

    fun getByteBufferUtf8(string: String?): ByteBuffer? {
        return getByteBuffer(string, StandardCharsets.UTF_8)
    }

    private fun getBytes(string: String?, charset: Charset): ByteArray? {
        return string?.toByteArray(charset)
    }

    fun getBytesIso8859_1(string: String?): ByteArray? {
        return getBytes(string, StandardCharsets.ISO_8859_1)
    }

    fun getBytesUnchecked(string: String?, charsetName: String): ByteArray? {
        return if (string == null) {
            null
        } else {
            try {
                string.toByteArray(charset(charsetName))
            } catch (var3: UnsupportedEncodingException) {
                throw newIllegalStateException(charsetName, var3)
            }
        }
    }

    fun getBytesUsAscii(string: String?): ByteArray? {
        return getBytes(string, StandardCharsets.US_ASCII)
    }

    fun getBytesUtf16(string: String?): ByteArray? {
        return getBytes(string, StandardCharsets.UTF_16)
    }

    fun getBytesUtf16Be(string: String?): ByteArray? {
        return getBytes(string, StandardCharsets.UTF_16BE)
    }

    fun getBytesUtf16Le(string: String?): ByteArray? {
        return getBytes(string, StandardCharsets.UTF_16LE)
    }

    @JvmStatic
    fun getBytesUtf8(string: String?): ByteArray? {
        return getBytes(string, StandardCharsets.UTF_8)
    }

    private fun newIllegalStateException(
        charsetName: String,
        e: UnsupportedEncodingException
    ): IllegalStateException {
        return IllegalStateException("$charsetName: $e")
    }

    private fun newString(bytes: ByteArray?, charset: Charset): String? {
        return if (bytes == null) null else String(bytes, charset)
    }

    fun newString(bytes: ByteArray?, charsetName: String): String? {
        return if (bytes == null) {
            null
        } else {
            try {
                String(bytes, charset(charsetName))
            } catch (var3: UnsupportedEncodingException) {
                throw newIllegalStateException(charsetName, var3)
            }
        }
    }

    fun newStringIso8859_1(bytes: ByteArray?): String? {
        return newString(bytes, StandardCharsets.ISO_8859_1)
    }

    fun newStringUsAscii(bytes: ByteArray?): String? {
        return newString(bytes, StandardCharsets.US_ASCII)
    }

    fun newStringUtf16(bytes: ByteArray?): String? {
        return newString(bytes, StandardCharsets.UTF_16)
    }

    fun newStringUtf16Be(bytes: ByteArray?): String? {
        return newString(bytes, StandardCharsets.UTF_16BE)
    }

    fun newStringUtf16Le(bytes: ByteArray?): String? {
        return newString(bytes, StandardCharsets.UTF_16LE)
    }

    fun newStringUtf8(bytes: ByteArray?): String? {
        return newString(bytes, StandardCharsets.UTF_8)
    }
}
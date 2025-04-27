package com.zhouyu.pet_science.utils.encoding.other

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class Hex : BinaryEncoder, BinaryDecoder {
    val charset: Charset?

    constructor() {
        charset = DEFAULT_CHARSET
    }

    constructor(charset: Charset?) {
        this.charset = charset
    }

    constructor(charsetName: String?) : this(Charset.forName(charsetName))

    override fun decode(var1: ByteArray): ByteArray? {
        try {
            return decodeHex(
                String(
                    var1, charset!!
                ).toCharArray()
            )
        } catch (e: DecoderException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(DecoderException::class)
    fun decode(buffer: ByteBuffer): ByteArray {
        return decodeHex(
            String(toByteArray(buffer), charset!!)
                .toCharArray()
        )
    }

    override fun decode(var1: Any): Any? {
        try {
            return if (var1 is String) {
                this.decode(var1.toCharArray() as Any)
            } else if (var1 is ByteArray) {
                this.decode(var1)
            } else if (var1 is ByteBuffer) {
                this.decode(var1)
            } else {
                try {
                    decodeHex(var1 as CharArray)
                } catch (var3: ClassCastException) {
                    throw DecoderException(var3.message, var3)
                }
            }
        } catch (e: DecoderException) {
            e.printStackTrace()
        }
        return null
    }


    override fun encode(array: ByteArray?): ByteArray {
        return encodeHexString(array).toByteArray(
            charset!!
        )
    }

    fun encode(array: ByteBuffer): ByteArray {
        return encodeHexString(array).toByteArray(
            charset!!
        )
    }

    override fun encode(var1: Any?): Any {
        var byteArray: ByteArray? = null
        if (var1 is String) {
            byteArray = var1.toByteArray(charset!!)
        } else if (var1 is ByteBuffer) {
            byteArray = toByteArray(
                var1
            )
        } else {
            try {
                byteArray = var1 as ByteArray?
            } catch (var4: ClassCastException) {
                try {
                    throw EncoderException(var4.message, var4)
                } catch (e: EncoderException) {
                    e.printStackTrace()
                }
            }
        }
        return encodeHex(byteArray)
    }

    val charsetName: String
        get() = charset!!.name()

    override fun toString(): String {
        return super.toString() + "[charsetName=" + charset + "]"
    }

    companion object {
        var DEFAULT_CHARSET: Charset? = null
        const val DEFAULT_CHARSET_NAME = "UTF-8"
        private val DIGITS_LOWER: CharArray
        private val DIGITS_UPPER: CharArray
        @Throws(DecoderException::class)
        fun decodeHex(data: CharArray): ByteArray {
            val out = ByteArray(data.size shr 1)
            decodeHex(data, out, 0)
            return out
        }

        @Throws(DecoderException::class)
        fun decodeHex(data: CharArray, out: ByteArray, outOffset: Int): Int {
            val len = data.size
            return if (len and 1 != 0) {
                throw DecoderException("Odd number of characters.")
            } else {
                val outLen = len shr 1
                if (out.size - outOffset < outLen) {
                    throw DecoderException("Output array is not large enough to accommodate decoded data.")
                } else {
                    var i = outOffset
                    var j = 0
                    while (j < len) {
                        var f =
                            toDigit(
                                data[j],
                                j
                            ) shl 4
                        ++j
                        f = f or toDigit(
                            data[j],
                            j
                        )
                        ++j
                        out[i] = (f and 255).toByte()
                        ++i
                    }
                    outLen
                }
            }
        }

        @Throws(DecoderException::class)
        fun decodeHex(data: String): ByteArray {
            return decodeHex(data.toCharArray())
        }

        @JvmOverloads
        fun encodeHex(data: ByteArray?, toLowerCase: Boolean = true): CharArray {
            return encodeHex(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
        }

        protected fun encodeHex(data: ByteArray?, toDigits: CharArray): CharArray {
            val l = data!!.size
            val out = CharArray(l shl 1)
            encodeHex(data, 0, data.size, toDigits, out, 0)
            return out
        }

        fun encodeHex(
            data: ByteArray?,
            dataOffset: Int,
            dataLen: Int,
            toLowerCase: Boolean
        ): CharArray {
            val out = CharArray(dataLen shl 1)
            encodeHex(
                data,
                dataOffset,
                dataLen,
                if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER,
                out,
                0
            )
            return out
        }

        fun encodeHex(
            data: ByteArray?,
            dataOffset: Int,
            dataLen: Int,
            toLowerCase: Boolean,
            out: CharArray,
            outOffset: Int
        ) {
            encodeHex(
                data,
                dataOffset,
                dataLen,
                if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER,
                out,
                outOffset
            )
        }

        private fun encodeHex(
            data: ByteArray?,
            dataOffset: Int,
            dataLen: Int,
            toDigits: CharArray,
            out: CharArray,
            outOffset: Int
        ) {
            var i = dataOffset
            var var7 = outOffset
            while (i < dataOffset + dataLen) {
                out[var7++] = toDigits[240 and data!![i].toInt() ushr 4]
                out[var7++] = toDigits[15 and data[i].toInt()]
                ++i
            }
        }

        @JvmOverloads
        fun encodeHex(data: ByteBuffer, toLowerCase: Boolean = true): CharArray {
            return encodeHex(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
        }

        protected fun encodeHex(byteBuffer: ByteBuffer, toDigits: CharArray): CharArray {
            return encodeHex(toByteArray(byteBuffer), toDigits)
        }

        @JvmStatic
        fun encodeHexString(data: ByteArray?): String {
            return String(encodeHex(data))
        }

        fun encodeHexString(data: ByteArray?, toLowerCase: Boolean): String {
            return String(encodeHex(data, toLowerCase))
        }

        fun encodeHexString(data: ByteBuffer): String {
            return String(encodeHex(data))
        }

        fun encodeHexString(data: ByteBuffer, toLowerCase: Boolean): String {
            return String(encodeHex(data, toLowerCase))
        }

        private fun toByteArray(byteBuffer: ByteBuffer): ByteArray {
            val remaining = byteBuffer.remaining()
            var byteArray: ByteArray
            if (byteBuffer.hasArray()) {
                byteArray = byteBuffer.array()
                if (remaining == byteArray.size) {
                    byteBuffer.position(remaining)
                    return byteArray
                }
            }
            byteArray = ByteArray(remaining)
            byteBuffer[byteArray]
            return byteArray
        }

        @Throws(DecoderException::class)
        protected fun toDigit(ch: Char, index: Int): Int {
            val digit = ch.digitToIntOrNull(16) ?: -1
            return if (digit == -1) {
                throw DecoderException("Illegal hexadecimal character $ch at index $index")
            } else {
                digit
            }
        }

        init {
            DEFAULT_CHARSET = StandardCharsets.UTF_8
            DIGITS_LOWER = charArrayOf(
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                'a',
                'b',
                'c',
                'd',
                'e',
                'f'
            )
            DIGITS_UPPER = charArrayOf(
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                'A',
                'B',
                'C',
                'D',
                'E',
                'F'
            )
        }
    }
}

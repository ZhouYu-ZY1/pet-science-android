package com.zhouyu.pet_science.utils.encoding.other

interface BinaryEncoder : Encoder {
    @Throws(EncoderException::class)
    fun encode(var1: ByteArray?): ByteArray
}

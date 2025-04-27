package com.zhouyu.pet_science.utils.encoding.other

interface BinaryDecoder : Decoder {
    @Throws(DecoderException::class)
    fun decode(var1: ByteArray): ByteArray?
}

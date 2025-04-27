package com.zhouyu.pet_science.utils.encoding.other

interface Decoder {
    @Throws(DecoderException::class)
    fun decode(var1: Any): Any?
}

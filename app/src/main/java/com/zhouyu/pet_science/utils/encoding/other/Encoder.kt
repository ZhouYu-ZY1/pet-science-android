package com.zhouyu.pet_science.utils.encoding.other

interface Encoder {
    @Throws(EncoderException::class)
    fun encode(var1: Any?): Any
}
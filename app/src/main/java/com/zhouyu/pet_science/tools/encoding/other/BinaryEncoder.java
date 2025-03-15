package com.zhouyu.pet_science.tools.encoding.other;


public interface BinaryEncoder extends Encoder {
    byte[] encode(byte[] var1) throws EncoderException;
}

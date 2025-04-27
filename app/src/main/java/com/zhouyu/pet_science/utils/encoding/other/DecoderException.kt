package com.zhouyu.pet_science.utils.encoding.other

class DecoderException : Exception {
    constructor()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

    companion object {
        private const val serialVersionUID = 1L
    }
}

package com.zhouyu.pet_science.utils.encoding.other

object CharSequenceUtils {
    fun regionMatches(
        cs: CharSequence,
        ignoreCase: Boolean,
        thisStart: Int,
        substring: CharSequence,
        start: Int,
        length: Int
    ): Boolean {
        return if (cs is String && substring is String) {
            cs.regionMatches(
                thisStart,
                substring,
                start,
                length,
                ignoreCase = ignoreCase
            )
        } else {
            var index1 = thisStart
            var index2 = start
            var var8 = length
            while (var8-- > 0) {
                val c1 = cs[index1++]
                val c2 = substring[index2++]
                if (c1 != c2) {
                    if (!ignoreCase) {
                        return false
                    }
                    if (c1.uppercaseChar() != c2.uppercaseChar() && c1.lowercaseChar() != c2.lowercaseChar()) {
                        return false
                    }
                }
            }
            true
        }
    }
}

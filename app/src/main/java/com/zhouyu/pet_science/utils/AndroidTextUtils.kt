package com.zhouyu.pet_science.utils

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.annotation.ColorInt
import com.zhouyu.pet_science.application.Application
import net.sourceforge.pinyin4j.PinyinHelper
import java.util.Calendar
import java.util.Date
import java.util.Random
import java.util.regex.Pattern

object AndroidTextUtils {
    /**
     * 设置文字样式
     * @return
     */
    fun setTextStyle(
        text: String?,
        dpSize: Int,
        isBold: Boolean,
        isUnderline: Boolean
    ): CharSequence {
        val spannableString = SpannableString(text)

        //设置字体大小
        spannableString.setSpan(
            AbsoluteSizeSpan(dpSize, true),
            0,
            spannableString.length,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        //设置加粗
        if (isBold) {
            val boldSpan = StyleSpan(Typeface.BOLD)
            spannableString.setSpan(
                boldSpan,
                0,
                spannableString.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        //设置下划线
        if (isUnderline) {
            spannableString.setSpan(
                UnderlineSpan(),
                0,
                spannableString.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        return spannableString
    }

    // 将中文字符串转换为拼音字符串
    fun convertToPinyin(chinese: String): String {
        val pinyin = StringBuilder()
        for (element in chinese) {
            val pyArray = PinyinHelper.toHanyuPinyinStringArray(element)
            if (pyArray != null) {
                pinyin.append(pyArray.contentToString())
                pinyin.append(" ") //使用空格分割每个拼音
            }
        }
        if (pinyin.isNotEmpty()) {
            pinyin.deleteCharAt(pinyin.length - 1) // 移除最后一个空格
        }
        return pinyin.toString()
    }

    /**
     * 改变特定文字的颜色
     */
    fun changeFindTextColor(
        text: String,
        keyword: String,
        @ColorInt color: Int
    ): SpannableString {
        val ss = SpannableString(text)
        try {
            val pattern = Pattern.compile(keyword)
            val matcher = pattern.matcher(ss)
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                ss.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(text) && text.contains(keyword)) {
//            int start = text.indexOf(keyword);
//            int end = start + keyword.length();
//            ss.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
        return ss
    }

    /**
     * 解析出url参数中的键值对
     */
    fun urlParse(url: String): Map<String, String> {
        val mapRequest: MutableMap<String, String> = HashMap()
        val arrSplit: Array<String>
        val strUrlParam = truncateUrlPage(url) ?: return mapRequest
        arrSplit = strUrlParam.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (strSplit in arrSplit) {
            val arrSplitEqual: Array<String> =
                strSplit.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            //解析出键值
            if (arrSplitEqual.size > 1) {
                //正确解析
                mapRequest[arrSplitEqual[0]] = arrSplitEqual[1]
            } else {
                if (arrSplitEqual[0] != "") {
                    mapRequest[arrSplitEqual[0]] = ""
                }
            }
        }
        return mapRequest
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     */
    private fun truncateUrlPage(strURL: String): String? {
        var strAllParam: String? = null
        val arrSplit: Array<String>
        val trimmedURL = strURL.trim { it <= ' ' }
        arrSplit = trimmedURL.split("[?]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (trimmedURL.length > 1) {
            if (arrSplit.size > 1) {
                for (i in 1 until arrSplit.size) {
                    strAllParam = arrSplit[i]
                }
            }
        }
        return strAllParam
    }

    /**
     * 从文本中提取url
     */
    fun findUrls(url: String): List<String> {
        val result: MutableList<String> = ArrayList()
        val pattern = Pattern.compile(
            "\\b(((ht|f)tp(s?)://|~/|/)|www.)" +
                    "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +
                    "|mil|biz|info|mobi|name|aero|jobs|museum" +
                    "|travel|[a-z]{2}))(:\\d{1,5})?" +
                    "(((/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|/)+|\\?|#)?" +
                    "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
                    "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
                    "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
                    "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
                    "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b"
        )
        val matcher = pattern.matcher(url)
        while (matcher.find()) {
            result.add(matcher.group())
        }
        return result
    }

    /**
     * 获取cookie的值
     * 类似这样的字符串：
     * c=1;b=12;a=123
     */
    fun getCookieValue(cookies: String, key: String): String? {
        val pattern = Pattern.compile("$key\\s*=\\s*([^;\\s]+)")
        val matcher = pattern.matcher(cookies)
        if (matcher.find()) {
            if (matcher.groupCount() == 1) {
                return matcher.group(1)
            }
        }
        return null
    }

    fun setCookieValue(cookies: String, key: String, value: String): String {
        return cookies.replaceFirst("$key\\s*=\\s*([^;\\s]+)".toRegex(), "$key = $value")
    }

    //判断文本是否为空
    @JvmStatic
    fun isEmpty(str: String?): Boolean {
        return str == null || str.trim { it <= ' ' } == "" || str.trim { it <= ' ' } == "null"
    }

    /**
     * 获取当前日期是星期几
     */
    fun getWeekOfCalendar(calendar: Calendar): String {
        val weekDays = arrayOf("星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        var w = calendar[Calendar.DAY_OF_WEEK] - 1
        if (w < 0) {
            w = 0
        }
        return weekDays[w]
    }

    val todayRemainSecondNum: Long
        /**
         * 获取当天剩余的时间，单位：秒
         * @return
         */
        get() {
            val current = System.currentTimeMillis() //当前时间毫秒数
            val calendar = Calendar.getInstance()
            calendar.time = Date(current)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            val tomorrowZero = calendar.timeInMillis
            return tomorrowZero - current
        }

    /**
     * 生成随机数字和字母组合
     */
    fun getRandomNickname(length: Int): String {
        val `val` = StringBuilder()
        val random = Random()
        for (i in 0 until length) {
            // 输出字母还是数字
            val charOrNum = if (random.nextInt(2) % 2 == 0) "char" else "num"
            // 字符串
            if ("char".equals(charOrNum, ignoreCase = true)) {
                // 取得大写字母还是小写字母
                val choice = if (random.nextInt(2) % 2 == 0) 65 else 97
                `val`.append((choice + random.nextInt(26)).toChar())
            } else { // 数字
                `val`.append(random.nextInt(10))
            }
        }
        return `val`.toString()
    }

    val copyContent: String
        /**
         * 获取复制内容
         */
        get() {
            var content = ""
            val clipboardManager =
                Application.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 获取剪贴板的剪贴数据集
            val clipData = clipboardManager.primaryClip
            if (null != clipData && clipData.itemCount > 0) {
                // 从数据集中获取第一条文本数据
                val item = clipData.getItemAt(0)
                if (null != item) {
                    content = item.text.toString()
                }
            }
            return content
        }

    /*生成numSize位16进制的数*/
    fun getRandomValue(numSize: Int): String {
        var str = ""
        for (i in 0 until numSize) {
            var temp = 0.toChar()
            val key = (Math.random() * 2).toInt()
            when (key) {
                0 -> temp = (Math.random() * 10 + 48).toInt().toChar() //产生随机数字
                1 -> temp = (Math.random() * 6 + 'a'.code.toDouble()).toInt().toChar() //产生a-f
                else -> {}
            }
            str += temp
        }
        return str
    }
}

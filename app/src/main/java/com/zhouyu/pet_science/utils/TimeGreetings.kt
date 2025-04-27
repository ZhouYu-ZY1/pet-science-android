package com.zhouyu.pet_science.utils

import android.annotation.SuppressLint
import android.widget.Toast
import com.zhouyu.pet_science.utils.MyToast.Companion.show
import java.util.Calendar

class TimeGreetings {
    var calendar = Calendar.getInstance()
    @SuppressLint("ShowToast")
    fun showGreetings() {
        val startStr = ""
        //        if(!Application.loginInformation.isNotLogin()){
//            startStr = "@"+Application.loginInformation.getNickname()+"，";
//        }
        val endStr = endStr
        val h = calendar[Calendar.HOUR_OF_DAY]
        if (h < 5) {
            show(startStr + "现在是休息时间，记得早点休息哦~", Toast.LENGTH_LONG)
        } else if (h < 9) {
            show(startStr + "早上好" + endStr + "~", Toast.LENGTH_LONG)
        } else if (h < 11) {
            show(startStr + "上午好" + endStr + "~", Toast.LENGTH_LONG)
        } else if (h < 13) {
            show(startStr + "中午好" + endStr + "~", Toast.LENGTH_LONG)
        } else if (h < 17) {
            show(startStr + "下午好" + endStr + "~", Toast.LENGTH_LONG)
        } else if (h < 20) {
            show(startStr + "Hi" + endStr + "~", Toast.LENGTH_LONG)
        } else if (h < 22) {
            show(startStr + "晚上好" + endStr + "~", Toast.LENGTH_LONG)
        } else {
            show(startStr + "夜深了，记得早点休息哦~", Toast.LENGTH_LONG)
        }
    }

    private val endStr: String
        private get() {
            var endStr = ""
            try {
                val month = calendar[Calendar.MONTH] + 1
                val day_Month = calendar[Calendar.DAY_OF_MONTH]
                when (month) {
                    1 -> {
                        if (day_Month == 1) {
                            endStr = "，祝您元旦节快乐"
                        }
                    }

                    5 -> {
                        if (day_Month == 1) {
                            endStr = "，祝您五一劳动节快乐"
                        }
                    }

                    6 -> {
                        if (day_Month == 1) {
                            endStr = "，祝您六一快乐"
                        }
                    }

                    9 -> {
                        if (day_Month == 10) {
                            endStr = "，祝您教师节快乐"
                        }
                    }

                    10 -> {
                        if (day_Month == 1) {
                            endStr = "，祝愿祖国繁荣昌盛，国庆节快乐"
                        }
                    }
                }
                val lunarUtil = LunarUtils(calendar)
                val lunarDay = lunarUtil.lunarDay
                when (lunarUtil.lunarMonth) {
                    "十二" -> {
                        if (lunarDay == "廿三" || lunarDay == "廿四") {
                            endStr = "，祝您小年快乐"
                        }
                    }

                    "一" -> {
                        if (lunarDay == "初一") {
                            endStr = "，祝您新年快乐"
                        } else if (lunarDay == "十五") {
                            endStr = "，祝您元宵节快乐"
                        }
                    }

                    "五" -> {
                        if (lunarDay == "初五") {
                            endStr = "，祝您端午安康"
                        }
                    }

                    "八" -> {
                        if (lunarDay == "十五") {
                            endStr = "，祝您中秋节快乐"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return endStr
        }

    companion object {
        /**
         * 获取节日
         */
        fun getFestival(calendar: Calendar): String {
            var endStr = ""
            try {
                val month = calendar[Calendar.MONTH] + 1
                val day_Month = calendar[Calendar.DAY_OF_MONTH]
                when (month) {
                    1 -> {
                        if (day_Month == 1) {
                            endStr = "元旦节"
                        }
                    }

                    5 -> {
                        if (day_Month == 1) {
                            endStr = "劳动节"
                        }
                    }

                    9 -> {
                        if (day_Month == 10) {
                            endStr = "教师节"
                        }
                    }

                    10 -> {
                        if (day_Month == 1) {
                            endStr = "国庆节"
                        }
                    }
                }
                val lunarUtil = LunarUtils(calendar)
                val lunarDay = lunarUtil.lunarDay
                when (lunarUtil.lunarMonth) {
                    "十二" -> {
                        if (lunarDay == "廿三" || lunarDay == "廿四") {
                            endStr = "小年"
                        }
                    }

                    "一" -> {
                        if (lunarDay == "初一") {
                            endStr = "新年"
                        } else if (lunarDay == "十五") {
                            endStr = "元宵节"
                        }
                    }

                    "五" -> {
                        if (lunarDay == "初五") {
                            endStr = "端午节"
                        }
                    }

                    "七" -> {
                        if (lunarDay == "初七") {
                            endStr = "七夕节"
                        }
                    }

                    "八" -> {
                        if (lunarDay == "十五") {
                            endStr = "中秋节"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return endStr
        }
    }
}

package com.zhouyu.pet_science.fragments

import android.view.View
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    var layoutView: View? = null
    fun <T : View?> findViewById(id: Int): T {
        return layoutView!!.findViewById(id)
    }

    fun runUiThread(runnable: Runnable?) {
        val activity = activity
        activity?.runOnUiThread(runnable)
    }

    fun executeThread(runnable: Runnable): Runnable {
        try {
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return runnable
    }
}

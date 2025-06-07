package com.zhouyu.pet_science.fragments

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zhouyu.pet_science.utils.PhoneMessage

open class BaseFragment : Fragment() {
    private var layoutView: View? = null
    fun <T : View?> findViewById(id: Int): T {
        return layoutView!!.findViewById(id)
    }
    fun runUiThread(runnable: Runnable?) {
        if(activity == null || activity?.isFinishing == true || activity?.isDestroyed == true){
            return
        }
        if(isDetached){
            return
        }
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

    fun setTopBarView(view: View, isPadding: Boolean) {
        if (isPadding) {
            view.setPadding(
                view.paddingLeft,
                PhoneMessage.statusBarHeight,
                view.paddingRight,
                view.paddingBottom
            )
        } else {
            val layoutParams = view.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.topMargin = PhoneMessage.statusBarHeight
                view.layoutParams = layoutParams
            }
        }
    }

    /**
     * 检查Fragment是否可见
     */
    fun isFragmentVisible(): Boolean {
        return isAdded && isVisible && userVisibleHint
    }
}

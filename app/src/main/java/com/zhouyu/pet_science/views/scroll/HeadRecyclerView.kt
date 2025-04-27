package com.zhouyu.pet_science.views.scroll

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by caizhiming on 2015/12/29.
 */
class HeadRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {
    private val mHeaderViews = ArrayList<View>()
    private val mFooterViews = ArrayList<View>()
    private var mAdapter: Adapter<*>? = null
    private var mWrapAdapter: Adapter<*>? = null
    private fun init(context: Context) {}
    override fun setAdapter(adapter: Adapter<*>?) {
        mAdapter = adapter
        mWrapAdapter = WrapAdapter(mHeaderViews, mFooterViews, adapter)
        super.setAdapter(mWrapAdapter)
        mAdapter!!.registerAdapterDataObserver(mDataObserver)
    }

    fun addHeaderView(view: View) {
        mHeaderViews.clear()
        mHeaderViews.add(view)
    }

    fun addFooterView(view: View) {
        mFooterViews.clear()
        mFooterViews.add(view)
    }

    val headerViewsCount: Int
        get() = mHeaderViews.size
    val footerViewsCount: Int
        get() = mFooterViews.size
    private val mDataObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            mWrapAdapter!!.notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            mWrapAdapter!!.notifyItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            mWrapAdapter!!.notifyItemRangeChanged(positionStart, itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            mWrapAdapter!!.notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            mWrapAdapter!!.notifyItemMoved(fromPosition, toPosition)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            mWrapAdapter!!.notifyItemRangeRemoved(positionStart, itemCount)
        }
    }

    init {
        init(context)
    }

    private inner class WrapAdapter(
        private val mHeaderViews: List<View>,
        private val mFooterViews: List<View>,
        private val mAdapter: Adapter<*>?
    ) : Adapter<ViewHolder>() {
        val headerCount: Int
            get() = this.mHeaderViews.size
        val footerCount: Int
            get() = this.mFooterViews.size

        fun isHeader(position: Int): Boolean {
            return position >= 0 && position < this.mHeaderViews.size
        }

        fun isFooter(position: Int): Boolean {
            return position < itemCount && position >= itemCount - this.mFooterViews.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return if (viewType == TYPE_HEADER) {
                CustomViewHolder(this.mHeaderViews[0])
            } else if (viewType == TYPE_FOOTER) {
                CustomViewHolder(this.mFooterViews[0])
            } else {
                this.mAdapter!!.onCreateViewHolder(parent, viewType)
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (isHeader(position)) return
            if (isFooter(position)) return
            val rePosition: Int = position - headerCount
            val itemCount = this.mAdapter!!.itemCount
            if (rePosition < itemCount) {
                this.mAdapter.onBindViewHolder(holder as Nothing, rePosition)
            }
        }

        override fun getItemId(position: Int): Long {
            if (this.mAdapter != null && position >= headerCount) {
                val rePosition: Int = position - headerCount
                val itemCount = this.mAdapter.itemCount
                if (rePosition < itemCount) {
                    return this.mAdapter.getItemId(rePosition)
                }
            }
            return -1
        }

        override fun getItemViewType(position: Int): Int {
            if (isHeader(position)) {
                return TYPE_HEADER
            }
            if (isFooter(position)) {
                return TYPE_FOOTER
            }
            val rePosition: Int = position - headerCount
            val itemCount = this.mAdapter!!.itemCount
            return if (rePosition < itemCount) {
                this.mAdapter.getItemViewType(position - headerCount)
            } else TYPE_LIST_ITEM
        }

        override fun getItemCount(): Int {
            return if (this.mAdapter != null) {
                headerCount + footerCount + this.mAdapter.itemCount
            } else {
                headerCount + footerCount
            }
        }

        override fun registerAdapterDataObserver(observer: AdapterDataObserver) {
            if (this.mAdapter != null) {
                this.mAdapter.registerAdapterDataObserver(observer)
            }
        }

        override fun unregisterAdapterDataObserver(observer: AdapterDataObserver) {
            if (this.mAdapter != null) {
                this.mAdapter.unregisterAdapterDataObserver(observer)
            }
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            when (holder.itemViewType) {
                TYPE_HEADER -> {
                    super.onViewDetachedFromWindow(holder)
                }
                TYPE_FOOTER -> {
                    super.onViewDetachedFromWindow(holder)
                }
                else -> {
                    this.mAdapter!!.onViewDetachedFromWindow(holder as Nothing)
                }
            }
        }

        private inner class CustomViewHolder(itemView: View?) : ViewHolder(
            itemView!!
        )
    }

    companion object {
        private const val TYPE_HEADER = -101
        private const val TYPE_FOOTER = -102
        private const val TYPE_LIST_ITEM = -103
    }
}

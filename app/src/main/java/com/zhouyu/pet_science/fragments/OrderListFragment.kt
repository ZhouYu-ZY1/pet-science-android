package com.zhouyu.pet_science.fragments

import PageResult
import Product
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.OrderDetailActivity
import com.zhouyu.pet_science.activities.OrderPayActivity
import com.zhouyu.pet_science.activities.ProductDetailActivity
import com.zhouyu.pet_science.adapter.OrderAdapter
import com.zhouyu.pet_science.model.Order
import com.zhouyu.pet_science.model.Result
import com.zhouyu.pet_science.network.OrderHttpUtils
import com.zhouyu.pet_science.utils.ConsoleUtils
import com.zhouyu.pet_science.utils.MyToast
import com.zhouyu.pet_science.views.dialog.MyDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderListFragment : Fragment(), OrderAdapter.OrderActionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: View
    private lateinit var progressBar: View
    private lateinit var orderAdapter: OrderAdapter
    private var status: String = ""
    private var pageNum = 1
    private val pageSize = 10
    private var isLoading = false
    private var hasMoreData = true

    companion object {
        private const val ARG_STATUS = "status"

        fun newInstance(status: String): OrderListFragment {
            val fragment = OrderListFragment()
            val args = Bundle()
            args.putString(ARG_STATUS, status)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            status = it.getString(ARG_STATUS, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_list, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        emptyView = view.findViewById(R.id.emptyView)
        progressBar = view.findViewById(R.id.progressBar)
        
        setupRecyclerView()
        setupSwipeRefresh()
        setupEmptyView()
        
        loadOrders(true)
        
        return view
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(this)
        recyclerView.adapter = orderAdapter
        
        // 添加滚动监听来实现加载更多
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                if (!isLoading && hasMoreData && !recyclerView.canScrollVertically(1)) {
                    loadOrders(false)
                }
            }
        })
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.Theme)
        swipeRefreshLayout.setOnRefreshListener {
            // 下拉刷新
            pageNum = 1
            hasMoreData = true
            loadOrders(true)
        }
    }

    private fun setupEmptyView() {
        // 空视图的"去逛逛"按钮点击事件
        emptyView.findViewById<Button>(R.id.btnGoShopping).setOnClickListener {
            // 跳转到商品列表页面
            activity?.finish()
        }
    }

    private fun loadOrders(isRefresh: Boolean) {
        if (isLoading) return
        isLoading = true
        
        if (isRefresh) {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.GONE
        }
        
        val params = HashMap<String, Any>()
        if (status.isNotEmpty()) {
            params["status"] = status
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    OrderHttpUtils.getOrderList(pageNum, pageSize, params)
                }
                
                processOrderResult(result, isRefresh)
            } catch (e: Exception) {
                e.printStackTrace()
                handleError(isRefresh)
            } finally {
                isLoading = false
                swipeRefreshLayout.isRefreshing = false
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun processOrderResult(result: Result<PageResult<Order>>?, isRefresh: Boolean) {
        if (result?.data != null) {
            val orderList = result.data.list
            
            // 如果刷新则清空之前的数据
            if (isRefresh) {
                orderAdapter.clearOrders()
            }
            
            if (orderList.isNotEmpty()) {
                orderAdapter.addOrders(orderList)
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
                
                // 判断是否有更多数据
                hasMoreData = orderList.size >= pageSize
                
                // 如果有更多数据，页码加1
                if (hasMoreData) {
                    pageNum++
                }
            } else if (isRefresh) {
                // 如果是刷新且没有数据，显示空视图
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            }
        } else {
            handleError(isRefresh)
        }
    }

    private fun handleError(isRefresh: Boolean) {
        if (isRefresh) {
            // 如果是刷新出错，显示空视图
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        }
        // 可以添加错误提示
    }

    // 订单操作监听实现
    override fun onOrderClick(order: Order) {
        // 点击订单跳转到详情页
        val intent = Intent(activity, OrderDetailActivity::class.java)
        intent.putExtra("orderId", order.orderId)
        startActivity(intent)
    }

    override fun onPrimaryButtonClick(order: Order, btn: Button) {
        when (order.status) {
            "pending" -> {
                // 跳转到支付页面
                val intent = Intent(activity, OrderPayActivity::class.java)
                intent.putExtra("orderId", order.orderId)
                intent.putExtra("orderNo", order.orderNo)
                intent.putExtra("amount", order.totalAmount)
                startActivity(intent)
            }
            "paid" -> {
                // 提醒发货
                // TODO: 实现提醒发货功能
                MyToast.show( "已提醒商家发货",true)
            }
            "shipped" -> {
                // 确认收货
                confirmReceipt(order.orderId)
            }
            "completed" -> {
                // 评价
                // TODO: 实现评价功能
            }
        }
    }

    override fun onSecondaryButtonClick(order: Order, btn: Button) {
        when (order.status) {
            "pending" -> {
                // 取消订单
                cancelOrder(order.orderId)
            }
            "paid", "shipped" -> {
                // 查看订单详情
                val intent = Intent(activity, OrderDetailActivity::class.java)
                intent.putExtra("orderId", order.orderId)
                startActivity(intent)
            }
            "completed" -> {
                // 再次购买
                startActivity(Intent(requireActivity(), ProductDetailActivity::class.java).apply {
                    order.orderItem?.apply {
                        ProductDetailActivity.product = Product(productId,productName,"",price,1,"","",productImage,"2.3万",1,"","")
                    }
                })
            }
        }
    }

    // 取消订单
    private fun cancelOrder(orderId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    OrderHttpUtils.updateOrderStatus(orderId, "cancelled")
                }
                
                if (result?.code == 200) {
                    // 重新加载订单列表
                    pageNum = 1
                    loadOrders(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 确认收货
    private fun confirmReceipt(orderId: Int) {
        MyDialog(requireActivity()).apply {
            setTitle("确认收货"); setMessage("您确认已收到商品吗？")
            setYesOnclickListener("确认") {
                dismiss()
                // 确认收货
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            OrderHttpUtils.completeOrder(orderId)
                        }
                        if (result?.code == 200) {
                            // 重新加载订单列表
                            pageNum = 1
                            loadOrders(true)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            setNoOnclickListener("取消") { dismiss() };show()
        }
    }
} 
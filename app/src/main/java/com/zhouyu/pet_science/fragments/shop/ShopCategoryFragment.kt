package com.zhouyu.pet_science.fragments.shop

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.fragments.BaseFragment

class ShopCategoryFragment : BaseFragment() {
    private var productRecyclerView: RecyclerView? = null
    private var category: String? = null
    private var iconGridLayout: GridLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            category = requireArguments().getString("category")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutView = inflater.inflate(R.layout.fragment_shop_category, container, false)
        initViews()
        loadProducts()
        return layoutView
    }

    private fun initViews() {
        productRecyclerView = findViewById(R.id.product_recycler_view)
        iconGridLayout = findViewById(R.id.icon_grid_layout)
        // 使用GridLayoutManager替换LinearLayoutManager，设置为2列
        productRecyclerView?.setLayoutManager(GridLayoutManager(context, 2))
        setupIconGrid()
    }

    private fun loadProducts() {
        // 根据category加载对应分类的商品数据
        val productItems: MutableList<ProductItem> = ArrayList()
        if (category != null) {
            // 这里根据不同的category加载不同的商品数据
            when (category) {
                "推荐" -> {
                    productItems.add(ProductItem("Nike Air Force 1", "¥899", "2.3万+人付款"))
                    productItems.add(ProductItem("Adidas Superstar", "¥699", "1.8万+人付款"))
                    productItems.add(ProductItem("New Balance 327", "¥799", "1.5万+人付款"))
                    productItems.add(ProductItem("Nike Air Force 1", "¥899", "2.3万+人付款"))
                    productItems.add(ProductItem("Adidas Superstar", "¥699", "1.8万+人付款"))
                    productItems.add(ProductItem("New Balance 327", "¥799", "1.5万+人付款"))
                    productItems.add(ProductItem("Nike Air Force 1", "¥899", "2.3万+人付款"))
                    productItems.add(ProductItem("Adidas Superstar", "¥699", "1.8万+人付款"))
                    productItems.add(ProductItem("New Balance 327", "¥799", "1.5万+人付款"))
                    productItems.add(ProductItem("Nike Air Force 1", "¥899", "2.3万+人付款"))
                    productItems.add(ProductItem("Adidas Superstar", "¥699", "1.8万+人付款"))
                    productItems.add(ProductItem("New Balance 327", "¥799", "1.5万+人付款"))
                    productItems.add(ProductItem("Nike Air Force 1", "¥899", "2.3万+人付款"))
                    productItems.add(ProductItem("Adidas Superstar", "¥699", "1.8万+人付款"))
                    productItems.add(ProductItem("New Balance 327", "¥799", "1.5万+人付款"))
                    productItems.add(ProductItem("Nike Air Force 1", "¥899", "2.3万+人付款"))
                    productItems.add(ProductItem("Adidas Superstar", "¥699", "1.8万+人付款"))
                    productItems.add(ProductItem("New Balance 327", "¥799", "1.5万+人付款"))
                }

                "鞋类" -> {
                    productItems.add(ProductItem("鸿星尔克 流云 网事拼接 耐磨跑鞋", "¥159", "1000+人付款"))
                    productItems.add(
                        ProductItem(
                            "鸿星尔克 百搭舒适 防滑耐磨休闲鞋",
                            "¥164",
                            "987人付款"
                        )
                    )
                    productItems.add(ProductItem("安踏 跑步鞋 男鞋运动鞋", "¥239", "2100+人付款"))
                }

                "潮服" -> {
                    productItems.add(
                        ProductItem(
                            "【得物粉丝专属福利】潮牌T恤",
                            "¥99",
                            "7.1万+人付款"
                        )
                    )
                    productItems.add(ProductItem("Nike 经典款运动卫衣", "¥399", "5000+人付款"))
                    productItems.add(ProductItem("Adidas 三叶草系列休闲裤", "¥299", "3000+人付款"))
                }

                "女装" -> {
                    productItems.add(ProductItem("ZARA 春季新款连衣裙", "¥399", "2000+人付款"))
                    productItems.add(ProductItem("优衣库 设计师合作款T恤", "¥199", "8000+人付款"))
                    productItems.add(ProductItem("H&M 时尚休闲套装", "¥299", "4000+人付款"))
                }

                "包袋" -> {
                    productItems.add(ProductItem("Coach 新款单肩包", "¥2999", "500+人付款"))
                    productItems.add(ProductItem("Michael Kors 托特包", "¥1999", "800+人付款"))
                    productItems.add(ProductItem("LV 经典款钱包", "¥4999", "300+人付款"))
                }

                "美妆" -> {
                    productItems.add(ProductItem("MAC 子弹头口红", "¥199", "1.2万+人付款"))
                    productItems.add(ProductItem("兰蔻 小黑瓶精华", "¥899", "5000+人付款"))
                    productItems.add(ProductItem("雅诗兰黛 DW粉底液", "¥459", "8000+人付款"))
                }
            }
        }
        val productAdapter = ProductAdapter(productItems)
        productRecyclerView!!.setAdapter(productAdapter)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupIconGrid() {
        // 添加图标项
        val iconTitles = arrayOf(
            "品牌专区", "疯狂折扣", "每日签到", "天天领券", "免费领好礼",
            "限时秒杀", "城市漫步", "Nike潮鞋", "全球购", "直播秒杀"
        )

        // 清除现有视图
        iconGridLayout!!.removeAllViews()

        // 设置网格布局参数
        val columnCount = 5
        iconGridLayout!!.columnCount = columnCount
        iconGridLayout!!.rowCount = 2

        // 设置GridLayout的布局参数
        val gridParams = iconGridLayout!!.layoutParams
        gridParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        gridParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        iconGridLayout!!.layoutParams = gridParams
        for (i in iconTitles.indices) {
            val iconView =
                LayoutInflater.from(context).inflate(R.layout.item_shop_icon, iconGridLayout, false)
            val iconImage = iconView.findViewById<ImageView>(R.id.icon_image)
            val iconTitle = iconView.findViewById<TextView>(R.id.icon_title)

            // 设置图标和标题
            // 这里应该设置实际的图标资源，暂时使用占位图
            iconImage.setImageResource(R.drawable.add_icon)
            iconTitle.text = iconTitles[i]

            // 添加到网格布局
            val params = GridLayout.LayoutParams()
            //            params.width = 0;
//            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % columnCount, 1, 1f)
            params.rowSpec = GridLayout.spec(i / columnCount, 1)
            params.setGravity(Gravity.CENTER)
            //            params.setMargins(8, 8, 8, 8);
            iconView.layoutParams = params
            iconGridLayout!!.addView(iconView)
        }
    }


    // 商品项数据类
    private class ProductItem(val name: String, val price: String, val sales: String)

    // 商品适配器
    private inner class ProductAdapter(private val productItems: List<ProductItem>) :
        RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_shop_product, parent, false)
            return ProductViewHolder(view)
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val item = productItems[position]
            holder.productName.text = item.name
            holder.productPrice.text = item.price
            holder.productSales.text = item.sales
        }

        override fun getItemCount(): Int {
            return productItems.size
        }

        inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var productImage: ImageView
            var productName: TextView
            var productPrice: TextView
            var productSales: TextView

            init {
                productImage = itemView.findViewById(R.id.product_image)
                productName = itemView.findViewById(R.id.product_name)
                productPrice = itemView.findViewById(R.id.product_price)
                productSales = itemView.findViewById(R.id.product_sales)
            }
        }
    }

    companion object {
        fun newInstance(category: String?): ShopCategoryFragment {
            val fragment = ShopCategoryFragment()
            val args = Bundle()
            args.putString("category", category)
            fragment.setArguments(args)
            return fragment
        }
    }
}
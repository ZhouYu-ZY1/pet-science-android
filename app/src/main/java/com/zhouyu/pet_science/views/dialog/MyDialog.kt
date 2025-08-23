package com.zhouyu.pet_science.views.dialog

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.utils.AndroidTextUtils
import com.zhouyu.pet_science.utils.InputUtils
import com.zhouyu.pet_science.utils.PhoneMessage
import com.zhouyu.pet_science.utils.Tool

/**
 * 自定义的Dialog
 */
class MyDialog : Dialog {
    private lateinit var yes: Button // 确定按钮
    private lateinit var no: Button // 取消按钮
    private lateinit var no_hint: Button // 不再提示按钮
    private lateinit var titleTV: TextView // 消息标题文本
    private lateinit var message: TextView // 消息提示文本
    var messageGravity = Gravity.CENTER
    private var titleStr: String? = null // 从外界设置的title文本
    private var titleSize = -1
    private var messageStr: CharSequence? = null // 从外界设置的消息文本
    private lateinit var dialog_input: EditText // 输入框
    private var dialog_input_type = InputType.TYPE_CLASS_TEXT
    private var dialog_input_hint = "" // 输入框提示信息
    private var dialog_input_default_value = ""
    // 确定文本和取消文本的显示的内容
    private var yesStr: String? = null
    private var noStr: String? = null
    private var noHintStr: String? = null
    private var noOnclickListener: OnNoOnclickListener? = null // 取消按钮被点击了的监听器
    private var yesOnclickListener: OnYesOnclickListener? = null // 确定按钮被点击了的监听器
    private var noHintOnclickListener: OnNoHintOnclickListener? = null // 确定按钮被点击了的监听器
    private var isHideNo = false // 是否隐藏取消按钮
    private var isHideYes = false // 是否隐藏确定按钮
    private var isShowNoHint = false // 是否显示不再提示按钮
    private var result = false // 返回值
    private var result_str = "" // 返回值
    private var canceledOnTouchOutside = true // 点击空白处消失
    private var isScroll = false
    // private var isShowRadioButton = false

    constructor(context: Context) : super(context, R.style.MyDialog)

    constructor(context: Context, @StyleRes themeResId: Int) : super(context, themeResId)

    constructor(context: Context, isScroll: Boolean) : super(context, R.style.MyDialog) {
        this.isScroll = isScroll
    }

    fun setDialog_input_default_value(value: String) {
        dialog_input_default_value = value
    }

    /**
     * 设置取消按钮的显示内容和监听 - Lambda版本
     */
    fun setNoOnclickListener(str: String?, onNoOnclickListener: (() -> Unit)?) {
        str?.let { noStr = it }
        this.noOnclickListener = if (onNoOnclickListener != null) {
            object : OnNoOnclickListener {
                override fun onNoClick() {
                    onNoOnclickListener()
                }
            }
        } else null
    }

    /**
     * 设置取消按钮的显示内容和监听 - 接口版本
     */
    fun setNoOnclickListener(str: String?, onNoOnclickListener: OnNoOnclickListener?) {
        str?.let { noStr = it }
        this.noOnclickListener = onNoOnclickListener
    }

    /**
     * 设置确定按钮的显示内容和监听 - Lambda版本
     */
    fun setYesOnclickListener(str: String?, yesOnclickListener: (() -> Unit)?) {
        str?.let { yesStr = it }
        this.yesOnclickListener = if (yesOnclickListener != null) {
            object : OnYesOnclickListener {
                override fun onYesOnclick() {
                    yesOnclickListener()
                }
            }
        } else null
    }

    /**
     * 设置确定按钮的显示内容和监听 - 接口版本
     */
    fun setYesOnclickListener(str: String?, yesOnclickListener: OnYesOnclickListener?) {
        str?.let { yesStr = it }
        this.yesOnclickListener = yesOnclickListener
    }

    /**
     * 设置确定按钮的显示内容和监听 - Lambda版本
     */
    fun setNoHintOnclickListener(str: String?, noHintOnclickListener: (() -> Unit)?) {
        str?.let { noHintStr = it }
        this.noHintOnclickListener = if (noHintOnclickListener != null) {
            object : OnNoHintOnclickListener {
                override fun onNoHintOnclick() {
                    noHintOnclickListener()
                }
            }
        } else null
        setShowNoHint(true)
    }

    /**
     * 设置确定按钮的显示内容和监听 - 接口版本
     */
    fun setNoHintOnclickListener(str: String?, noHintOnclickListener: OnNoHintOnclickListener?) {
        str?.let { noHintStr = it }
        this.noHintOnclickListener = noHintOnclickListener
        setShowNoHint(true)
    }

    // 是否隐藏取消按钮
    fun hideNoButton(isHide: Boolean) {
        isHideNo = isHide
    }

    // 是否隐藏确定按钮
    fun hideYesButton(isHide: Boolean) {
        isHideYes = isHide
    }

    // 是否显示不再提示按钮
    fun setShowNoHint(showNoHint: Boolean) {
        isShowNoHint = showNoHint
    }

    // 是否为输入框弹框
    private var isInput = false
    fun isInputDialog(isInput: Boolean) {
        this.isInput = isInput
    }

    private lateinit var my_dialog_content_view: LinearLayout
    private lateinit var content_view: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isScroll) {
            setContentView(R.layout.my_scroll_dialog)

            val scroll = findViewById<View>(R.id.scroll)
            val layoutParams = scroll.layoutParams
            layoutParams.height = (PhoneMessage.heightPixels * 0.4).toInt()
            scroll.layoutParams = layoutParams
        } else {
            setContentView(R.layout.my_dialog)
        }
        val window = window

        val layoutParams = window!!.attributes // 获取dialog布局的参数
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT // 全屏
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT // 全屏
        window.attributes = layoutParams

        BaseActivity.setStatusBarFullTransparent(window) // 设置透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }

        // 设置状态栏为白色
        BaseActivity.setStatusBarFullTransparent(window)

        window.attributes.windowAnimations = R.style.PauseDialogAnimation

        my_dialog_content_view = findViewById(R.id.my_dialog_content_view)
        val contentViewLayoutParams = my_dialog_content_view.layoutParams
        contentViewLayoutParams.width = (PhoneMessage.widthPixels * 1000) / 1250
        my_dialog_content_view.layoutParams = contentViewLayoutParams
        content_view = findViewById(R.id.content_view)

        // 初始化界面控件
        initView()

        // 初始化界面数据
        initData()
        // 初始化界面控件的事件
        initEvent()
    }

    /**
     * 初始化界面控件
     */
    private fun initView() {
        yes = findViewById(R.id.yes)
        no = findViewById(R.id.no)
        no_hint = findViewById(R.id.no_hint)
        titleTV = findViewById(R.id.title)
        message = findViewById(R.id.message)
        dialog_input = findViewById(R.id.dialog_input)
        val center_view = findViewById<View>(R.id.center_view)
        if (isHideNo) {
            center_view.visibility = View.GONE
            no.visibility = View.GONE
        }
        if (isHideYes) {
            center_view.visibility = View.GONE
            yes.visibility = View.GONE
        }
        if (isShowNoHint) {
            findViewById<View>(R.id.no_hint_view).visibility = View.VISIBLE
            findViewById<View>(R.id.center_view2).visibility = View.VISIBLE
        }
        if (isInput) {
            findViewById<View>(R.id.dialog_input_view).visibility = View.VISIBLE
            dialog_input.hint = dialog_input_hint
            dialog_input.setText(dialog_input_default_value)
            dialog_input.inputType = dialog_input_type
            if (messageStr == null || AndroidTextUtils.isEmpty(messageStr.toString())) {
                message.visibility = View.GONE
            }
        }
    }

    /**
     * 初始化界面控件的显示数据
     */
    private fun initData() {
        // 如果用户自定了title和message
        titleStr?.let { titleTV.text = it }
        if (titleSize != -1) {
            titleTV.textSize = titleSize.toFloat()
        }
        messageStr?.let {
            message.text = it
            if (isScroll) {
                message.movementMethod = LinkMovementMethod.getInstance() // 响应ClickableSpan点击事件必须设置
            }
        }
        message.gravity = messageGravity
        // 如果设置按钮文字
        yesStr?.let { yes.text = it }
        noStr?.let { no.text = it }
        if (noHintStr != null && ::no_hint.isInitialized) {
            no_hint.text = noHintStr
        }
        if (themeColor != -1) {
            titleTV.setTextColor(themeColor)
            yes.setTextColor(themeColor)
        }
    }

    /**
     * 初始化界面的确定和取消监听
     */
    private fun initEvent() {
        if (canceledOnTouchOutside) {
            findViewById<View>(R.id.content).setOnClickListener { dismiss() }
            my_dialog_content_view.setOnClickListener { }
        }

        // 设置确定按钮被点击后，向外界提供监听
        yes.setOnClickListener {
            yesOnclickListener?.onYesOnclick()
        }
        // 设置取消按钮被点击后，向外界提供监听
        no.setOnClickListener {
            noOnclickListener?.onNoClick()
        }

        if (::no_hint.isInitialized) {
            no_hint.setOnClickListener {
                noHintOnclickListener?.onNoHintOnclick()
            }
        }
    }

    fun getDialog_input_type(): Int {
        return dialog_input_type
    }

    fun setDialog_input_type(dialog_input_type: Int) {
        this.dialog_input_type = dialog_input_type
    }

    /**
     * 从外界Activity为Dialog设置标题
     */
    fun setTitle(title: String?) {
        titleStr = title
    }

    /**
     * 设置标题字体大小，单位dp
     */
    fun setTitleSize(size: Int) {
        titleSize = size
    }

    /**
     * 从外界Activity为Dialog设置message
     */
    fun setMessage(message: CharSequence?) {
        messageStr = message
    }

    /**
     * 设置输入框提示信息
     */
    fun setDialogInputHint(hint: String) {
        dialog_input_hint = hint
    }

    val dialogInputText: String
        get() = if (isInput) {
            dialog_input.text.toString()
        } else {
            ""
        }

    interface OnNoOnclickListener {
        fun onNoClick()
    }

    interface OnYesOnclickListener {
        fun onYesOnclick()
    }

    interface OnNoHintOnclickListener {
        fun onNoHintOnclick()
    }

    fun getResult(): Boolean {
        return result
    }

    fun setResult(result: Boolean) {
        this.result = result
    }

    fun getResult_str(): String {
        return result_str
    }

    fun setResult_str(result_str: String) {
        this.result_str = result_str
    }

    private var themeColor = -1
    fun setThemeColor(color: Int) {
        themeColor = color
    }

    private var keyboardListener: InputUtils.KeyboardListener? = null

    override fun show() {
        val activity = Tool.contextToActivity(context)
        if (activity == null || activity.isDestroyed) {
            return
        }
        super.show()
        if (isInput) {
            val input = findViewById<EditText>(R.id.dialog_input)
            keyboardListener = InputUtils.KeyboardListener(activity, object : InputUtils.OnKeyboardListener {
                private var moveHeight = 0

                override fun onKeyboardShown(currentKeyboardHeight: Int) {
                    try {
                        val i = (PhoneMessage.heightPixels + PhoneMessage.statusBarHeight) / 2 -
                                my_dialog_content_view.height / 2 - currentKeyboardHeight
                        moveHeight = i
                        val objectAnimator = ObjectAnimator.ofFloat(my_dialog_content_view, "translationY", 0f, i.toFloat())
                        objectAnimator.interpolator = DecelerateInterpolator() // 设置动画为0匀速执行
                        objectAnimator.duration = 300 // 动画执行一次的时间
                        objectAnimator.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onKeyboardHidden() {
                    try {
                        val objectAnimator = ObjectAnimator.ofFloat(my_dialog_content_view, "translationY", moveHeight.toFloat(), 0f)
                        objectAnimator.interpolator = DecelerateInterpolator() // 设置动画为0匀速执行
                        objectAnimator.duration = 300 // 动画执行一次的时间
                        objectAnimator.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

            Handler().postDelayed({
                // 弹起输入框
                InputUtils.inputShow(activity, input)
            }, 200)

            if (!AndroidTextUtils.isEmpty(dialog_input_default_value)) {
                input.selectAll()
            }
        }
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        if (listener == null || !isInput) {
            super.setOnDismissListener(listener)
            return
        }
        super.setOnDismissListener { dialog ->
            keyboardListener?.removeKeyboardListener()
            listener.onDismiss(dialog)
        }
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        canceledOnTouchOutside = cancel
    }
}
package com.zhouyu.pet_science.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.network.UserHttpTool
import android.content.Intent
import com.zhouyu.pet_science.databinding.ActivityLoginBinding
import com.zhouyu.pet_science.tools.StorageTool
import com.zhouyu.pet_science.tools.utils.ConsoleUtils

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    
    // 验证码倒计时
    private var countDownTimer: CountDownTimer? = null
    private val countDownTime = 60000L // 60秒
    private val countDownInterval = 1000L // 1秒
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTopBarView(binding.container, true)
        // 设置点击事件
        setupClickListeners()
    }
    

    
    private fun setupClickListeners() {
        // 设置选项卡点击事件
        binding.tabSmsLogin.setOnClickListener {
            switchToSmsLogin()
        }
        
        binding.tabPasswordLogin.setOnClickListener {
            switchToPasswordLogin()
        }
        
        // 设置获取验证码按钮点击事件
        binding.btnGetCode.setOnClickListener {
            val email = binding.etEmailSms.text.toString().trim()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("请输入正确的邮箱地址")
                return@setOnClickListener
            }
            
            // 发送验证码请求
            executeThread {
                val (success, errorMessage) = UserHttpTool.sendVerificationCode(email)
                runOnUiThread {
                    if (success) {
                        showToast("验证码已发送")
                        startCountDown()
                    } else {
                        showToast(errorMessage ?: "验证码发送失败")
                    }
                }
            }
        }
        
        // 设置验证码登录按钮点击事件
        binding.btnSmsLogin.setOnClickListener {
            val email = binding.etEmailSms.text.toString().trim()
            val code = binding.etSmsCode.text.toString().trim()
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("请输入正确的邮箱地址")
                return@setOnClickListener
            }
            
            if (code.length != 6) {
                showToast("请输入6位验证码")
                return@setOnClickListener
            }

            executeThread{
                val (success, data) = UserHttpTool.verifyVerificationCode(email, code)
                ConsoleUtils.logErr(data.toString())
                runOnUiThread {
                    if (success) {
                        try {
                            StorageTool.put("token",data.getString("token"))
                            if(data.getBoolean("isRegister")){  // 判断是否为注册
                                // 注册成功后跳转到个人信息填写页面
                                showToast("注册成功")
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }else {
                                // 登录成功后跳转到主页
                                showToast("登录成功")
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }catch (e: Exception){
                            showToast("登录失败："+e.message)
                        }
                    } else {
                        showToast("验证码过期/错误")
                    }
                }
            }
        }
        
        // 设置密码登录按钮点击事件
        binding.btnPasswordLogin.setOnClickListener {
            val email = binding.etEmailPassword.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("请输入正确的邮箱地址")
                return@setOnClickListener
            }
            
            if (password.isEmpty()) {
                showToast("请输入密码")
                return@setOnClickListener
            }
            
            // 登录成功后跳转到主页
            showToast("登录成功")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        
        // 设置忘记密码点击事件
        binding.tvForgotPassword.setOnClickListener {
            showToast("忘记密码功能开发中")
        }
        
        // 设置社交媒体登录按钮点击事件
        binding.btnWechatLogin.setOnClickListener {
            showToast("微信登录功能开发中")
        }
        
        binding.btnQqLogin.setOnClickListener {
            showToast("QQ登录功能开发中")
        }
        
        binding.btnWeiboLogin.setOnClickListener {
            showToast("微博登录功能开发中")
        }
        
        binding.passwordEye.setOnClickListener {
            val isPasswordVisible = binding.etPassword.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            if (isPasswordVisible) {
                // 切换为密码隐藏状态
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.passwordEyeImage.setImageResource(R.drawable.close_eye_icon)
            } else {
                // 切换为密码显示状态
                binding.etPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.passwordEyeImage.setImageResource(R.drawable.open_eye_icon)
            }
            // 保持光标位置在文本末尾
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
    }
    
    private fun switchToSmsLogin() {
        // 更新选项卡样式
        binding.tabSmsLoginText.setTextColor(getColor(R.color.Theme))
        binding.tabSmsLoginText.setTypeface(null, android.graphics.Typeface.BOLD)
        binding.tabPasswordLoginText.setTextColor(getColor(R.color.textTinge))
        binding.tabPasswordLoginText.setTypeface(null, android.graphics.Typeface.NORMAL)
        
        // 更新指示器
        binding.tabIndicatorSms.setBackgroundColor(getColor(R.color.Theme))
        binding.tabIndicatorPassword.setBackgroundColor(getColor(android.R.color.transparent))
        
        // 切换表单
        binding.smsLoginForm.visibility = View.VISIBLE
        binding.passwordLoginForm.visibility = View.GONE
    }
    
    private fun switchToPasswordLogin() {
        // 更新选项卡样式
        binding.tabSmsLoginText.setTextColor(getColor(R.color.textTinge))
        binding.tabSmsLoginText.setTypeface(null, android.graphics.Typeface.NORMAL)
        binding.tabPasswordLoginText.setTextColor(getColor(R.color.Theme))
        binding.tabPasswordLoginText.setTypeface(null, android.graphics.Typeface.BOLD)
        
        // 更新指示器
        binding.tabIndicatorSms.setBackgroundColor(getColor(android.R.color.transparent))
        binding.tabIndicatorPassword.setBackgroundColor(getColor(R.color.Theme))
        
        // 切换表单
        binding.smsLoginForm.visibility = View.GONE
        binding.passwordLoginForm.visibility = View.VISIBLE
    }
    
    private fun startCountDown() {
        binding.btnGetCode.isEnabled = false
        countDownTimer = object : CountDownTimer(countDownTime, countDownInterval) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.btnGetCode.text = "${seconds}秒后重新获取"
            }
            
            override fun onFinish() {
                binding.btnGetCode.isEnabled = true
                binding.btnGetCode.text = "获取验证码"
            }
        }.start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 取消倒计时
        countDownTimer?.cancel()
    }
}
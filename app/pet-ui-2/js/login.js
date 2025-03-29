document.addEventListener('DOMContentLoaded', function() {
    // 登录标签切换
    const loginTabs = document.querySelectorAll('.login-tab');
    const tabContents = document.querySelectorAll('.tab-content');
    
    loginTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');
            
            // 移除所有active类
            loginTabs.forEach(t => t.classList.remove('active'));
            tabContents.forEach(c => c.classList.remove('active'));
            
            // 添加active类到当前点击的标签和对应内容
            this.classList.add('active');
            document.getElementById(`${tabId}-login`).classList.add('active');
        });
    });
    
    // 获取验证码按钮
    const getCodeBtn = document.getElementById('get-code-btn');
    let countdown = 60;
    
    getCodeBtn.addEventListener('click', function() {
        const phoneInput = document.getElementById('phone-sms');
        const phone = phoneInput.value.trim();
        
        if (!validatePhone(phone)) {
            alert('请输入正确的手机号码');
            return;
        }
        
        // 模拟发送验证码
        this.disabled = true;
        this.innerText = `${countdown}秒后重新获取`;
        
        const timer = setInterval(() => {
            countdown--;
            this.innerText = `${countdown}秒后重新获取`;
            
            if (countdown <= 0) {
                clearInterval(timer);
                this.disabled = false;
                this.innerText = '获取验证码';
                countdown = 60;
            }
        }, 1000);
        
        // 这里应该有发送验证码的API调用
        console.log(`向 ${phone} 发送验证码`);
    });
    
    // 密码登录按钮
    const passwordLoginBtn = document.getElementById('password-login-btn');
    
    passwordLoginBtn.addEventListener('click', function() {
        const phone = document.getElementById('phone-password').value.trim();
        const password = document.getElementById('password').value.trim();
        
        if (!validatePhone(phone)) {
            alert('请输入正确的手机号码');
            return;
        }
        
        if (!password) {
            alert('请输入密码');
            return;
        }
        
        // 模拟登录请求
        console.log(`使用密码登录: ${phone}`);
        
        // 模拟登录成功
        loginSuccess();
    });
    
    // 验证码登录按钮
    const smsLoginBtn = document.getElementById('sms-login-btn');
    
    smsLoginBtn.addEventListener('click', function() {
        const phone = document.getElementById('phone-sms').value.trim();
        const smsCode = document.getElementById('sms-code').value.trim();
        
        if (!validatePhone(phone)) {
            alert('请输入正确的手机号码');
            return;
        }
        
        if (!smsCode || smsCode.length !== 6) {
            alert('请输入6位验证码');
            return;
        }
        
        // 模拟登录请求
        console.log(`使用验证码登录: ${phone}, 验证码: ${smsCode}`);
        
        // 模拟登录成功
        loginSuccess();
    });
    
    // 跳转到注册页面
    const goRegisterBtn = document.getElementById('go-register');
    const goRegisterSmsBtn = document.getElementById('go-register-sms');
    
    goRegisterBtn.addEventListener('click', function(e) {
        e.preventDefault();
        window.location.href = 'register.html';
    });
    
    goRegisterSmsBtn.addEventListener('click', function(e) {
        e.preventDefault();
        window.location.href = 'register.html';
    });
    
    // 忘记密码
    const forgotPasswordBtn = document.getElementById('forgot-password');
    const forgotPasswordSmsBtn = document.getElementById('forgot-password-sms');
    
    forgotPasswordBtn.addEventListener('click', function(e) {
        e.preventDefault();
        alert('忘记密码功能开发中...');
    });
    
    forgotPasswordSmsBtn.addEventListener('click', function(e) {
        e.preventDefault();
        alert('忘记密码功能开发中...');
    });
    
    // 社交登录按钮
    const socialBtns = document.querySelectorAll('.social-btn');
    
    socialBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            alert('第三方登录功能开发中...');
        });
    });
});

// 验证手机号
function validatePhone(phone) {
    const phoneRegex = /^1[3-9]\d{9}$/;
    return phoneRegex.test(phone);
}

// 登录成功处理
function loginSuccess() {
    // 存储登录状态
    localStorage.setItem('isLoggedIn', 'true');
    
    // 检查是否是新用户
    const isNewUser = Math.random() > 0.5; // 模拟新用户判断
    
    if (isNewUser) {
        // 新用户跳转到个人信息填写页面
        window.location.href = 'user-info.html';
    } else {
        // 老用户直接进入主页
        // 向父窗口发送登录成功消息
        window.parent.postMessage({ type: 'login-success' }, '*');
    }
}
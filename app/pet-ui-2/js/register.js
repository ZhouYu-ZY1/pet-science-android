document.addEventListener('DOMContentLoaded', function() {
    // 返回按钮
    const backBtn = document.getElementById('back-btn');
    
    backBtn.addEventListener('click', function() {
        window.location.href = 'login.html';
    });
    
    // 获取验证码按钮
    const getCodeBtn = document.getElementById('get-code-btn');
    let countdown = 60;
    
    getCodeBtn.addEventListener('click', function() {
        const phoneInput = document.getElementById('phone');
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
    
    // 注册按钮
    const registerBtn = document.getElementById('register-btn');
    
    registerBtn.addEventListener('click', function() {
        const phone = document.getElementById('phone').value.trim();
        const smsCode = document.getElementById('sms-code').value.trim();
        const password = document.getElementById('password').value.trim();
        const confirmPassword = document.getElementById('confirm-password').value.trim();
        const agreeTerms = document.getElementById('agree-terms').checked;
        
        // 验证表单
        if (!validatePhone(phone)) {
            alert('请输入正确的手机号码');
            return;
        }
        
        if (!smsCode || smsCode.length !== 6) {
            alert('请输入6位验证码');
            return;
        }
        
        if (!password || password.length < 6 || password.length > 20) {
            alert('请设置6-20位的密码');
            return;
        }
        
        if (password !== confirmPassword) {
            alert('两次输入的密码不一致');
            return;
        }
        
        if (!agreeTerms) {
            alert('请阅读并同意用户协议和隐私政策');
            return;
        }
        
        // 模拟注册请求
        console.log(`注册账号: ${phone}`);
        
        // 模拟注册成功
        registerSuccess();
    });
    
    // 跳转到登录页面
    const goLoginBtn = document.getElementById('go-login');
    
    goLoginBtn.addEventListener('click', function(e) {
        e.preventDefault();
        window.location.href = 'login.html';
    });
});

// 验证手机号
function validatePhone(phone) {
    const phoneRegex = /^1[3-9]\d{9}$/;
    return phoneRegex.test(phone);
}

// 注册成功处理
function registerSuccess() {
    // 存储登录状态
    localStorage.setItem('isLoggedIn', 'true');
    localStorage.setItem('isNewUser', 'true');
    
    // 跳转到个人信息填写页面
    window.location.href = 'user-info.html';
}
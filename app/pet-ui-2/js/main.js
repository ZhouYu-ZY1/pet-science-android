document.addEventListener('DOMContentLoaded', function() {
    // 更新状态栏时间
    updateTime();
    setInterval(updateTime, 60000);
    
    // 检查登录状态
    checkLoginStatus();
    
    // 底部导航切换
    const tabItems = document.querySelectorAll('.tab-item');
    const mainContent = document.getElementById('main-content');
    
    tabItems.forEach(item => {
        item.addEventListener('click', function() {
            // 移除所有active类
            tabItems.forEach(tab => tab.classList.remove('active'));
            // 添加active类到当前点击的项
            this.classList.add('active');
            // 更新iframe的src
            const page = this.getAttribute('data-page');
            mainContent.src = page;
        });
    });
    
    // 监听来自iframe的消息
    window.addEventListener('message', function(event) {
        if (event.data.type === 'login-success') {
            // 登录成功，显示主界面
            showMainInterface();
        } else if (event.data.type === 'logout') {
            // 登出，返回登录页面
            hideMainInterface();
        }
    });
});

// 更新状态栏时间
function updateTime() {
    const now = new Date();
    let hours = now.getHours();
    let minutes = now.getMinutes();
    
    // 格式化时间
    hours = hours < 10 ? '0' + hours : hours;
    minutes = minutes < 10 ? '0' + minutes : minutes;
    
    document.querySelector('.time').textContent = `${hours}:${minutes}`;
}

// 检查登录状态
function checkLoginStatus() {
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    
    if (isLoggedIn) {
        showMainInterface();
    } else {
        hideMainInterface();
    }
}

// 显示主界面
function showMainInterface() {
    document.querySelector('.tab-bar').style.display = 'flex';
    document.getElementById('main-content').src = 'pages/home.html';
}

// 隐藏主界面，显示登录页面
function hideMainInterface() {
    document.querySelector('.tab-bar').style.display = 'none';
    document.getElementById('main-content').src = 'pages/login.html';
}
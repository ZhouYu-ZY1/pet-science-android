document.addEventListener('DOMContentLoaded', function() {
    // 标签页切换
    const tabs = document.querySelectorAll('.tab');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');
            
            // 移除所有active类
            tabs.forEach(t => t.classList.remove('active'));
            tabContents.forEach(c => c.classList.remove('active'));
            
            // 添加active类到当前点击的标签和对应内容
            this.classList.add('active');
            document.getElementById(`${tabId}-content`).classList.add('active');
        });
    });
    
    // 编辑资料按钮点击事件
    const editProfileBtn = document.querySelector('.edit-profile-btn');
    
    editProfileBtn.addEventListener('click', function() {
        alert('即将进入个人资料编辑页面');
    });
    
    // 功能菜单项点击事件
    const functionItems = document.querySelectorAll('.function-item');
    
    functionItems.forEach(item => {
        item.addEventListener('click', function() {
            const functionName = this.querySelector('span').textContent;
            alert(`即将进入${functionName}页面`);
        });
    });
    
    // 添加宠物按钮点击事件
    const addPetBtn = document.querySelector('.more-link');
    
    addPetBtn.addEventListener('click', function(e) {
        e.preventDefault();
        alert('即将进入添加宠物页面');
    });
    
    // 内容项点击事件
    const contentItems = document.querySelectorAll('.content-item');
    
    contentItems.forEach(item => {
        item.addEventListener('click', function() {
            alert('即将查看详细内容');
        });
    });
});
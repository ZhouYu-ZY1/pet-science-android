document.addEventListener('DOMContentLoaded', function() {
    // 分类标签切换
    const categories = document.querySelectorAll('.category');
    
    categories.forEach(category => {
        category.addEventListener('click', function() {
            // 移除所有active类
            categories.forEach(c => c.classList.remove('active'));
            // 添加active类到当前点击的项
            this.classList.add('active');
            
            // 这里可以添加加载对应分类内容的逻辑
            const categoryName = this.textContent;
            console.log(`切换到分类: ${categoryName}`);
            // 模拟加载新内容
            simulateContentLoading(categoryName);
        });
    });
    
    // 点赞功能
    const heartIcons = document.querySelectorAll('.fa-heart');
    
    heartIcons.forEach(icon => {
        icon.addEventListener('click', function() {
            this.classList.toggle('far');
            this.classList.toggle('fas');
            this.classList.toggle('text-danger');
            
            // 更新点赞数
            const countElement = this.nextSibling;
            let count = parseInt(countElement.textContent);
            
            if (this.classList.contains('fas')) {
                countElement.textContent = count + 1;
            } else {
                countElement.textContent = count - 1;
            }
        });
    });
});

// 模拟加载新内容
function simulateContentLoading(category) {
    const contentWaterfall = document.querySelector('.content-waterfall');
    
    // 显示加载中效果
    contentWaterfall.innerHTML = '<div class="text-center w-100 py-5"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">加载中...</span></div></div>';
    
    // 模拟网络请求延迟
    setTimeout(() => {
        // 根据分类生成不同的内容
        let newContent = '';
        
        // 这里只是示例，实际应用中应该从服务器获取数据
        for (let i = 0; i < 4; i++) {
            let imageUrl, title, username;
            
            switch(category) {
                case '猫咪':
                    imageUrl = `https://images.unsplash.com/photo-${1500000000 + i * 10000}-cat`;
                    title = `猫咪日常护理小技巧 #${i+1}`;
                    username = `猫咪专家${i+1}号`;
                    break;
                case '狗狗':
                    imageUrl = `https://images.unsplash.com/photo-${1600000000 + i * 10000}-dog`;
                    title = `狗狗训练方法大全 #${i+1}`;
                    username = `狗狗训练师${i+1}号`;
                    break;
                case '小宠':
                    imageUrl = `https://images.unsplash.com/photo-${1700000000 + i * 10000}-small-pet`;
                    title = `小宠饲养环境布置 #${i+1}`;
                    username = `小宠达人${i+1}号`;
                    break;
                case '水族':
                    imageUrl = `https://images.unsplash.com/photo-${1800000000 + i * 10000}-aquarium`;
                    title = `水族箱设置与维护 #${i+1}`;
                    username = `水族爱好者${i+1}号`;
                    break;
                default:
                    imageUrl = `https://images.unsplash.com/photo-${1900000000 + i * 10000}-pet`;
                    title = `宠物生活精彩瞬间 #${i+1}`;
                    username = `宠物达人${i+1}号`;
            }
            
            newContent += `
            <div class="content-card">
                <div class="card-image">
                    <img src="${imageUrl}" alt="${category}照片">
                </div>
                <div class="card-content">
                    <h3 class="card-title">${title}</h3>
                    <div class="card-user">
                        <img src="https://randomuser.me/api/portraits/women/${20 + i}.jpg" alt="用户头像" class="user-avatar">
                        <span class="user-name">${username}</span>
                    </div>
                    <div class="card-stats">
                        <div class="stat"><i class="far fa-heart"></i> ${Math.floor(Math.random() * 500)}</div>
                        <div class="stat"><i class="far fa-comment"></i> ${Math.floor(Math.random() * 100)}</div>
                    </div>
                </div>
            </div>`;
        }
        
        contentWaterfall.innerHTML = newContent;
    }, 800);
}
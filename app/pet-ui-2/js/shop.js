document.addEventListener('DOMContentLoaded', function() {
    // 初始化轮播
    const carousel = new bootstrap.Carousel(document.getElementById('shopCarousel'), {
        interval: 3000,
        wrap: true
    });
    
    // 分类点击事件
    const categoryItems = document.querySelectorAll('.category-item');
    
    categoryItems.forEach(item => {
        item.addEventListener('click', function() {
            const categoryName = this.querySelector('.category-name').textContent;
            console.log(`选择了分类: ${categoryName}`);
            // 这里可以添加跳转到分类页面的逻辑
            alert(`即将进入${categoryName}分类`);
        });
    });
    
    // 添加到购物车按钮点击事件
    const addToCartButtons = document.querySelectorAll('.add-to-cart-btn');
    
    addToCartButtons.forEach(button => {
        button.addEventListener('click', function() {
            const productCard = this.closest('.product-card');
            const productName = productCard.querySelector('.product-name').textContent;
            const productPrice = productCard.querySelector('.product-price').textContent;
            
            console.log(`添加商品到购物车: ${productName}, 价格: ${productPrice}`);
            
            // 显示添加成功提示
            const originalText = this.innerHTML;
            this.innerHTML = '<i class="fas fa-check"></i> 已添加';
            this.disabled = true;
            
            setTimeout(() => {
                this.innerHTML = originalText;
                this.disabled = false;
            }, 2000);
        });
    });
    
    // 更多链接点击事件
    const moreLinks = document.querySelectorAll('.more-link');
    
    moreLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const sectionTitle = this.closest('.section-title').querySelector('h2').textContent;
            console.log(`查看更多: ${sectionTitle}`);
            alert(`即将查看更多${sectionTitle}`);
        });
    });
});
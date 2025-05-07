// 设置状态栏高度
function setStatusBarHeight(height) {
    if (height && height > 0) {
        const dpr = window.devicePixelRatio || 1; // 获取设备像素比
        const cssHeight = height / dpr; // 将物理像素转换为CSS像素
        // 获取 payment-header 元素
        const paymentHeader = document.querySelector('.payment-header');
        if (paymentHeader) {
            // 设置 padding-top 为状态栏高度
            paymentHeader.style.paddingTop = cssHeight + 'px';
        }
    }
}
    
document.addEventListener('DOMContentLoaded', function() {
    // 返回按钮
    const backBtn = document.getElementById('back-btn');
    
    backBtn.addEventListener('click', function() {
        // 调用Android的返回方法
        if (window.Android) {
            window.Android.goBack();
        }
    });
    
    // 订单详情折叠面板
    const showDetailBtn = document.getElementById('show-detail-btn');
    const orderDetailPanel = document.getElementById('order-detail-panel');
    let detailPanelVisible = false;
    
    showDetailBtn.addEventListener('click', function() {
        detailPanelVisible = !detailPanelVisible;
        
        if (detailPanelVisible) {
            orderDetailPanel.style.display = 'block';
            showDetailBtn.querySelector('i').style.transform = 'rotate(180deg)';
        } else {
            orderDetailPanel.style.display = 'none';
            showDetailBtn.querySelector('i').style.transform = 'rotate(0)';
        }
    });
    
    // 支付方式选择
    const paymentMethods = document.querySelectorAll('.payment-method-item');
    let selectedPaymentMethod = 'wx'; // 默认选择微信支付
    
    paymentMethods.forEach(method => {
        method.addEventListener('click', function() {
            // 移除所有支付方式的active类
            paymentMethods.forEach(m => {
                m.classList.remove('active');
            });
            
            // 添加active类到当前支付方式
            this.classList.add('active');
            
            // 获取选中的支付方式
            selectedPaymentMethod = this.getAttribute('data-method');
        });
    });
    
    // 倒计时功能
    const countdownTimer = document.getElementById('countdown-timer');
    let countdownInterval;
    let orderId;
    
    // 从Android获取订单过期时间
    function initCountdown() {
        if (window.Android) {
            try {
                const expirationData = JSON.parse(window.Android.getOrderExpiration());
                const totalSeconds = expirationData.expirationSeconds;
                
                if (totalSeconds <= 0) {
                    // 订单已过期
                    if (window.Android) {
                        window.Android.payError('订单已过期，请重新下单');
                    }
                    return;
                }
                
                let minutes = Math.floor(totalSeconds / 60);
                let seconds = totalSeconds % 60;
                
                // 更新倒计时显示
                countdownTimer.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
                
                // 启动倒计时
                countdownInterval = setInterval(function() {
                    seconds--;
                    
                    if (seconds < 0) {
                        minutes--;
                        seconds = 59;
                    }
                    
                    if (minutes < 0) {
                        clearInterval(countdownInterval);
                        if (window.Android) {
                            window.Android.payError('支付超时，订单已自动取消');
                        }
                        return;
                    }
                    
                    countdownTimer.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
                }, 1000);
                
            } catch (e) {
                console.error('获取订单过期时间失败', e);
                window.Android.payError('订单数据异常，请重试');
            }
        }
    }
    
    // 支付按钮
    const payNowBtn = document.getElementById('pay-now-btn');
    const paymentSuccessModal = document.getElementById('payment-success-modal');
    const paymentOverlay = document.getElementById('payment-overlay');
    const viewOrderBtn = document.getElementById('view-order-btn');
    const continueShoppingBtn = document.getElementById('continue-shopping-btn');
    
    // 支付密码相关元素
    const paymentPasswordModal = document.getElementById('payment-password-modal');
    const closePasswordBtn = document.getElementById('close-password-btn');
    const passwordDots = document.querySelectorAll('.password-dot');
    const numberKeys = document.querySelectorAll('.number-key');
    const deleteKey = document.getElementById('delete-key');
    const passwordError = document.getElementById('password-error');
    const modalPaymentAmount = document.getElementById('modal-payment-amount');
    
    // 支付密码
    let paymentPassword = '';
    // 正确的支付密码（实际应用中应该从服务器验证）
    const correctPassword = '123456';
    
    payNowBtn.addEventListener('click', function() {
        // 获取选中的支付方式
        const selectedMethod = document.querySelector('.payment-method-item.active');
        const paymentMethod = selectedMethod.getAttribute('data-method');
        
        // 显示支付密码弹窗
        paymentOverlay.style.display = 'block';
        paymentPasswordModal.classList.add('show');
        
        // 更新密码弹窗中的金额
        modalPaymentAmount.textContent = document.getElementById('payment-amount').textContent;
        
        // 重置密码输入
        resetPasswordInput();
    });
    
    // 关闭密码弹窗
    closePasswordBtn.addEventListener('click', function() {
        closePasswordModal();
    });
    
    // 点击遮罩层关闭密码弹窗
    paymentOverlay.addEventListener('click', function(event) {
        // 确保点击的是遮罩层本身，而不是其子元素
        if (event.target === paymentOverlay) {
            closePasswordModal();
        }
    });
    
    // 关闭密码弹窗的函数
    function closePasswordModal() {
        paymentPasswordModal.classList.remove('show');
        paymentOverlay.style.display = 'none';
        
        // 恢复支付按钮状态
        payNowBtn.textContent = '立即支付';
        payNowBtn.disabled = false;
        
        // 重置密码输入
        resetPasswordInput();
    }
    
    // 点击数字键
    numberKeys.forEach(key => {
        key.addEventListener('click', function() {
            // 添加按键按下效果
            this.classList.add('pressed');
            setTimeout(() => {
                this.classList.remove('pressed');
            }, 100);
            
            // 如果密码已满，不再添加
            if (paymentPassword.length >= 6) return;
            
            // 获取按键值
            const keyValue = this.getAttribute('data-key');
            
            // 添加到密码
            paymentPassword += keyValue;
            
            // 更新密码点
            updatePasswordDots();
            
            // 如果密码已满，验证密码
            if (paymentPassword.length === 6) {
                setTimeout(verifyPassword, 300);
            }
        });
    });
    
    // 删除键
    deleteKey.addEventListener('click', function() {
        // 添加按键按下效果
        this.classList.add('pressed');
        setTimeout(() => {
            this.classList.remove('pressed');
        }, 100);
        
        // 如果密码为空，不执行操作
        if (paymentPassword.length === 0) return;
        
        // 删除最后一位
        paymentPassword = paymentPassword.slice(0, -1);
        
        // 更新密码点
        updatePasswordDots();
        
        // 清除错误提示
        passwordError.textContent = '';
    });
    
    // 更新密码点
    function updatePasswordDots() {
        passwordDots.forEach((dot, index) => {
            if (index < paymentPassword.length) {
                dot.classList.add('filled');
            } else {
                dot.classList.remove('filled');
            }
        });
    }
    
    // 重置密码输入
    function resetPasswordInput() {
        paymentPassword = '';
        updatePasswordDots();
        passwordError.textContent = '';
    }
    
    // 验证密码
    function verifyPassword() {
        if (paymentPassword === correctPassword) {
            // 密码正确，关闭密码弹窗
            paymentPasswordModal.classList.remove('show');

            payNowBtn.textContent = '支付中...';
            payNowBtn.disabled = true;

            setTimeout(function() {
                if (window.Android) {
                    try {
                        // 调用Android的支付方法
                        const result = JSON.parse(window.Android.payOrder(selectedPaymentMethod));

                        if (result.success) {
                            // 支付成功
                            // 显示支付成功弹窗
                            paymentSuccessModal.style.display = 'block';

                            // 清除倒计时
                            clearInterval(countdownInterval);

                            // 通知Android支付成功
                            window.Android.paymentSuccess();
                        } else {
                            // 支付失败
                            alert('支付失败: ' + result.message);
                            payNowBtn.textContent = '立即支付';
                            payNowBtn.disabled = false;
                        }
                    } catch (e) {
                        console.error('支付过程中发生错误', e);
                        alert('支付过程中发生错误');
                        payNowBtn.textContent = '立即支付';
                        payNowBtn.disabled = false;
                    }
                }
            }, 500);
        } else {
            // 密码错误，显示错误信息
            passwordError.textContent = '支付密码错误，请重新输入';
            
            // 清空密码
            paymentPassword = '';
            
            // 更新密码点
            setTimeout(() => {
                updatePasswordDots();
            }, 200);
        }
    }
    
    // 支付成功后的按钮
    viewOrderBtn.addEventListener('click', function() {
        if (window.Android) {
            window.Android.goBack();
        }
    });
    
    continueShoppingBtn.addEventListener('click', function() {
        if (window.Android) {
            window.Android.goBack();
        }
    });
    
    // 从Android获取商品信息
    if (window.Android) {
        try {
            const productInfo = JSON.parse(window.Android.getProductInfo());
            updateProductInfo(productInfo);
            orderId = productInfo.orderId;
            
            // 初始化倒计时
            initCountdown();
        } catch (e) {
            console.error('获取商品信息失败', e);
        }
    }
    
    // 更新商品信息的函数
    function updateProductInfo(info) {
        if (info.productPrice) {
            // 计算总价（无论数量是多少）
            const quantity = info.quantity || 1;
            const totalPrice = (parseFloat(info.productPrice) * quantity).toFixed(2);

            // 更新页面上的金额
            document.getElementById('payment-amount').textContent = totalPrice;
            document.getElementById('bottom-payment-amount').textContent = totalPrice;

            // 更新商品价格
            const productPrice = document.querySelector('.product-price');
            if (productPrice) {
                productPrice.textContent = `¥${info.productPrice}`;
            }
            const priceValue = document.querySelector('.price-detail .price-item:first-child .price-value');
            if (priceValue) {
                priceValue.textContent = `¥${info.productPrice}`;
            }

            // 更新价格详情
            const priceTotalValue = document.querySelector('.price-detail .price-item.total .price-value');
            if (priceTotalValue) {
                 priceTotalValue.textContent = `¥${totalPrice}`;
            }


            // 如果有单品总价显示区域，也更新它
            const totalPriceElement = document.querySelector('.total-price');
            if (totalPriceElement) {
                totalPriceElement.textContent = `¥${totalPrice}`;
            }
        }

        if (info.productName) {
            document.querySelector('.product-name').textContent = info.productName;
        }

        if (info.productSpec) {
            document.querySelector('.product-spec').textContent = `规格：${info.productSpec}`;
        }

        if (info.productImage) {
            document.querySelector('.product-img').src = info.productImage;
        }

        if (info.quantity && info.quantity > 1) {
            document.querySelector('.product-quantity').textContent = `x${info.quantity}`;
        }
    }
});
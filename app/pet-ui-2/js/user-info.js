document.addEventListener('DOMContentLoaded', function() {
    // 头像上传
    const avatarPreview = document.getElementById('avatar-preview');
    const avatarInput = document.getElementById('avatar-input');
    const avatarUpload = document.querySelector('.avatar-preview');
    
    avatarUpload.addEventListener('click', function() {
        avatarInput.click();
    });
    
    avatarInput.addEventListener('change', function() {
        if (this.files && this.files[0]) {
            const reader = new FileReader();
            
            reader.onload = function(e) {
                avatarPreview.src = e.target.result;
            };
            
            reader.readAsDataURL(this.files[0]);
        }
    });
    
    // 个人简介字数统计
    const bioTextarea = document.getElementById('bio');
    const bioCount = document.getElementById('bio-count');
    
    bioTextarea.addEventListener('input', function() {
        const count = this.value.length;
        bioCount.textContent = count;
    });
    
    // 省市联动
    const provinceSelect = document.getElementById('province');
    const citySelect = document.getElementById('city');
    
    // 简化版的省市数据
    const cityData = {
        'beijing': ['东城区', '西城区', '朝阳区', '海淀区', '丰台区'],
        'shanghai': ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区'],
        'guangdong': ['广州市', '深圳市', '珠海市', '汕头市', '佛山市']
    };
    
    provinceSelect.addEventListener('change', function() {
        const province = this.value;
        
        // 清空城市选项
        citySelect.innerHTML = '<option value="">城市</option>';
        
        if (province && cityData[province]) {
            cityData[province].forEach(city => {
                const option = document.createElement('option');
                option.value = city;
                option.textContent = city;
                citySelect.appendChild(option);
            });
        }
    });
    
    // 宠物弹窗
    const petModal = document.getElementById('pet-modal');
    const addPetBtn = document.getElementById('add-pet-btn');
    const closeModalBtn = document.getElementById('close-modal-btn');
    const addPetConfirmBtn = document.getElementById('add-pet-confirm-btn');
    const petList = document.getElementById('pet-list');
    
    // 打开宠物弹窗
    addPetBtn.addEventListener('click', function() {
        petModal.style.display = 'flex';
    });
    
    // 关闭宠物弹窗
    closeModalBtn.addEventListener('click', function() {
        petModal.style.display = 'none';
    });
    
    // 点击弹窗外部关闭
    petModal.addEventListener('click', function(e) {
        if (e.target === petModal) {
            petModal.style.display = 'none';
        }
    });
    
    // 添加宠物
    addPetConfirmBtn.addEventListener('click', function() {
        const petName = document.getElementById('pet-name').value.trim();
        const petType = document.querySelector('input[name="pet-type"]:checked').value;
        const petBreed = document.getElementById('pet-breed').value.trim();
        const petAgeYear = document.getElementById('pet-age-year').value || 0;
        const petAgeMonth = document.getElementById('pet-age-month').value || 0;
        
        if (!petName) {
            alert('请输入宠物名称');
            return;
        }
        
        // 移除空提示
        const emptyTip = document.querySelector('.empty-pet-tip');
        if (emptyTip) {
            emptyTip.remove();
        }
        
        // 创建宠物项
        const petItem = document.createElement('div');
        petItem.className = 'pet-item';
        
        // 设置宠物图标
        let petIcon = 'fa-cat';
        if (petType === 'dog') {
            petIcon = 'fa-dog';
        } else if (petType === 'other') {
            petIcon = 'fa-paw';
        }
        
        // 设置宠物年龄文本
        let ageText = '';
        if (petAgeYear > 0) {
            ageText += `${petAgeYear}岁`;
        }
        if (petAgeMonth > 0) {
            ageText += `${petAgeMonth}个月`;
        }
        if (!ageText) {
            ageText = '未满月';
        }
        
        petItem.innerHTML = `
            <div class="pet-icon">
                <i class="fas ${petIcon}"></i>
            </div>
            <div class="pet-info">
                <h4>${petName}</h4>
                <p>${petBreed || '未知品种'} · ${ageText}</p>
            </div>
            <button class="pet-delete">
                <i class="fas fa-times"></i>
            </button>
        `;
        
        // 添加到宠物列表
        petList.appendChild(petItem);
        
        // 绑定删除事件
        const deleteBtn = petItem.querySelector('.pet-delete');
        deleteBtn.addEventListener('click', function() {
            petItem.remove();
            
            // 如果没有宠物了，显示空提示
            if (petList.children.length === 0) {
                petList.innerHTML = `
                    <div class="empty-pet-tip">
                        <i class="fas fa-paw"></i>
                        <p>添加您的宠物信息，获得更精准的内容推荐</p>
                    </div>
                `;
            }
        });
        
        // 重置表单并关闭弹窗
        document.getElementById('pet-name').value = '';
        document.getElementById('pet-type-cat').checked = true;
        document.getElementById('pet-breed').value = '';
        document.getElementById('pet-age-year').value = '';
        document.getElementById('pet-age-month').value = '';
        petModal.style.display = 'none';
    });
    
    // 跳过按钮
    const skipBtn = document.getElementById('skip-btn');
    
    skipBtn.addEventListener('click', function() {
        // 直接进入主页
        localStorage.removeItem('isNewUser');
        window.parent.postMessage({ type: 'login-success' }, '*');
    });
    
    // 提交按钮
    const submitBtn = document.getElementById('submit-btn');
    
    submitBtn.addEventListener('click', function() {
        const nickname = document.getElementById('nickname').value.trim();
        
        if (!nickname) {
            alert('请输入昵称');
            return;
        }
        
        // 收集表单数据
        const gender = document.querySelector('input[name="gender"]:checked').value;
        const birthday = document.getElementById('birthday').value;
        const province = document.getElementById('province').value;
        const city = document.getElementById('city').value;
        const bio = document.getElementById('bio').value.trim();
        
        // 这里应该有保存用户信息的API调用
        console.log('保存用户信息:', {
            nickname,
            gender,
            birthday,
            location: province && city ? `${province} ${city}` : '',
            bio
        });
        
        // 移除新用户标记并进入主页
        localStorage.removeItem('isNewUser');
        window.parent.postMessage({ type: 'login-success' }, '*');
    });
});
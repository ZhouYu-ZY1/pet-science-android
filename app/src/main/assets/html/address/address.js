// 设置状态栏高度
function setStatusBarHeight(height) {
    if (height && height > 0) {
        const dpr = window.devicePixelRatio || 1; // 获取设备像素比
        const cssHeight = height / dpr; // 将物理像素转换为CSS像素
        // 获取 payment-header 元素
        const paymentHeader = document.querySelector('.address-header');
        if (paymentHeader) {
            // 设置 padding-top 为状态栏高度
            paymentHeader.style.paddingTop = cssHeight + 'px';
        }
    }
}


// 地区数据
let regionData = [];

// 从Android获取地区数据
function loadRegionData() {
    if (window.Android) {
        try {
            // 调用Android接口获取地区数据
            const regionDataJson = window.Android.getRegionData();
            regionData = JSON.parse(regionDataJson);
        } catch (error) {
            console.error('获取地区数据失败:', error);
            // 加载失败时使用默认数据
            regionData = [
                {
                    "name": "北京市",
                    "city": [
                        {
                            "name": "北京市",
                            "area": ["东城区", "西城区", "朝阳区"]
                        }
                    ]
                }
            ];
        }
    }
}

// 模拟地址数据
let addresses = [];


// DOM 元素
const addressList = document.getElementById('address-list');
const addAddressBtn = document.getElementById('add-address-btn');
const addressModal = document.getElementById('address-modal');
const closeModalBtn = document.getElementById('close-modal-btn');
const addressForm = document.getElementById('address-form');
const modalTitle = document.getElementById('modal-title');
const deleteConfirmModal = document.getElementById('delete-confirm-modal');
const cancelDeleteBtn = document.getElementById('cancel-delete-btn');
const confirmDeleteBtn = document.getElementById('confirm-delete-btn');
const backBtn = document.getElementById('back-btn');

// 省市区选择器
const provinceSelect = document.getElementById('province');
const citySelect = document.getElementById('city');
const districtSelect = document.getElementById('district');

// 当前编辑的地址ID
let currentEditId = null;
// 当前要删除的地址ID
let currentDeleteId = null;

// 从Android获取地址列表
function loadAddressList() {
    if (window.Android) {
        try {
            // 调用Android接口获取地址列表
            const response = JSON.parse(window.Android.getAddressList());
            if (response.success && response.data) {
                // 转换后端数据格式为前端格式
                addresses = response.data.map(item => ({
                    id: item.id,
                    recipient: item.recipientName,
                    phone: item.recipientPhone,
                    province: item.province,
                    city: item.city,
                    district: item.district,
                    detailAddress: item.detailAddress,
                    tag: item.addressTag,
                    isDefault: item.isDefault === 1
                }));
                renderAddressList();
            } else {
                // 显示错误信息
                window.Android.showToast(response.message || '获取地址列表失败');
            }
        } catch (error) {
            console.error('获取地址列表失败:', error);
            window.Android.showToast('获取地址列表失败');
        }
    }
}

// 初始化
function init() {
    // 加载地区数据
    loadRegionData();
    // 加载地址列表
    loadAddressList();
    // 初始化地区选择器
    initRegionSelects();
    // 绑定事件
    bindEvents();
}

// 渲染地址列表
function renderAddressList() {
    if (addresses.length === 0) {
        addressList.innerHTML = `
            <div class="empty-address">
                <i class="fas fa-map-marker-alt"></i>
                <p>暂无收货地址，请添加</p>
            </div>
        `;
        return;
    }

    addressList.innerHTML = '';
    addresses.forEach(address => {
        const addressItem = document.createElement('div');
        addressItem.className = 'address-item';
        
        // 检查是否是选择模式
        const isSelectMode = window.Android && window.Android.isSelectMode && window.Android.isSelectMode();
        
        // 根据是否是选择模式，设置不同的HTML内容
        if (isSelectMode) {
            // 选择模式下，点击整个地址项可以选择地址
            addressItem.innerHTML = `
                <div class="address-info" data-id="${address.id}">
                    <div class="address-user">
                        <span class="user-name">${address.recipient}</span>
                        <span class="user-phone">${formatPhone(address.phone)}</span>
                        ${address.tag ? `<span class="address-tag-label">${address.tag}</span>` : ''}
                    </div>
                    <div class="address-detail">
                        ${address.isDefault ? '<span class="default-tag">默认</span>' : ''}
                        <p>${address.province} ${address.city} ${address.district} ${address.detailAddress}</p>
                    </div>
                </div>
                <div class="address-select">
                    <i class="fas fa-chevron-right"></i>
                </div>
            `;
            
            // 添加点击事件
            addressItem.addEventListener('click', () => {
                selectAddress(address);
            });
        } else {
            // 非选择模式下，显示编辑和删除按钮
            addressItem.innerHTML = `
                <div class="address-info">
                    <div class="address-user">
                        <span class="user-name">${address.recipient}</span>
                        <span class="user-phone">${formatPhone(address.phone)}</span>
                        ${address.tag ? `<span class="address-tag-label">${address.tag}</span>` : ''}
                    </div>
                    <div class="address-detail">
                        ${address.isDefault ? '<span class="default-tag">默认</span>' : ''}
                        <p>${address.province} ${address.city} ${address.district} ${address.detailAddress}</p>
                    </div>
                </div>
                <div class="address-actions">
                    <button class="edit-btn" data-id="${address.id}">
                        <i class="fas fa-pen"></i>
                    </button>
                    <button class="delete-btn" data-id="${address.id}">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            `;
        }
        
        addressList.appendChild(addressItem);
    });
}

// 选择地址
function selectAddress(address) {
    if (window.Android && window.Android.selectAddress) {
        // 将地址对象转换为JSON字符串
        const addressJson = JSON.stringify({
            id: address.id,
            recipientName: address.recipient,
            recipientPhone: address.phone,
            province: address.province,
            city: address.city,
            district: address.district,
            detailAddress: address.detailAddress,
            addressTag: address.tag,
            isDefault: address.isDefault ? 1 : 0
        });
        
        // 调用Android接口选择地址
        window.Android.selectAddress(addressJson);
    }
}

// 初始化地区选择器
function initRegionSelects() {
    // 获取DOM元素
    const regionPickerTrigger = document.getElementById('region-picker-trigger');
    const regionPickerModal = document.getElementById('region-picker-modal');
    const cancelPickerBtn = document.querySelector('.cancel-picker-btn');
    const confirmPickerBtn = document.querySelector('.confirm-picker-btn');
    const regionTabs = document.querySelectorAll('.region-tab');
    const regionLists = document.querySelectorAll('.region-list');
    const provinceList = document.getElementById('province-list');
    const cityList = document.getElementById('city-list');
    const districtList = document.getElementById('district-list');
    const regionText = document.getElementById('region-text');

    // 当前选中的省市区
    let selectedProvince = '';
    let selectedCity = '';
    let selectedDistrict = '';
    let tempProvince = '';
    let tempCity = '';
    let tempDistrict = '';

    // 填充省份列表
    function renderProvinceList() {
        provinceList.innerHTML = '';
        regionData.forEach(province => {
            const item = document.createElement('div');
            item.className = 'region-item';
            if (province.name === selectedProvince) {
                item.className += ' active';
            }
            item.textContent = province.name;
            item.dataset.name = province.name;
            provinceList.appendChild(item);
        });
    }

    // 填充城市列表
    function renderCityList(provinceName) {
        cityList.innerHTML = '';
        const province = regionData.find(p => p.name === provinceName);
        if (province) {
            province.city.forEach(city => {
                const item = document.createElement('div');
                item.className = 'region-item';
                if (city.name === selectedCity) {
                    item.className += ' active';
                }
                item.textContent = city.name;
                item.dataset.name = city.name;
                cityList.appendChild(item);
            });
        }
    }

    // 填充区县列表
    function renderDistrictList(provinceName, cityName) {
        districtList.innerHTML = '';
        const province = regionData.find(p => p.name === provinceName);
        if (province) {
            const city = province.city.find(c => c.name === cityName);
            if (city) {
                city.area.forEach(area => {
                    const item = document.createElement('div');
                    item.className = 'region-item';
                    if (area === selectedDistrict) {
                        item.className += ' active';
                    }
                    item.textContent = area;
                    item.dataset.name = area;
                    districtList.appendChild(item);
                });
            }
        }
    }

    // 切换标签页
    function switchTab(level) {
        regionTabs.forEach(tab => {
            tab.classList.remove('active');
            if (tab.dataset.level === level) {
                tab.classList.add('active');
            }
        });

        regionLists.forEach(list => {
            list.classList.remove('active');
            if (list.id === level + '-list') {
                list.classList.add('active');
            }
        });
    }

    // 打开地区选择器
    regionPickerTrigger.addEventListener('click', function() {
        // 保存当前选中的值作为临时值
        tempProvince = selectedProvince;
        tempCity = selectedCity;
        tempDistrict = selectedDistrict;

        // 渲染省份列表
        renderProvinceList();

        // 如果已选择省份，则渲染城市列表
        if (tempProvince) {
            renderCityList(tempProvince);
            // 如果已选择城市，则渲染区县列表
            if (tempCity) {
                renderDistrictList(tempProvince, tempCity);
            }
        }

        // 显示弹窗
        regionPickerModal.style.display = 'block';
    });

    // 取消选择
    cancelPickerBtn.addEventListener('click', function() {
        regionPickerModal.style.display = 'none';
    });

    // 确认选择
    confirmPickerBtn.addEventListener('click', function() {
        // 将临时选择的值保存为正式选择的值
        selectedProvince = tempProvince;
        selectedCity = tempCity;
        selectedDistrict = tempDistrict;

        // 更新隐藏的input值
        document.getElementById('province').value = selectedProvince;
        document.getElementById('city').value = selectedCity;
        document.getElementById('district').value = selectedDistrict;

        // 更新显示文本
        if (selectedProvince && selectedCity && selectedDistrict) {
            regionText.value = `${selectedProvince} ${selectedCity} ${selectedDistrict}`;
        } else {
            regionText.value = '';
        }

        // 关闭弹窗
        regionPickerModal.style.display = 'none';
    });

    // 点击省份
    provinceList.addEventListener('click', function(e) {
        const item = e.target.closest('.region-item');
        if (item) {
            // 更新选中状态
            provinceList.querySelectorAll('.region-item').forEach(el => {
                el.classList.remove('active');
            });
            item.classList.add('active');

            // 更新临时选择的省份
            tempProvince = item.dataset.name;
            tempCity = '';
            tempDistrict = '';

            // 渲染城市列表并切换到城市标签
            renderCityList(tempProvince);
            switchTab('city');
        }
    });

    // 点击城市
    cityList.addEventListener('click', function(e) {
        const item = e.target.closest('.region-item');
        if (item) {
            // 更新选中状态
            cityList.querySelectorAll('.region-item').forEach(el => {
                el.classList.remove('active');
            });
            item.classList.add('active');

            // 更新临时选择的城市
            tempCity = item.dataset.name;
            tempDistrict = '';

            // 渲染区县列表并切换到区县标签
            renderDistrictList(tempProvince, tempCity);
            switchTab('district');
        }
    });

    // 点击区县
    districtList.addEventListener('click', function(e) {
        const item = e.target.closest('.region-item');
        if (item) {
            // 更新选中状态
            districtList.querySelectorAll('.region-item').forEach(el => {
                el.classList.remove('active');
            });
            item.classList.add('active');

            // 更新临时选择的区县
            tempDistrict = item.dataset.name;
        }
    });

    // 点击标签切换
    regionTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            switchTab(this.dataset.level);
        });
    });

    // 点击弹窗外部关闭
    regionPickerModal.addEventListener('click', function(e) {
        if (e.target === regionPickerModal) {
            regionPickerModal.style.display = 'none';
        }
    });

     // 添加自定义事件监听，用于编辑地址时更新选中状态
     document.addEventListener('updateRegion', function() {
        // 获取当前设置的省市区值
        const provinceValue = document.getElementById('province').value;
        const cityValue = document.getElementById('city').value;
        const districtValue = document.getElementById('district').value;
        
        // 更新选中状态
        selectedProvince = provinceValue;
        selectedCity = cityValue;
        selectedDistrict = districtValue;
        
        // 同时更新临时值，以便打开选择器时显示正确
        tempProvince = provinceValue;
        tempCity = cityValue;
        tempDistrict = districtValue;
    });

}

// 绑定事件
function bindEvents() {
    // 添加地址按钮
    addAddressBtn.addEventListener('click', function() {
        openAddressModal();
    });


    // 表单提交
    addressForm.addEventListener('submit', function(e) {
        e.preventDefault();
        saveAddress();
    });

    // 编辑按钮点击事件委托
    addressList.addEventListener('click', function(e) {
        if (e.target.closest('.edit-btn')) {
            const id = parseInt(e.target.closest('.edit-btn').dataset.id);
            editAddress(id);
        } else if (e.target.closest('.delete-btn')) {
            const id = parseInt(e.target.closest('.delete-btn').dataset.id);
            showDeleteConfirm(id);
        }
    });

    // 取消删除
    cancelDeleteBtn.addEventListener('click', function() {
        deleteConfirmModal.style.display = 'none';
    });

    // 确认删除
    confirmDeleteBtn.addEventListener('click', function() {
        deleteAddress();
    });

    // 返回按钮
    backBtn.addEventListener('click', function() {
        if (window.Android) {
            window.Android.goBack();
        } else {
            window.history.back();
        }
    });

    // 点击弹窗外部关闭
    addressModal.addEventListener('click', function(e) {
        if (e.target === addressModal) {
            addressModal.style.display = 'none';
        }
    });

    deleteConfirmModal.addEventListener('click', function(e) {
        if (e.target === deleteConfirmModal) {
            deleteConfirmModal.style.display = 'none';
        }
    });

    // 地址标签点击事件
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('address-tag') && !e.target.classList.contains('custom-tag')) {
            // 移除其他标签的active状态
            document.querySelectorAll('.address-tag').forEach(tag => {
                tag.classList.remove('active');
            });
            // 添加当前标签的active状态
            e.target.classList.add('active');
        }
    });

    // 自定义标签输入框处理
    const customTagInput = document.querySelector('.custom-tag input');
    if (customTagInput) {
        customTagInput.addEventListener('focus', function() {
            // 移除其他标签的active状态
            document.querySelectorAll('.address-tag').forEach(tag => {
                tag.classList.remove('active');
            });
            // 添加自定义标签的active状态
            this.parentElement.classList.add('active');
        });

        customTagInput.addEventListener('blur', function() {
            // 如果输入框为空，移除active状态
            if (!this.value.trim()) {
                this.parentElement.classList.remove('active');
            }
        });
    }
}

// 打开地址弹窗（添加模式）
function openAddressModal() {
    modalTitle.textContent = '添加收货地址';
    addressForm.reset();
    currentEditId = null;
    addressModal.style.display = 'block';
}

// 编辑地址
function editAddress(id) {
    const address = addresses.find(a => a.id === id);
    if (!address) return;

    modalTitle.textContent = '编辑收货地址';
    currentEditId = id;

    // 填充表单
    document.getElementById('recipient').value = address.recipient;
    document.getElementById('phone').value = address.phone;
    document.getElementById('detail-address').value = address.detailAddress;
    document.getElementById('default-address').checked = address.isDefault;

    // 设置地址标签
    if (address.tag) {
        const tags = document.querySelectorAll('.address-tag');
        tags.forEach(tag => {
            tag.classList.remove('active');
            if (tag.dataset.tag === address.tag) {
                tag.classList.add('active');
            }
        });
        
        // 如果是自定义标签
        const customTagInput = document.querySelector('.custom-tag input');
        if (!document.querySelector('.address-tag.active') && customTagInput) {
            customTagInput.value = address.tag;
            customTagInput.parentElement.classList.add('active');
        }
    }

    // 设置省市区
    document.getElementById('province').value = address.province;
    document.getElementById('city').value = address.city;
    document.getElementById('district').value = address.district;
    
    // 更新地区选择器的显示文本
    const regionText = document.getElementById('region-text');
    regionText.value = `${address.province} ${address.city} ${address.district}`;
    
    // 更新地区选择器的临时选择值和选中状态
    // 在initRegionSelects函数中定义的变量，需要通过DOM获取
    const regionPickerTrigger = document.getElementById('region-picker-trigger');
    if (regionPickerTrigger) {
        // 触发一次点击事件来更新选中状态
        const event = new Event('updateRegion');
        document.dispatchEvent(event);
    }

    addressModal.style.display = 'block';
}

// 保存地址
function saveAddress() {
    const recipient = document.getElementById('recipient').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const province = provinceSelect.value;
    const city = citySelect.value;
    const district = districtSelect.value;
    const detailAddress = document.getElementById('detail-address').value.trim();
    const isDefault = document.getElementById('default-address').checked;
    
    // 获取选中的标签
    let tag = '';
    const activeTag = document.querySelector('.address-tag.active');
    if (activeTag) {
        if (activeTag.classList.contains('custom-tag')) {
            tag = activeTag.querySelector('input').value.trim();
        } else {
            tag = activeTag.dataset.tag;
        }
    }

    // 简单验证
    if (!recipient || !phone || !province || !city || !district || !detailAddress) {
        window.Android.showToast('请填写完整信息');
        return;
    }

    if (!/^1\d{10}$/.test(phone)) {
        window.Android.showToast('请输入正确的手机号码');
        return;
    }

    // 构建地址对象，使用UserAddress的字段名
    const addressData = {
        recipientName: recipient,
        recipientPhone: phone,
        province: province,
        city: city,
        district: district,
        detailAddress: detailAddress,
        addressTag: tag,
        isDefault: isDefault ? 1 : 0
    };

    if (currentEditId) {
        // 编辑模式
        addressData.id = currentEditId;
        
        if (window.Android) {
            try {
                // 调用Android接口更新地址
                const response = JSON.parse(window.Android.updateAddress(JSON.stringify(addressData)));
                if (response.success) {
                    window.Android.showToast('地址更新成功');
                    loadAddressList(); // 重新加载地址列表
                } else {
                    window.Android.showToast(response.message || '更新地址失败');
                }
            } catch (error) {
                console.error('更新地址失败:', error);
                window.Android.showToast('更新地址失败');
            }
        }
    } else {
        // 添加模式
        if (window.Android) {
            try {
                // 调用Android接口添加地址
                const response = JSON.parse(window.Android.addAddress(JSON.stringify(addressData)));
                if (response.success) {
                    window.Android.showToast('地址添加成功');
                    loadAddressList(); // 重新加载地址列表
                } else {
                    window.Android.showToast(response.message || '添加地址失败');
                }
            } catch (error) {
                console.error('添加地址失败:', error);
                window.Android.showToast('添加地址失败');
            }
        }
    }

    addressModal.style.display = 'none';
}

// 显示删除确认对话框
function showDeleteConfirm(id) {
    currentDeleteId = id;
    deleteConfirmModal.style.display = 'flex';
}

// 删除地址
function deleteAddress() {
    if (currentDeleteId === null) return;

    if (window.Android) {
        try {
            // 调用Android接口删除地址
            const response = JSON.parse(window.Android.deleteAddress(currentDeleteId));
            if (response.success) {
                window.Android.showToast('地址删除成功');
                loadAddressList(); // 重新加载地址列表
            } else {
                window.Android.showToast(response.message || '删除地址失败');
            }
        } catch (error) {
            console.error('删除地址失败:', error);
            window.Android.showToast('删除地址失败');
        }
    }

    deleteConfirmModal.style.display = 'none';
    currentDeleteId = null;
}

// 设置默认地址
function setDefaultAddress(id) {
    if (window.Android) {
        try {
            // 调用Android接口设置默认地址
            const response = JSON.parse(window.Android.setDefaultAddress(id));
            if (response.success) {
                window.Android.showToast('设置默认地址成功');
                loadAddressList(); // 重新加载地址列表
            } else {
                window.Android.showToast(response.message || '设置默认地址失败');
            }
        } catch (error) {
            console.error('设置默认地址失败:', error);
            window.Android.showToast('设置默认地址失败');
        }
    }
}

// 格式化手机号码，中间4位显示为星号
function formatPhone(phone) {
    if (!phone || phone.length !== 11) return phone;
    return phone.substring(0, 3) + '****' + phone.substring(7);
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', init);
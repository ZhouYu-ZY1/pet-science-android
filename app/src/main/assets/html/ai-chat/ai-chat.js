// 设置状态栏高度
function setStatusBarHeight(height) {
    if (height && height > 0) {
        const dpr = window.devicePixelRatio || 1; // 获取设备像素比
        const cssHeight = height / dpr; // 将物理像素转换为CSS像素
        // 获取 payment-header 元素
        const paymentHeader = document.querySelector('.chat-header');
        if (paymentHeader) {
            // 设置 padding-top 为状态栏高度
            paymentHeader.style.paddingTop = cssHeight + 'px';
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const chatContent = document.getElementById('chat-content');
    const messageInput = document.getElementById('message-input');
    const sendBtn = document.getElementById('send-btn');
    const backBtn = document.getElementById('back-btn');
    const modelSelector = document.getElementById('model-selector');
    const modelModal = document.getElementById('model-modal');
    const closeModelBtn = document.getElementById('close-model-btn');
    const modelItems = document.querySelectorAll('.model-item');
    const chatInputArea = document.querySelector('.chat-input-area');
    const aiChatContainer = document.querySelector('.ai-chat-container');
    
    // 宠物选择相关元素
    const petSelectorBtn = document.getElementById('pet-selector-btn');
    const petModal = document.getElementById('pet-modal');
    const closePetBtn = document.getElementById('close-pet-btn');
    const petList = document.getElementById('pet-list');
    const petEmpty = document.getElementById('pet-empty');
    
    // 宠物数据
    let petData = [];
    
    // 添加软键盘监听
    setupKeyboardListeners();

    // 模型配置
    let modelConfigs = [];

    // 当前选择的模型
    let currentModelIndex = 0;
    let currentModel = '';
    let currentModelName = '';
    let currentModelType = '';

    // API配置
    let apiKey = '';
    let baseUrl = '';

    // 用户滚动状态跟踪
    let isNearBottom = true;

    // 从Android获取模型列表和API配置
    if (window.Android) {
        try {
            // 获取模型列表
            const modelListJson = window.Android.getModelList();
            modelConfigs = JSON.parse(modelListJson);

            // 设置默认模型（第一个）
            if (modelConfigs.length > 0) {
                currentModel = modelConfigs[0].model;
                currentModelName = modelConfigs[0].name;
                currentModelType = modelConfigs[0].type;
                apiKey = modelConfigs[0].apiKey;
                baseUrl = modelConfigs[0].baseUrl;
            }

            // 获取API Key和Base URL（如果有自定义配置）
            const androidApiKey = window.Android.getApiKey();
            const androidBaseUrl = window.Android.getBaseUrl();

            if (androidApiKey && androidApiKey.trim() !== '') {
                apiKey = androidApiKey;
            }

            if (androidBaseUrl && androidBaseUrl.trim() !== '') {
                baseUrl = androidBaseUrl;
            }
            
            // 获取用户宠物列表
            try {
                const petsJson = window.Android.getPetList();
                if (petsJson) {
                    petData = JSON.parse(petsJson);
                }
            } catch (error) {
                console.error('获取宠物列表失败:', error);
            }
        } catch (error) {
            console.error('获取模型配置失败:', error);
        }
    }

    // 初始化聊天历史
    let chatHistory = [
        {
            role: "system",
            content: modelConfigs.length > 0 ? modelConfigs[0].systemPrompt :
                "你是一个专业的宠物顾问AI助手。你擅长回答关于宠物健康、行为、训练和日常护理的问题。请用友好、专业的语气回答用户的问题，并提供有用的建议。"
        }
    ];

    // 初始化页面
    function init() {
        // 清空示例消息
        chatContent.innerHTML = '';

        // 添加日期分隔线
        const dayDivider = document.createElement('div');
        dayDivider.className = 'chat-day-divider';
        dayDivider.innerHTML = '<span>今天</span>';
        chatContent.appendChild(dayDivider);

        // 更新模型选择器UI
        updateModelSelectorUI();
        
        // 更新宠物列表UI
        updatePetListUI();
        
        // 设置输入框事件
        setupInputEvents();

        // 添加欢迎消息
        addAIMessage(`🐾 你好呀！我是你的专属宠物顾问小助手~ 🐾
                      有什么关于毛孩子的问题都可以随时问我！比如：
                      ✨ <b>健康指南</b>：常见疾病、症状判断、护理建议
                      ✨ <b>行为解密</b>：乱叫/乱尿怎么办？社会化训练技巧
                      ✨ <b>吃啥更健康</b>：各年龄段食谱、零食选择禁忌
                      ✨ <b>好物推荐</b>：玩具/窝具挑选攻略、避雷指南
                      无论是喵星人、汪星人还是异宠宝宝的问题，我都会认真解答哟`);

        // 添加事件监听
        sendBtn.addEventListener('click', sendMessage);
        messageInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });

        backBtn.addEventListener('click', function() {
            if (window.Android) {
                window.Android.goBack();
            }
        });

        modelSelector.addEventListener('click', function() {
            modelModal.style.display = 'flex';
        });

        closeModelBtn.addEventListener('click', function() {
            modelModal.style.display = 'none';
        });
        
        // 宠物选择按钮点击事件
        petSelectorBtn.addEventListener('click', function() {
            petModal.style.display = 'flex';
        });
        
        closePetBtn.addEventListener('click', function() {
            petModal.style.display = 'none';
        });

        // 添加滚动事件监听
        chatContent.addEventListener('scroll', handleScroll);
        
        // 初始化滚动状态
        checkIfNearBottom();
    }
    
    // 更新宠物列表UI
    function updatePetListUI() {
        petList.innerHTML = '';
        
        if (petData && petData.length > 0) {
            petEmpty.style.display = 'none';
            petList.style.display = 'flex';
            
            petData.forEach(pet => {
                let petIconSrc = pet.avatarUrl;
                
                const petAge = calculatePetAge(pet.birthday);
                
                const petItemHTML = `
                    <div class="pet-item" data-pet-id="${pet.id}">
                        <div class="pet-icon">
                            <img src="${petIconSrc}" alt="${pet.name}">
                        </div>
                        <div class="pet-info">
                            <h4>${pet.name}</h4>
                            <p>${pet.breed}，${petAge}</p>
                        </div>
                    </div>
                `;
                petList.insertAdjacentHTML('beforeend', petItemHTML);
            });
            
            // 添加宠物选择事件
            document.querySelectorAll('.pet-item').forEach(item => {
                item.addEventListener('click', function(e) {
                    // 阻止点击事件传播到内部的元素
                    e.stopPropagation();
                    
                    const petId = this.getAttribute('data-pet-id');
                    const selectedPet = petData.find(pet => pet.id == petId);
                    
                    if (selectedPet) {
                        insertPetInfo(selectedPet);
                        petModal.style.display = 'none';
                    }
                });
                
                // 阻止子元素的点击事件冒泡
                const children = item.querySelectorAll('*');
                children.forEach(child => {
                    child.addEventListener('click', function(e) {
                        e.stopPropagation();
                        // 手动触发父元素的点击事件
                        item.click();
                    });
                });
            });
        } else {
            petEmpty.style.display = 'block';
            petList.style.display = 'none';
        }
    }
    
    // 计算宠物年龄
    function calculatePetAge(birthday) {
        if (!birthday) return '未知年龄';
        
        const birthDate = new Date(birthday);
        const now = new Date();
        
        const yearDiff = now.getFullYear() - birthDate.getFullYear();
        const monthDiff = now.getMonth() - birthDate.getMonth();
        
        if (yearDiff > 0) {
            return `${yearDiff}岁${monthDiff > 0 ? monthDiff + '个月' : ''}`;
        } else {
            const totalMonths = yearDiff * 12 + monthDiff;
            if (totalMonths > 0) {
                return `${totalMonths}个月`;
            } else {
                const dayDiff = Math.floor((now - birthDate) / (1000 * 60 * 60 * 24));
                return `${dayDiff}天`;
            }
        }
    }
    
    // 将宠物信息插入到输入框
    function insertPetInfo(pet) {
        const petAge = calculatePetAge(pet.birthday);
        const petInfo = `【宠物信息】\n名称：${pet.name}\n品种：${pet.breed}\n年龄：${petAge}\n类型：${pet.type === 'cat' ? '猫咪' : pet.type === 'dog' ? '狗狗' : '其他'}\n\n`;
        
        // 创建宠物标签元素
        const petTagElement = document.createElement('span');
        petTagElement.className = 'input-pet-tag';
        petTagElement.setAttribute('data-pet-id', pet.id);
        petTagElement.setAttribute('contenteditable', 'false'); // 设置为不可编辑
        petTagElement.setAttribute('data-pet-info', JSON.stringify({
            name: pet.name,
            breed: pet.breed,
            age: petAge,
            type: pet.type === 'cat' ? '猫咪' : pet.type === 'dog' ? '狗狗' : '其他'
        }));
        
        // 宠物头像
        const petIcon = document.createElement('img');
        petIcon.className = 'pet-tag-avatar';
        petIcon.src = pet.avatarUrl;
        petIcon.alt = pet.name;
        
        // 宠物名称
        const petName = document.createElement('span');
        petName.className = 'pet-tag-name';
        petName.textContent = pet.name;
        
        // 组装宠物标签
        petTagElement.appendChild(petIcon);
        petTagElement.appendChild(petName);
        
        // 将标签插入到光标位置
        insertNodeAtCursor(petTagElement);
        
        // 聚焦输入框
        messageInput.focus();
    }
    
    // 在光标位置插入节点
    function insertNodeAtCursor(node) {
        const selection = window.getSelection();
        if (selection.rangeCount) {
            const range = selection.getRangeAt(0);
            
            // 确保光标在输入框内部
            if (!messageInput.contains(range.startContainer)) {
                // 如果光标不在输入框内，将光标移到输入框末尾
                range.selectNodeContents(messageInput);
                range.collapse(false); // 折叠到末尾
            }
            
            range.deleteContents();
            range.insertNode(node);
            
            // 将光标移到插入的节点后面
            range.setStartAfter(node);
            range.setEndAfter(node);
            selection.removeAllRanges();
            selection.addRange(range);
            
            // 插入一个空格，确保光标可见且位于标签后面
            const spaceNode = document.createTextNode('\u200B'); // 零宽空格
            range.insertNode(spaceNode);
            range.setStartAfter(spaceNode);
            range.setEndAfter(spaceNode);
            selection.removeAllRanges();
            selection.addRange(range);
        } else {
            // 如果没有选区，添加到输入框末尾
            messageInput.appendChild(node);
            
            // 创建新范围并设置光标
            const range = document.createRange();
            range.selectNodeContents(messageInput);
            range.collapse(false); // 折叠到末尾
            selection.removeAllRanges();
            selection.addRange(range);
        }
    }
    
    // 处理输入框事件，支持宠物标签的删除和防止编辑
    function setupInputEvents() {
        // 设置键盘事件监听
        messageInput.addEventListener('keydown', function(e) {
            // 如果按下Backspace键
            if (e.key === 'Backspace') {
                const selection = window.getSelection();
                if (selection.rangeCount) {
                    const range = selection.getRangeAt(0);
                    
                    // 只处理光标状态（而非选中状态）
                    if (range.collapsed) {
                        let isPreviousElementPetTag = false;
                        let tagToRemove = null;
                        
                        // 情况1: 光标在文本节点内部且在起始位置
                        if (range.startContainer.nodeType === Node.TEXT_NODE && range.startOffset === 0) {
                            // 检查前一个兄弟节点
                            const previousNode = range.startContainer.previousSibling;
                            if (previousNode && previousNode.classList && previousNode.classList.contains('input-pet-tag')) {
                                isPreviousElementPetTag = true;
                                tagToRemove = previousNode;
                            }
                        }
                        
                        // 情况2: 光标在元素节点且在起始位置
                        else if (range.startContainer.nodeType === Node.ELEMENT_NODE && range.startOffset === 0) {
                            // 如果光标在输入框起始位置且有子元素
                            if (range.startContainer === messageInput && messageInput.childNodes.length > 0) {
                                // 检查光标之前的最后一个元素
                                const prevChildIndex = range.startOffset - 1;
                                if (prevChildIndex >= 0) {
                                    const prevNode = messageInput.childNodes[prevChildIndex];
                                    if (prevNode && prevNode.classList && prevNode.classList.contains('input-pet-tag')) {
                                        isPreviousElementPetTag = true;
                                        tagToRemove = prevNode;
                                    }
                                }
                            }
                            // 如果在元素内部的起始位置
                            else {
                                const currentNode = range.startContainer;
                                if (currentNode !== messageInput) {
                                    // 寻找前一个兄弟节点
                                    let prevNode = currentNode.previousSibling;
                                    while (!prevNode && currentNode.parentNode && currentNode.parentNode !== messageInput) {
                                        const parent = currentNode.parentNode;
                                        prevNode = parent.previousSibling;
                                        currentNode = parent;
                                    }
                                    
                                    if (prevNode && prevNode.classList && prevNode.classList.contains('input-pet-tag')) {
                                        isPreviousElementPetTag = true;
                                        tagToRemove = prevNode;
                                    }
                                }
                            }
                        }
                        
                        // 情况3: 光标紧随宠物标签之后
                        else {
                            // 尝试找到前一个节点
                            let currentNode = range.startContainer;
                            let prevNode = null;
                            
                            // 寻找前一个节点
                            if (range.startOffset > 0) {
                                // 如果在文本节点中间，检查文本前的内容
                                if (currentNode.nodeType === Node.TEXT_NODE) {
                                    // 如果光标不在文本开始处，那么不可能紧随标签
                                    if (range.startOffset > 0) {
                                        return;
                                    }
                                }
                                // 如果在元素节点中，检查前一个子节点
                                else if (currentNode.nodeType === Node.ELEMENT_NODE) {
                                    prevNode = currentNode.childNodes[range.startOffset - 1];
                                }
                            } else {
                                // 光标在当前节点开始位置，检查前一个兄弟节点
                                prevNode = currentNode.previousSibling;
                            }
                            
                            // 检查找到的节点是否为宠物标签
                            if (prevNode && prevNode.classList && prevNode.classList.contains('input-pet-tag')) {
                                isPreviousElementPetTag = true;
                                tagToRemove = prevNode;
                            }
                        }
                        
                        // 如果找到了要删除的宠物标签
                        if (isPreviousElementPetTag && tagToRemove) {
                            e.preventDefault(); // 阻止默认行为
                            tagToRemove.parentNode.removeChild(tagToRemove);
                        }
                    }
                }
            }
            
            // 阻止在标签内使用方向键
            if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') {
                const selection = window.getSelection();
                if (selection.rangeCount) {
                    const range = selection.getRangeAt(0);
                    const startNode = range.startContainer;
                    
                    // 检查是否在标签内或旁边
                    let node = startNode;
                    while (node && node !== messageInput) {
                        if (node.classList && node.classList.contains('input-pet-tag')) {
                            // 如果在标签内按左右方向键
                            if (e.key === 'ArrowLeft') {
                                // 左方向键：将光标移到标签前
                                e.preventDefault();
                                const newRange = document.createRange();
                                newRange.setStartBefore(node);
                                newRange.setEndBefore(node);
                                selection.removeAllRanges();
                                selection.addRange(newRange);
                            } else if (e.key === 'ArrowRight') {
                                // 右方向键：将光标移到标签后
                                e.preventDefault();
                                const newRange = document.createRange();
                                newRange.setStartAfter(node);
                                newRange.setEndAfter(node);
                                selection.removeAllRanges();
                                selection.addRange(newRange);
                            }
                            break;
                        }
                        node = node.parentNode;
                    }
                }
            }
        });
        
        // 监听光标变化事件，防止光标进入标签内部
        document.addEventListener('selectionchange', function() {
            // 确保焦点在消息输入框内
            if (document.activeElement === messageInput) {
                const selection = window.getSelection();
                if (selection.rangeCount > 0) {
                    const range = selection.getRangeAt(0);
                    
                    // 如果选区不是折叠的（即有文本被选中），不处理
                    if (!range.collapsed) return;
                    
                    const startNode = range.startContainer;
                    let insidePetTag = false;
                    let petTagNode = null;
                    
                    // 检查光标是否在宠物标签内部
                    let node = startNode;
                    while (node && node !== messageInput) {
                        if (node.classList && node.classList.contains('input-pet-tag')) {
                            insidePetTag = true;
                            petTagNode = node;
                            break;
                        }
                        // 如果当前节点是标签子元素
                        if (node.parentNode && node.parentNode.classList && 
                            node.parentNode.classList.contains('input-pet-tag')) {
                            insidePetTag = true;
                            petTagNode = node.parentNode;
                            break;
                        }
                        node = node.parentNode;
                    }
                    
                    // 如果光标在宠物标签内，将其移到标签后面
                    if (insidePetTag && petTagNode) {
                        // 防止无限循环，检查是否已经处理过
                        if (petTagNode._processingSelection) return;
                        
                        try {
                            petTagNode._processingSelection = true;
                            
                            // 创建一个新的范围，放在标签后面
                            const newRange = document.createRange();
                            newRange.setStartAfter(petTagNode);
                            newRange.setEndAfter(petTagNode);
                            
                            // 应用新范围
                            selection.removeAllRanges();
                            selection.addRange(newRange);
                            
                            // 使光标可见闪烁
                            messageInput.focus();
                        } finally {
                            // 清除标记
                            setTimeout(function() {
                                delete petTagNode._processingSelection;
                            }, 0);
                        }
                    }
                }
            }
        });
        
        // 监听鼠标按下事件，防止在标签内选中
        messageInput.addEventListener('mousedown', function(e) {
            const target = e.target;
            
            // 检查点击位置是否在标签或标签内部
            if (target.classList && target.classList.contains('input-pet-tag') || 
                target.closest('.input-pet-tag')) {
                
                e.preventDefault(); // 阻止默认选择行为
                
                // 获取标签元素
                const tagElement = target.classList.contains('input-pet-tag') ? 
                                  target : target.closest('.input-pet-tag');
                
                // 设置一个延时，确保在mouseup事件后执行
                setTimeout(function() {
                    // 将光标放在标签后面
                    const range = document.createRange();
                    range.setStartAfter(tagElement);
                    range.setEndAfter(tagElement);
                    
                    const selection = window.getSelection();
                    selection.removeAllRanges();
                    selection.addRange(range);
                    
                    // 聚焦输入框确保光标可见
                    messageInput.focus();
                }, 0);
            }
        });
        
        // 监听输入事件，确保标签不被编辑
        messageInput.addEventListener('input', function(e) {
            // 检查是否有宠物标签被部分删除
            const tags = messageInput.querySelectorAll('.input-pet-tag');
            tags.forEach(tag => {
                // 检查标签是否完整（至少包含头像和名称）
                if (!tag.querySelector('.pet-tag-avatar') || !tag.querySelector('.pet-tag-name')) {
                    // 标签不完整，删除整个标签
                    tag.parentNode.removeChild(tag);
                }
            });
            
            // 清理输入框中的所有元素，保留纯文本和宠物标签
            const allElements = messageInput.querySelectorAll('*:not(.input-pet-tag):not(.pet-tag-avatar):not(.pet-tag-name)');
            allElements.forEach(el => {
                // 如果元素是<br>标签，保留它
                if (el.tagName === 'BR') return;
                
                // 如果元素内部没有宠物标签，则可以安全地删除它
                if (!el.querySelector('.input-pet-tag') && !el.classList.contains('input-pet-tag')) {
                    // 获取元素的文本内容，并删除所有HTML元素
                    const text = el.textContent;
                    if (text.trim()) {
                        el.parentNode.replaceChild(document.createTextNode(text), el);
                    } else {
                        el.parentNode.removeChild(el);
                    }
                }
            });
        });
        
        // 监听粘贴事件，防止粘贴非文本内容
        messageInput.addEventListener('paste', function(e) {
            // 阻止默认粘贴行为
            e.preventDefault();
            
            // 获取粘贴的纯文本内容
            let text = (e.clipboardData || window.clipboardData).getData('text/plain');
            
            // 将纯文本内容插入到输入框中
            document.execCommand('insertText', false, text);
        });
    }

    // 处理滚动事件
    function handleScroll() {
        checkIfNearBottom();
    }
    // 检查是否接近底部
    function checkIfNearBottom() {
        const scrollPosition = chatContent.scrollTop + chatContent.clientHeight;
        const scrollHeight = chatContent.scrollHeight;
        // 如果距离底部小于10px，认为是在底部附近
        isNearBottom = (scrollHeight - scrollPosition) < 5;
    }

    // 更新模型选择器UI
    function updateModelSelectorUI() {
        // 更新当前模型名称
        document.querySelector('.current-model').textContent = currentModelName;

        // 清空模型列表
        const modelList = document.querySelector('.model-list');
        modelList.innerHTML = '';

        // 添加模型选项
        modelConfigs.forEach((config, index) => {
            const isActive = index === currentModelIndex;

            // 根据模型类型确定图标图片路径
            let iconSrc = 'images/deepseek.svh'; // 默认图标
            if (config.type === 'aliyun') {
                iconSrc = 'images/tongyi.png'; // 阿里云图标
            } else if (config.type === 'deepseek') {
                iconSrc = 'images/deepseek.svg'; // DeepSeek 图标
            } else if (config.type === 'hunyuan') {
                iconSrc = 'images/yuanbao.png'; // 混元图标
            }
            // 可以根据需要添加更多模型的 else if 判断

            const modelItemHTML = `
                <div class="model-item ${isActive ? 'active' : ''}" data-model-index="${index}">
                    <div class="model-icon">
                        <img src="${iconSrc}" alt="${config.name}图标" class="model-icon-img">
                    </div>
                    <div class="model-info">
                        <h4>${config.name}</h4>
                        <p>${config.model}</p>
                    </div>
                </div>
            `;
            modelList.insertAdjacentHTML('beforeend', modelItemHTML);
        });

        // 重新绑定事件
        document.querySelectorAll('.model-item').forEach(item => {
            item.addEventListener('click', function() {
                const modelIndex = parseInt(this.getAttribute('data-model-index'));
                const config = modelConfigs[modelIndex];

                // 更新当前模型
                currentModelIndex = modelIndex;
                currentModel = config.model;
                currentModelName = config.name;
                currentModelType = config.type;
                apiKey = config.apiKey;
                baseUrl = config.baseUrl;

                // 通知Android更新模型配置
                if (window.Android && typeof window.Android.updateModelConfig === 'function') {
                    window.Android.updateModelConfig(currentModelType);
                }

                // 更新UI
                document.querySelector('.current-model').textContent = currentModelName;

                // 移除所有active类
                document.querySelectorAll('.model-item').forEach(mi => mi.classList.remove('active'));

                // 添加active类到当前选中的模型
                this.classList.add('active');

                // 关闭模态框
                modelModal.style.display = 'none';

                // 更新系统提示
                updateSystemPrompt();
            });
        });
    }

    // 更新系统提示
    function updateSystemPrompt() {
        // 获取当前模型的系统提示
        const systemPrompt = modelConfigs[currentModelIndex].systemPrompt;

        // 更新聊天历史中的系统提示
        if (chatHistory.length > 0 && chatHistory[0].role === "system") {
            chatHistory[0].content = systemPrompt;
        } else {
            chatHistory.unshift({
                role: "system",
                content: systemPrompt
            });
        }

        // 添加模型切换提示
        addAIMessage(`已切换到${currentModelName}，有什么可以帮助你的吗？`);
    }

    // 发送消息
    function sendMessage() {
        // 检查输入框是否为空
        if (messageInput.innerHTML.trim() === '') return;
        
        // 获取所有宠物标签信息和文本内容
        let fullMessage = '';
        let displayMessage = '';
        let hasPetTag = false;
        
        // 遍历输入框中的所有节点
        Array.from(messageInput.childNodes).forEach(node => {
            if (node.nodeType === Node.TEXT_NODE) {
                // 纯文本节点
                const text = node.textContent.trim();
                if (text) {
                    fullMessage += text;
                    // 如果前面有宠物标签且当前文本不是以空格开头，添加一个空格
                    if (hasPetTag && displayMessage.endsWith('>') && !text.startsWith(' ')) {
                        displayMessage += ' ';
                    }
                    displayMessage += text;
                }
            } else if (node.classList && node.classList.contains('input-pet-tag')) {
                // 宠物标签节点
                const petId = node.getAttribute('data-pet-id');
                const petInfoStr = node.getAttribute('data-pet-info');
                if (petInfoStr) {
                    const petInfo = JSON.parse(petInfoStr);
                    const petInfoText = `【宠物信息】\n名称：${petInfo.name}\n品种：${petInfo.breed}\n年龄：${petInfo.age}\n类型：${petInfo.type}\n\n`;
                    fullMessage += petInfoText;
                    
                    // 为显示消息保留标签格式
                    displayMessage += `<span class="pet-tag" data-pet-id="${petId}">
                        <img src="${node.querySelector('.pet-tag-avatar').src}" class="pet-tag-avatar">
                        <span class="pet-tag-name">${petInfo.name}</span>
                    </span>`;
                    
                    hasPetTag = true;
                }
            } else if (node.tagName === 'BR') {
                // 换行符
                fullMessage += '\n';
                displayMessage += '<br>';
                hasPetTag = false; // 重置标签标识，因为已经换行
            } else {
                // 其他HTML元素，保留HTML格式
                fullMessage += node.textContent;
                displayMessage += node.outerHTML;
            }
        });
        
        if (!fullMessage.trim()) return;

        // 添加用户消息到聊天界面，使用HTML格式的消息
        addUserMessage(displayMessage, true);

        // 添加用户消息到聊天历史，使用纯文本+宠物信息格式
        chatHistory.push({
            role: "user",
            content: fullMessage.trim()
        });

        // 清空输入框
        messageInput.innerHTML = '';

        // 发送消息时重置滚动状态，确保显示新消息
        isNearBottom = true;

        // 调用AI API
        callAIAPI(fullMessage.trim());
    }

    // 调用AI API
    async function callAIAPI(message) {
        if (!apiKey) {
            addAIMessage("错误：未设置API密钥。请在应用设置中配置您的API密钥。");
            if (window.Android) {
                window.Android.showToast("未设置API密钥");
            }
            return;
        }

        let messageId = null; // 将 messageId 声明移到外部，以便在 catch 块中也能访问

        try {
            // 创建一个空的AI消息，获取相关的ID
            const ids = addEmptyAIMessage();
            messageId = ids.messageId;
            const { contentId, reasoningId, indicatorId } = ids;

            const response = await fetch(`${baseUrl}/chat/completions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${apiKey}`
                },
                body: JSON.stringify({
                    model: currentModel,
                    messages: chatHistory,
                    temperature: 0.7,
                    stream: true // 启用流式输出
                })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error?.message || '请求失败');
            }

            // 处理流式响应
            const reader = response.body.getReader();
            const decoder = new TextDecoder("utf-8");
            let aiResponse = "";
            let aiReasoningResponse = ""; // 用于累积思考内容

            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                // 解码收到的数据
                const chunk = decoder.decode(value, { stream: true });
                // 处理数据块
                const lines = chunk.split('\n');

                for (const line of lines) {
                    if (line.startsWith('data: ') && line !== 'data: [DONE]') {
                        try {
                            const data = JSON.parse(line.substring(6));
                            // 检查并处理主要内容
                            if (data.choices && data.choices[0].delta && data.choices[0].delta.content) {
                                const contentChunk = data.choices[0].delta.content;
                                aiResponse += contentChunk;
                                // 更新AI消息内容，传入 contentId
                                updateAIMessage(contentId, aiResponse);
                            }
                            // 新增：检查并处理思考内容
                            if (data.choices && data.choices[0].delta && data.choices[0].delta.reasoning_content) {
                                const reasoningChunk = data.choices[0].delta.reasoning_content;
                                aiReasoningResponse += reasoningChunk;
                                updateAIReasoning(reasoningId, aiReasoningResponse); // 调用新函数更新思考内容
                            }
                        } catch (e) {
                            console.error('解析流数据失败:', e, line);
                        }
                    }
                }
            }

             // 流式响应完成后，移除加载动画
             completeAIMessage(indicatorId);

            // 添加AI主要回复到聊天历史
            chatHistory.push({
                role: "assistant",
                content: aiResponse
            });

            // 获取当前时间并调用 Android 接口保存最后消息
            const finalTime = new Date().getTime();
            if (window.Android && typeof window.Android.saveLastMessage === 'function') {
                window.Android.saveLastMessage(aiResponse, finalTime);
            }

        } catch (error) {
            console.error('API调用失败:', error);
            // 如果出错，也需要移除可能存在的加载动画
            const indicatorElement = document.getElementById('ai-indicator-' + messageId); // 使用准确的ID查找
            if (indicatorElement) indicatorElement.remove();
            // 也可以考虑移除思考内容容器
            const reasoningElement = document.getElementById('ai-reasoning-' + messageId);
            if(reasoningElement) reasoningElement.remove();

            addAIMessage(`抱歉，发生了错误：${error.message}`);
            if (window.Android) {
                window.Android.showToast(`API调用失败: ${error.message}`);
            }
        }
    }


    // 添加空的AI消息，返回消息ID
    function addEmptyAIMessage() {
        const time = new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
        const messageId = 'ai-msg-' + Date.now();
        // 为消息内容、思考内容和加载动画创建唯一的ID
        const contentId = 'ai-content-' + messageId;
        const reasoningId = 'ai-reasoning-' + messageId; // 新增：思考内容ID
        const indicatorId = 'ai-indicator-' + messageId;

        const messageHTML = `
            <div class="chat-message ai-message" id="${messageId}">
                <div class="message-avatar">
                    <img src="images/ai_icon.svg" alt="AI头像">
                </div>
                <div class="message-content">
                    <div class="message-bubble">
                        <div id="${reasoningId}" class="reasoning-content" style="display: none;"></div>
                        <span id="${contentId}"></span>
                        <div class="typing-indicator" id="${indicatorId}">
                            <span></span>
                            <span></span>
                            <span></span>
                        </div>
                    </div>
                    <div class="message-time">${time}</div>
                </div>
            </div>
        `;

        chatContent.insertAdjacentHTML('beforeend', messageHTML);
        scrollToBottom();
        // 返回所有相关的ID
        return { messageId, contentId, reasoningId, indicatorId };
    }

    // 完成AI消息更新，移除加载动画
    function completeAIMessage(indicatorId) {
        // 使用 indicatorId 查找对应的加载动画元素
        const indicatorElement = document.getElementById(indicatorId);
        if (indicatorElement) {
            // 移除加载动画元素
            indicatorElement.remove();
        }
    }

     // 更新AI消息内容
     function updateAIMessage(contentId, content) {
        // 使用 contentId 查找对应的消息内容元素
        const contentElement = document.getElementById(contentId);
        if (contentElement) {
            // 只更新消息内容的 innerHTML
            contentElement.innerHTML = formatMessage(content) + '<br/>';
            scrollToBottom(); // 每次更新都滚动到底部，确保最新内容可见
        }
    }

    // 更新AI思考内容
    function updateAIReasoning(reasoningId, content) {
        const reasoningElement = document.getElementById(reasoningId);
        if (reasoningElement) {
            reasoningElement.innerHTML = formatMessage(content); // 可以使用相同的格式化函数，或创建特定的
            reasoningElement.style.display = 'block'; // 确保容器可见
            scrollToBottom();
        }
    }


    // 添加用户消息
    function addUserMessage(message, isHTML = false) {
        const time = new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
        
        // 获取用户头像URL，如果在Android环境中
        let userAvatarUrl = 'images/default_avatar.jpg'; // 默认头像
        if (window.Android && typeof window.Android.getUserAvatarUrl === 'function') {
            const avatarUrl = window.Android.getUserAvatarUrl();
            if (avatarUrl && avatarUrl.trim() !== '') {
                userAvatarUrl = avatarUrl;
            }
        }
        
        // 处理消息内容，确保适当的格式
        let messageContent = isHTML ? message : formatMessage(message);
        
        // 如果消息内容不是以<p>标签包裹，则添加<p>标签
        if (!messageContent.startsWith('<p>') && !messageContent.includes('<span class="pet-tag"')) {
            messageContent = `<p>${messageContent}</p>`;
        }
        
        const messageHTML = `
            <div class="chat-message user-message">
                <div class="message-content">
                    <div class="message-bubble">
                        ${messageContent}
                    </div>
                    <div class="message-time">${time}</div>
                </div>
                <div class="message-avatar">
                    <img src="${userAvatarUrl}" alt="用户头像">
                </div>
            </div>
        `;
        
        chatContent.insertAdjacentHTML('beforeend', messageHTML);
        
        // 确保用户消息的头像在右侧显示
        const userMessages = document.querySelectorAll('.user-message');
        const lastUserMessage = userMessages[userMessages.length - 1];
        
        // 添加内联样式确保正确显示
        lastUserMessage.style.display = 'flex';
        lastUserMessage.style.flexDirection = 'row';
        lastUserMessage.style.justifyContent = 'flex-end';
        
        scrollToBottom();
    }

    // 添加AI消息
    function addAIMessage(message) {
        const time = new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});

        const messageHTML = `
            <div class="chat-message ai-message">
                <div class="message-avatar">
                    <img src="images/ai_icon.svg" alt="AI头像">
                </div>
                <div class="message-content">
                    <div class="message-bubble">
                        ${formatMessage(message)}
                    </div>
                    <div class="message-time">${time}</div>
                </div>
            </div>
        `;

        chatContent.insertAdjacentHTML('beforeend', messageHTML);
        scrollToBottom();
    }

    // 格式化消息（处理换行和Markdown）
    function formatMessage(message) {
        // 简单的Markdown处理
        let formatted = message
            .replace(/\n/g, '<br>')
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.*?)\*/g, '<em>$1</em>');

        // 处理列表
        if (formatted.includes('\n- ')) {
            formatted = formatted.replace(/\n- (.*?)(?=\n|$)/g, '<li>$1</li>');
            formatted = formatted.replace(/<li>(.*?)<\/li>(?=<li>|$)/g, '<ul><li>$1</li></ul>');
            formatted = formatted.replace(/<\/ul><ul>/g, '');
        }
        if (formatted.includes('\n1. ')) {
            formatted = formatted.replace(/\n\d+\. (.*?)(?=\n|$)/g, '<li>$1</li>');
            formatted = formatted.replace(/<li>(.*?)<\/li>(?=<li>|$)/g, '<ol><li>$1</li></ol>');
            formatted = formatted.replace(/<\/ol><ol>/g, '');
        }

        // 处理标题 (从 h1 到 h6)
        formatted = formatted
                 .replace(/^[\s]*#{6} +([^\n]+?)(?=\s*$|\s*<br>)/gm, '<h6>$1</h6>')
                 .replace(/^[\s]*#{5} +([^\n]+?)(?=\s*$|\s*<br>)/gm, '<h5>$1</h5>')
                 .replace(/^[\s]*#{4} +([^\n]+?)(?=\s*$|\s*<br>)/gm, '<h4>$1</h4>')
                 .replace(/^[\s]*#{3} +([^\n]+?)(?=\s*$|\s*<br>)/gm, '<h3>$1</h3>')
                 .replace(/^[\s]*#{2} +([^\n]+?)(?=\s*$|\s*<br>)/gm, '<h2>$1</h2>')
                 .replace(/^[\s]*# +([^\n]+?)(?=\s*$|\s*<br>)/gm, '<h1>$1</h1>');

        return formatted;
    }

    // 添加软键盘监听函数
    function setupKeyboardListeners() {
        // 检测是否在Android环境中
        if (window.Android) {
            // 通过Android接口监听软键盘高度变化
            window.addEventListener('resize', function() {
                // 获取可视区域高度
                const viewportHeight = window.innerHeight;
                const initialHeight = window.outerHeight;

                // 如果可视区域高度变小，说明软键盘弹出
                if (viewportHeight < initialHeight) {
                    // 计算软键盘高度（近似值）
                    const keyboardHeight = initialHeight - viewportHeight;

                    // 设置聊天内容区域的底部padding，确保内容不被键盘遮挡
                    chatContent.style.paddingBottom = `${keyboardHeight}px`;

                    // 确保输入框可见
                    chatInputArea.style.position = 'fixed';
                    chatInputArea.style.bottom = '0';
                    chatInputArea.style.left = '0';
                    chatInputArea.style.right = '0';
                    chatInputArea.style.zIndex = '1000';

                    // 滚动到底部
                    scrollToBottom();
                } else {
                    // 软键盘收起，恢复原状
                    chatContent.style.paddingBottom = '';
                    chatInputArea.style.position = '';
                    chatInputArea.style.bottom = '';
                    chatInputArea.style.left = '';
                    chatInputArea.style.right = '';
                    chatInputArea.style.zIndex = '';
                }
            });

            // 通知Android我们已准备好接收软键盘事件
            if (typeof window.Android.registerKeyboardListener === 'function') {
                window.Android.registerKeyboardListener();
            }
        } else {
            // 在非Android环境中使用visualViewport API
            if (window.visualViewport) {
                window.visualViewport.addEventListener('resize', function() {
                    const currentHeight = window.visualViewport.height;
                    const initialHeight = window.innerHeight;

                    if (currentHeight < initialHeight) {
                        const keyboardHeight = initialHeight - currentHeight;
                        aiChatContainer.style.paddingBottom = `${keyboardHeight}px`;
                        scrollToBottom();
                    } else {
                        aiChatContainer.style.paddingBottom = '';
                    }
                });
            }
        }
    }

    // 滚动到底部
    function scrollToBottom() {
        // 已经在底部附近时，才自动滚动到底部
        if (isNearBottom) {
            chatContent.scrollTop = chatContent.scrollHeight;
        }
    }

    // 初始化
    init();
});

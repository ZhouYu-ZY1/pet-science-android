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
        messageInput.addEventListener('keypress', function(e) {
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

        // 添加滚动事件监听
        chatContent.addEventListener('scroll', handleScroll);
        
        // 初始化滚动状态
        checkIfNearBottom();
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
            let iconSrc = '../images/deepseek.svh'; // 默认图标
            if (config.type === 'aliyun') {
                iconSrc = '../images/tongyi.png'; // 阿里云图标
            } else if (config.type === 'deepseek') {
                iconSrc = '../images/deepseek.svg'; // DeepSeek 图标
            } else if (config.type === 'hunyuan') {
                iconSrc = '../images/yuanbao.png'; // 混元图标
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
        const message = messageInput.value.trim();
        if (!message) return;

        // 添加用户消息到聊天界面
        addUserMessage(message);

        // 添加用户消息到聊天历史
        chatHistory.push({
            role: "user",
            content: message
        });

        // 清空输入框
        messageInput.value = '';

        // 发送消息时重置滚动状态，确保显示新消息
        isNearBottom = true;

        // 调用AI API
        callAIAPI(message);
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
                    <img src="../images/ai_icon.svg" alt="AI头像">
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
    function addUserMessage(message) {
        const time = new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});

        const messageHTML = `
            <div class="chat-message user-message">
                <div class="message-content">
                    <div class="message-bubble">
                        <p>${formatMessage(message)}</p>
                    </div>
                    <div class="message-time">${time}</div>
                </div>
                <div class="message-avatar">
                    <img src="../images/default_avatar.jpg" alt="用户头像">
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
                    <img src="../images/ai_icon.svg" alt="AI头像">
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

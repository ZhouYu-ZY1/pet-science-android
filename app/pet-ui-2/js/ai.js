document.addEventListener('DOMContentLoaded', function() {
    // 聊天窗口控制
    const chatContainer = document.getElementById('chatContainer');
    const openChatBtn = document.getElementById('openChatBtn');
    const closeChatBtn = document.getElementById('closeChatBtn');
    const chatMessages = document.getElementById('chatMessages');
    const userInput = document.getElementById('userInput');
    const sendMessageBtn = document.getElementById('sendMessageBtn');
    
    // 打开聊天窗口
    openChatBtn.addEventListener('click', function() {
        chatContainer.style.display = 'flex';
    });
    
    // 关闭聊天窗口
    closeChatBtn.addEventListener('click', function() {
        chatContainer.style.display = 'none';
    });
    
    // 发送消息
    function sendMessage() {
        const message = userInput.value.trim();
        if (message === '') return;
        
        // 添加用户消息
        addMessage(message, 'user');
        
        // 清空输入框
        userInput.value = '';
        
        // 模拟AI回复
        setTimeout(() => {
            const aiResponse = getAIResponse(message);
            addMessage(aiResponse, 'ai');
        }, 1000);
    }
    
    // 发送按钮点击事件
    sendMessageBtn.addEventListener('click', sendMessage);
    
    // 输入框回车事件
    userInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
    
    // 添加消息到聊天窗口
    function addMessage(text, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}-message`;
        
        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'message-avatar';
        
        const icon = document.createElement('i');
        icon.className = sender === 'ai' ? 'fas fa-robot' : 'fas fa-user';
        avatarDiv.appendChild(icon);
        
        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';
        
        const paragraph = document.createElement('p');
        paragraph.textContent = text;
        contentDiv.appendChild(paragraph);
        
        messageDiv.appendChild(avatarDiv);
        messageDiv.appendChild(contentDiv);
        
        chatMessages.appendChild(messageDiv);
        
        // 滚动到底部
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
    
    // 模拟AI回复
    function getAIResponse(message) {
        message = message.toLowerCase();
        
        if (message.includes('猫') && (message.includes('吃') || message.includes('食物'))) {
            return '猫咪可以吃专门的猫粮、新鲜的肉类（煮熟）、少量蔬菜。但不能吃巧克力、葡萄、洋葱、大蒜等食物，这些对猫咪有毒。';
        } else if (message.includes('狗') && (message.includes('吃') || message.includes('食物'))) {
            return '狗狗可以吃专门的狗粮、煮熟的肉类、部分蔬菜和水果。但不能吃巧克力、葡萄、葡萄干、洋葱、大蒜、咖啡因等，这些对狗狗有毒。';
        } else if (message.includes('猫') && message.includes('洗澡')) {
            return '猫咪通常不需要经常洗澡，因为它们会自己清洁。一般来说，家猫每2-3个月洗一次澡就足够了。洗澡时水温应保持在38-40度，使用专门的猫咪洗浴产品。';
        } else if (message.includes('狗') && message.includes('洗澡')) {
            return '狗狗洗澡频率取决于品种、活动量和皮肤状况。一般来说，短毛犬4-6周洗一次，长毛犬2-4周洗一次。使用专门的狗狗洗浴产品，水温保持在38-40度。';
        } else if (message.includes('训练') || message.includes('教')) {
            return '宠物训练需要耐心和一致性。使用正向强化（奖励好行为）比惩罚更有效。从简单的命令开始，如"坐下"，逐渐增加难度。每天短时间多次训练比一次长时间训练效果更好。';
        } else if (message.includes('生病') || message.includes('症状') || message.includes('不舒服')) {
            return '常见的宠物生病症状包括：食欲减退、精神不振、呕吐、腹泻、异常排泄、咳嗽、打喷嚏、皮肤问题等。如果发现这些症状，建议及时咨询兽医，不要自行用药。';
        } else {
            return '感谢您的提问！我是宠物AI助手，可以回答关于宠物喂养、训练、健康等方面的问题。如果您有具体的宠物问题，请详细描述，我会尽力提供帮助。';
        }
    }
    
    // FAQ展开/收起
    const faqItems = document.querySelectorAll('.faq-item');
    
    faqItems.forEach(item => {
        const question = item.querySelector('.faq-question');
        
        question.addEventListener('click', function() {
            // 切换当前FAQ的展开状态
            item.classList.toggle('show-answer');
            
            // 关闭其他展开的FAQ（可选，取消注释以启用）
            // faqItems.forEach(otherItem => {
            //     if (otherItem !== item && otherItem.classList.contains('show-answer')) {
            //         otherItem.classList.remove('show-answer');
            //     }
            // });
        });
    });
    
    // 功能按钮点击事件
    const featureButtons = document.querySelectorAll('.feature-btn');
    
    featureButtons.forEach(button => {
        if (button.id !== 'openChatBtn') {
            button.addEventListener('click', function() {
                const featureTitle = this.closest('.feature-content').querySelector('h3').textContent;
                
                if (featureTitle === '宠物识别') {
                    // 模拟打开相机/上传功能
                    alert('即将打开相机进行宠物识别');
                } else if (featureTitle === '健康分析') {
                    // 模拟上传照片功能
                    alert('请选择宠物照片进行健康分析');
                }
            });
        }
    });
});
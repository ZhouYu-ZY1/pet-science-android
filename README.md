# 萌宠视界 (Pet Science Android)

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" alt="萌宠视界" width="120" height="120">
  
  <h3>一个集宠物社交、视频分享、商城购物于一体的Android应用</h3>
  
  ![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)
  ![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)
  ![License](https://img.shields.io/badge/License-MIT-yellow.svg)
  ![Version](https://img.shields.io/badge/Version-1.0-red.svg)
</div>

## 📱 应用简介

萌宠视界是一款专为宠物爱好者打造的综合性移动应用，提供宠物社交、短视频分享、智能聊天、商城购物等功能。用户可以分享宠物日常、观看宠物视频、购买宠物用品，与其他宠物爱好者交流互动。

## ✨ 主要功能

### 🏪 商城模块
- **商品浏览**: 支持分类浏览宠物用品
- **购物车**: 商品添加、数量调整、批量管理
- **订单管理**: 订单创建、支付、物流跟踪
- **地址管理**: 收货地址的增删改查

### 📹 视频模块
- **短视频播放**: 流畅的视频播放体验
- **智能推荐**: 基于用户喜好的视频推荐算法
- **视频缓存**: 预加载机制，提升播放体验
- **滑动切换**: 类似抖音的滑动播放体验

### 💬 聊天模块
- **即时通讯**: 基于WebSocket的实时聊天
- **AI智能聊天**: 集成AI助手，提供宠物护理建议
- **消息管理**: 聊天记录保存与管理

### 👤 个人中心
- **用户资料**: 个人信息编辑与展示
- **宠物管理**: 宠物信息录入与管理
- **作品发布**: 发布宠物相关内容
- **关注系统**: 关注/粉丝功能

## 🛠️ 技术栈

### 开发环境
- **开发语言**: Kotlin + Java
- **最低SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 14 (API 34)
- **编译SDK**: Android 15 (API 35)
- **构建工具**: Gradle 8.4

### 核心框架
- **UI框架**: ViewBinding + Material Design
- **网络请求**: OkHttp 4.12.0
- **JSON解析**: Gson 2.10.1
- **本地存储**: Hawk 2.0.1
- **图片加载**: Glide 4.11.0
- **视频播放**: ExoPlayer 2.18.7

### 第三方库
```gradle
// 权限管理
implementation 'com.github.getActivity:XXPermissions:11.5'

// 下拉刷新
implementation 'io.github.scwang90:refresh-layout-kernel:3.0.0-alpha'

// 图片选择
implementation 'com.github.HuanTanSheng:EasyPhotos:3.1.4'

// 图片缩放
implementation 'com.github.chrisbanes:PhotoView:2.3.0'

// 日期选择
implementation 'com.contrarywind:Android-PickerView:4.1.9'

// 底部导航
implementation 'com.github.ibrahimsn98:SmoothBottomBar:1.7.6'

// 屏幕适配
implementation 'com.github.JessYanCoding:AndroidAutoSize:v1.2.1'

// 高德地图
implementation 'com.amap.api:3dmap-location-search:latest.integration'

// Banner轮播
implementation 'io.github.youth5201314:banner:2.2.3'
```

## 🚀 快速开始

### 环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 8 或更高版本
- Android SDK 24 或更高版本

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/your-username/pet-science-android.git
cd pet-science-android
```

2. **配置环境**
```bash
# 在项目根目录创建 local.properties 文件
echo "sdk.dir=/path/to/your/android/sdk" > local.properties
```

3. **配置签名**
```bash
# 在 gradle.properties 中添加签名配置（可选）
RELEASE_KEY_ALIAS=your_key_alias
RELEASE_KEY_PASSWORD=your_key_password
RELEASE_STORE_FILE=path/to/your/keystore
RELEASE_STORE_PASSWORD=your_store_password
```

4. **构建项目**
```bash
./gradlew assembleDebug
```

5. **安装到设备**
```bash
./gradlew installDebug
```

## 📁 项目结构

```
app/src/main/
├── java/com/zhouyu/pet_science/
│   ├── activities/              # Activity页面
│   │   ├── MainActivity.kt      # 主页面
│   │   ├── LoginActivity.kt     # 登录页面
│   │   ├── ChatActivity.kt      # 聊天页面
│   │   └── ...
│   ├── fragments/               # Fragment组件
│   │   ├── shop/               # 商城相关Fragment
│   │   ├── MessageFragment.kt   # 消息Fragment
│   │   ├── VideoPlayFragment.kt # 视频播放Fragment
│   │   └── PersonalCenterFragment.kt # 个人中心Fragment
│   ├── network/                # 网络请求模块
│   │   ├── HttpUtils.kt        # HTTP工具类
│   │   ├── UserHttpUtils.kt    # 用户相关API
│   │   ├── ProductHttpUtils.kt # 商品相关API
│   │   └── ...
│   ├── model/                  # 数据模型
│   │   ├── User.kt            # 用户模型
│   │   ├── Pet.kt             # 宠物模型
│   │   ├── Product.kt         # 商品模型
│   │   └── ...
│   ├── utils/                  # 工具类
│   │   ├── StorageUtils.java   # 存储工具
│   │   ├── VideoUtils.kt       # 视频工具
│   │   └── ...
│   ├── adapter/                # 适配器
│   ├── views/                  # 自定义View
│   └── application/            # 应用程序类
├── res/                        # 资源文件
│   ├── layout/                 # 布局文件
│   ├── drawable/               # 图片资源
│   ├── values/                 # 值资源
│   └── ...
└── AndroidManifest.xml         # 应用配置文件
```

## 🔧 配置说明


### 高德地图配置
在 `AndroidManifest.xml` 中配置高德地图API Key：

```xml
<meta-data
    android:name="com.amap.api.v2.apikey"
    android:value="your_amap_api_key" />
```

### 权限配置
应用需要以下权限：
- 网络访问权限
- 位置权限（地图功能）
- 存储权限（文件读写）
- 相机权限（拍照功能）
- 通知权限

## 📊 API接口

### 用户相关
- `POST /user/login` - 用户登录
- `GET /user/getUserInfo` - 获取用户信息
- `PUT /user/update` - 更新用户信息

### 宠物相关
- `GET /pet/list` - 获取宠物列表
- `POST /pet/add` - 添加宠物
- `PUT /pet/update` - 更新宠物信息

### 商品相关
- `GET /product/list` - 获取商品列表
- `GET /product/category` - 获取商品分类
- `GET /product/detail` - 获取商品详情

### 订单相关
- `POST /order/create` - 创建订单
- `GET /order/list` - 获取订单列表
- `GET /order/detail` - 获取订单详情

## 🎨 UI设计

应用采用Material Design设计规范，支持日间/夜间模式切换。主要使用以下颜色主题：

- 主色调：`#FF6B6B`
- 辅助色：`#4ECDC4`
- 背景色：`#FFFFFF` / `#121212`（夜间模式）

## 🔒 安全特性

- JWT Token身份验证
- HTTPS加密传输
- 代码混淆保护
- 签名验证

## 📱 兼容性

- **最低版本**: Android 7.0 (API 24)
- **目标版本**: Android 14 (API 34)
- **测试设备**: 支持主流Android设备
- **屏幕适配**: 使用今日头条适配方案


## 📞 联系方式

- 邮箱：2179853437@qq.com
- 项目地址：https://github.com/your-username/pet-science-android

package com.zhouyu.pet_science.manager

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.zhouyu.pet_science.activities.ChatActivity
import com.zhouyu.pet_science.activities.MainActivity
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.application.Application
import com.zhouyu.pet_science.fragments.MessageFragment
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.network.UserHttpUtils
import com.zhouyu.pet_science.pojo.ChatMessage
import com.zhouyu.pet_science.pojo.MessageListItem
import com.zhouyu.pet_science.utils.ConsoleUtils
import com.zhouyu.pet_science.utils.MessageArrayList
import com.zhouyu.pet_science.utils.MyToast
import com.zhouyu.pet_science.utils.NotificationHelper
import com.zhouyu.pet_science.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.x52im.mobileimsdk.android.ClientCoreSDK
import net.x52im.mobileimsdk.android.conf.ConfigEntity
import net.x52im.mobileimsdk.android.core.LocalDataSender.SendCommonDataAsync
import net.x52im.mobileimsdk.android.core.LocalDataSender.SendLoginDataAsync
import net.x52im.mobileimsdk.android.event.ChatBaseEvent
import net.x52im.mobileimsdk.android.event.ChatMessageEvent
import net.x52im.mobileimsdk.android.event.MessageQoSEvent
import net.x52im.mobileimsdk.server.protocal.Protocal
import net.x52im.mobileimsdk.server.protocal.c.PLoginInfo
import net.x52im.mobileimsdk.server.protocal.s.PKickoutInfo
import org.json.JSONObject


/**
 * MobileIMSDK的管理类。
 * 正式的APP项目中，建议在Application中管理本类，确保SDK的生命周期同步于整个APP的生命周期。
 *
 * @author Jack Jiang(http://www.52im.net/thread-2792-1-1.html)
 */
class IMClientManager private constructor(private val context: BaseActivity) {
    companion object {
        private val IP = "192.168.1.100"

        private val TAG : String = "IMClientManager"

        private var instance: IMClientManager? = null

        fun getInstance(context: BaseActivity): IMClientManager {
            if (instance == null) instance = IMClientManager(context)
            return instance!!
        }
    }
    /** MobileIMSDK是否已被初始化. true表示已初化完成，否则未初始化.  */
    private var init = false

    /** 基本连接状态事件监听器  */
    var chatBaseListener: ChatBaseEventImpl? = null
        private set

    /** 数据接收事件监听器  */
    var chatMessageListener: ChatMessageEventImpl? = null
        private set

    /** 消息送达保证事件监听器  */
    var messageQoSListener: MessageQoSEventImpl? = null
        private set

    init {
        initMobileIMSDK()
    }

    /**
     * MobileIMSDK的初始化方法。正式的APP项目中，建议本方法在Application的子类中调用。
     */
    fun initMobileIMSDK() {
        if (!init) {
            // 设置服务器ip和服务器端口
			ConfigEntity.serverIP = "api.u1156996.nyat.app"
			ConfigEntity.serverPort = 61833

            // MobileIMSDK核心IM框架的敏感度模式设置
            ConfigEntity.setSenseMode(ConfigEntity.SenseMode.MODE_5S)

            // 设置最大TCP帧内容长度（不设置则默认最大是 6 * 1024字节）
//			LocalSocketProvider.TCP_FRAME_MAX_BODY_LENGTH = 60 * 1024;

            // 开启/关闭DEBUG信息输出
	    	ClientCoreSDK.DEBUG = true

            // 开启SSL/TLS加密传输（请确保服务端也已开启SSL）
//			LocalSocketProvider.sslContext = createSslContext();

            // 【特别注意】请确保首先进行核心库的初始化（这是不同于iOS和Java端的地方)
            ClientCoreSDK.getInstance().init(this.context)

            // 设置事件回调
            chatBaseListener = ChatBaseEventImpl()
            chatMessageListener = ChatMessageEventImpl()
            messageQoSListener = MessageQoSEventImpl()
            ClientCoreSDK.getInstance().chatBaseEvent = chatBaseListener
            ClientCoreSDK.getInstance().chatMessageEvent = chatMessageListener
            ClientCoreSDK.getInstance().messageQoSEvent = messageQoSListener

            Log.e(TAG,"【DEBUG_UI】MobileIMSDK初始化成功！")
            init = true
        }
    }

    /**
     * 创建SslContext对象，用于开启SSL/TLS加密传输。
     *
     * @return 如果成功创建则返回SslContext对象，否则返回null
     */
    //	public SslContext createSslContext() {
    //		SslContext sslContext = null;
    //		try {
    //			sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
    //			Log.e(TAG, "【IMCORE-TCP】已开启SSL/TLS加密(单向认证)，且sslContext创建成功。");
    //		} catch (Exception e) {
    //			Log.w(TAG, "【IMCORE-TCP】创建sslContext时出错，原因是：" + e.getMessage(), e);
    //		}
    //
    //		return sslContext;
    //	}
    /**
     * MobileIMSDK的资源释放方法（退出SDK时使用）。
     */
    fun release() {
        ClientCoreSDK.getInstance().release()
        resetInitFlag()
    }

    /**
     * 重置init标识。
     *
     *
     * **重要说明：**不退出APP的情况下，重新登陆时记得调用一下本方法，不然再
     * 次调用 [.initMobileIMSDK] 时也不会重新初始化MobileIMSDK（
     * 详见 [.initMobileIMSDK]代码）而报 code=203错误！
     */
    fun resetInitFlag() {
        init = false
    }

    /**
     * 发送消息
     */
    fun sendMessage(msg: String, friendId: String) {
        if (msg.isNotEmpty() && friendId.isNotEmpty()) {
            // 发送消息（Android系统要求必须要在独立的线程中发送哦）
            object : SendCommonDataAsync(msg, friendId) {
                override fun onPostExecute(code: Int) {
                    if (code == 0) Log.d(TAG, "2数据已成功发出！")
                    else Toast.makeText(
                        context,
                        "发送失败，错误码是：$code！",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.execute()
        }
    }


    private var currentToken: String = ""
    /**
     * 登陆MobileIMSDK
     */
    fun login(userId: String, token: String) {
        // 异步提交登陆id和token
        object : SendLoginDataAsync(PLoginInfo(userId, token)) {

            /**
             * 登陆信息发送完成后将调用本方法（注意：此处仅是登陆信息发送完成，真正的登陆结果要在异步回调中处理哦）。
             * @param code 数据发送返回码，0 表示数据成功发出，否则是错误码
             */
            override fun fireAfterSendLogin(code: Int) {
                if (code == 0) {
                    currentToken = token
                    ConsoleUtils.logErr("登陆/连接信息已成功发出！")
                } else {
                    currentToken = ""
                }
            }

        }.execute()
    }

    /**
     * 与IM服务器的连接事件在此ChatBaseEvent子类中实现即可。
     */
    inner class ChatBaseEventImpl : ChatBaseEvent {

        /**
         * 本地用户的登陆结果回调事件通知。
         * @param errorCode 服务端反馈的登录结果：0 表示登陆成功，否则为服务端自定义的出错代码（按照约定通常为>=1025的数）
         */
        override fun onLoginResponse(errorCode: Int) {
            if (errorCode == 0) {
                Log.e(TAG, "【DEBUG_UI】IM服务器登录/重连成功！")
            } else {
                Toast.makeText(context, "连接服务器失败，请检查网络是否正常！errCode: $errorCode", Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * 与服务端的通信断开的回调事件通知。
         * 该消息只有在客户端连接服务器成功之后网络异常中断之时触发。
         * 导致与与服务端的通信断开的原因有（但不限于）：无线网络信号不稳定、WiFi与2G/3G/4G等同开情况下的网络切换、手机系统的省电策略等。
         *
         * @param errorCode 本回调参数表示表示连接断开的原因，目前错误码没有太多意义，仅作保留字段，目前通常为-1
         */
        override fun onLinkClose(errorCode: Int) {
            Log.e(TAG, "【DEBUG_UI】与IM服务器的网络连接出错关闭了，error：$errorCode")
            Toast.makeText(context, "与服务器断开连接，error：$errorCode", Toast.LENGTH_SHORT).show()
        }

        /**
         * 本的用户被服务端踢出的回调事件通知。
         * @param kickoutInfo 被踢信息对象，[PKickoutInfo] 对象中的 code字段定义了被踢原因代码
         */
        override fun onKickout(kickoutInfo: PKickoutInfo) {
            Log.e(TAG, "【DEBUG_UI】已收到服务端的\"被踢\"指令，kickoutInfo.code：" + kickoutInfo.code)
            val alertContent = when (kickoutInfo.code) {
                PKickoutInfo.KICKOUT_FOR_DUPLICATE_LOGIN -> "账号已在其它地方登录，你被迫下线！"
                PKickoutInfo.KICKOUT_FOR_ADMIN -> "当前账号已被管理员强制下线！"
                else -> "你已被踢出聊天，当前会话已断开（kickoutReason=" + kickoutInfo.reason + "）！"
            }
            ActivityManager.logout(alertContent)
        }
    }


    /**
     * 与IM服务器的数据交互事件在此ChatTransDataEvent子类中实现即可。
     *
     * @author Jack Jiang(http://www.52im.net/thread-2792-1-1.html)
     * @version 1.1
     */
    inner class ChatMessageEventImpl : ChatMessageEvent {

        /**
         * 收到普通消息的回调事件通知。
         * <br></br>应用层可以将此消息进一步按自已的IM协议进行定义，从而实现完整的即时通信软件逻辑。
         *
         * @param fingerPrintOfProtocal 当该消息需要QoS支持时本回调参数为该消息的特征指纹码，否则为null
         * @param userid                消息的发送者id（MobileIMSDK框架中规定发送者id="0"即表示是由服务端主动发过的，否则表示的
         * 是其它客户端发过来的消息）
         * @param dataContent           消息内容的文本表示形式
         * @param typeu                 意义：应用层专用字段——用于应用层存放聊天、推送等场景下的消息类型。 注意：此值为-1时表示未定
         * 义。MobileIMSDK框架中，本字段为保留字段，不参与框架的核心算法，专留用应用 层自行定义
         * 和使用。 默认：-1。
         * @see [Protocal](http://docs.52im.net/extend/docs/api/mobileimsdk/server_netty/net/openmob/mobileimsdk/server/protocal/Protocal.html)
         */
        override fun onRecieveMessage(
            fingerPrintOfProtocal: String,
            userid: String,
            dataContent: String,
            typeu: Int
        ) {
            Log.e(TAG, "【DEBUG_UI】[typeu=" + typeu + "]收到来自用户" + userid + "的消息:" + dataContent)
            try {
                Application.executeThread {
                    val senderUser = UserHttpUtils.getUserInfoById(userid.toInt())
                    Log.e(TAG, "onRecieveMessage: $senderUser")
                    senderUser?.apply {
                        val senderId = senderUser.userId.toString()
                        val nickname = senderUser.nickname
                        val avatar = senderUser.avatarUrl
                        val messageList = MessageFragment.getMessageList()
                        var sendMessageListItem: MessageListItem? = null
                        var isExist = false
                        //遍历查找聊天记录，如已存在则直接在聊天记录添加新消息
                        for (messageListItem in messageList!!) {
                            val userId = messageListItem.userId
                            if (userId.trim() == senderId.trim()) {
                                //如果存在，则更新消息
                                sendMessageListItem = messageListItem
                                messageListItem.lastMessage = dataContent //设置未读信息
                                messageListItem.lastTime =
                                    TimeUtils.getMessageTime(System.currentTimeMillis())
                                messageListItem.unreadCount += 1 //未读加1
                                //添加新消息到消息列表中
                                val chatMessageList: MutableList<ChatMessage> =
                                    messageListItem.getChatMessageList()
                                val chatMessage = ChatMessage(
                                    senderId, nickname,
                                    currentToken, dataContent, 1
                                )
                                chatMessage.senderAvatar = avatar
                                chatMessageList.add(chatMessage) //头像
                                isExist = true
                                break
                            }
                        }

                        //如不存在聊天记录，则新建聊天记录，再添加到聊天中
                        if (!isExist) {
                            val currentTimeMillis = System.currentTimeMillis()
                            val messageListItem = MessageListItem()
                            messageListItem.userId = senderId
                            messageListItem.avatar = avatar
                            messageListItem.lastMessage = dataContent
                            messageListItem.lastTime = TimeUtils.getMessageTime(currentTimeMillis)
                            messageListItem.isOnline = true
                            messageListItem.unreadCount = 1
                            messageListItem.username = nickname
                            val chatMessages: MessageArrayList<ChatMessage> = MessageArrayList()
                            val chatMessage = ChatMessage()
                            chatMessage.content = dataContent
                            chatMessage.messageType = 1
                            chatMessage.senderAvatar = avatar
                            chatMessage.senderId = senderId
                            chatMessage.senderName = nickname
                            chatMessage.receiverId = currentToken
                            chatMessage.timestamp = currentTimeMillis
                            chatMessages.add(chatMessage)
                            messageListItem.chatMessageList = chatMessages
                            (messageList as MutableList<MessageListItem>).add(messageListItem)
                            sendMessageListItem = messageListItem
                        }

                        //发送通知
                        val allowNotification: Boolean =
                            NotificationHelper.isOpenNotification(context)
                        if (allowNotification) {
                            ConsoleUtils.logErr("收到消息：$dataContent")
                            val avatarBitmap: Bitmap = Glide.with(context).asBitmap()
                                .load(BASE_URL + sendMessageListItem!!.avatar)
                                .submit(500, 500).get()
                            val time = TimeUtils.getNotificationTime(System.currentTimeMillis())
                            NotificationHelper.createMessageNotification(
                                context,
                                nickname,
                                dataContent,
                                time,
                                avatarBitmap,
                                sendMessageListItem
                            )
                        }
                        MessageFragment.refreshList = true
                        ChatActivity.isRefresh = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 服务端反馈的出错信息回调事件通知。
         *
         * @param errorCode 错误码，定义在常量表 ErrorCode.ForS 类中
         * @param errorMsg  描述错误内容的文本信息
         * @see [ErrorCode.ForS类](http://docs.52im.net/extend/docs/api/mobileimsdk/server/net/openmob/mobileimsdk/server/protocal/ErrorCode.ForS.html)
         */
        override fun onErrorResponse(errorCode: Int, errorMsg: String) {
            Log.e(
                TAG,
                "【DEBUG_UI】收到服务端错误消息，errorCode=$errorCode, errorMsg=$errorMsg"
            )
            //			if (errorCode == ErrorCode.ForS.RESPONSE_FOR_UNLOGIN)
            //this.mainGUI.showIMInfo_brightred("服务端会话已失效，自动登陆/重连将启动! ("+errorCode+")");

            //			else
//				this.mainGUI.showIMInfo_red("Server反馈错误码：" + errorCode + ",errorMsg=" + errorMsg);
        }
    }


    /**
     * 消息送达相关事件（由QoS机制通知上来的）在此MessageQoSEvent子类中实现即可。
     *
     * @author Jack Jiang(http://www.52im.net/thread-2792-1-1.html)
     * @version 1.1
     */
    inner class MessageQoSEventImpl : MessageQoSEvent {
        private var mainGUI: Application? = null

        /**
         * 消息未送达的回调事件通知.
         *
         * @param lostMessages 由MobileIMSDK QoS算法判定出来的未送达消息列表（此列表中的Protocal对象是原对象的
         * clone（即原对象的深拷贝），请放心使用哦），应用层可通过指纹特征码找到原消息并可
         * 以UI上将其标记为”发送失败“以便即时告之用户
         * @see net.x52im.mobileimsdk.server.protocal.Protocal
         */
        override fun messagesLost(lostMessages: ArrayList<Protocal>) {
            Log.e(
                TAG,
                "【DEBUG_UI】收到系统的未实时送达事件通知，当前共有" + lostMessages.size + "个包QoS保证机制结束，判定为【无法实时送达】！"
            )
            if (this.mainGUI != null) {
//				this.mainGUI.showIMInfo_brightred("[消息未成功送达]共" + lostMessages.size() + "条!(网络状况不佳或对方id不存在)");
            }
        }

        /**
         * 消息已被对方收到的回调事件通知.
         *
         *
         * **目前，判定消息被对方收到是有两种可能：**<br></br>
         *
         *  * 1) 对方确实是在线并且实时收到了；
         *  * 2) 对方不在线或者服务端转发过程中出错了，由服务端进行离线存储成功后的反馈（此种情况严格来讲不能算是“已被
         * 收到”，但对于应用层来说，离线存储了的消息原则上就是已送达了的消息：因为用户下次登陆时肯定能通过HTTP协议取到）。
         *
         *
         * @param theFingerPrint 已被收到的消息的指纹特征码（唯一ID），应用层可据此ID来找到原先已发生的消息并可在
         * UI是将其标记为”已送达“或”已读“以便提升用户体验
         * @see net.x52im.mobileimsdk.server.protocal.Protocal
         */
        override fun messagesBeReceived(theFingerPrint: String?) {
            if (theFingerPrint != null) {
                Log.e(TAG, "【DEBUG_UI】收到对方已收到消息事件的通知，fp=$theFingerPrint")
                if (this.mainGUI != null) {
//					this.mainGUI.showIMInfo_blue("[收到对方消息应答]fp=" + theFingerPrint);
                }
            }
        }
    }
}
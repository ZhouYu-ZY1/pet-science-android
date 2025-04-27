package com.zhouyu.pet_science.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.ChatActivity
import com.zhouyu.pet_science.activities.MainActivity
import com.zhouyu.pet_science.pojo.MessageListItem
import com.zhouyu.pet_science.views.dialog.MyDialog

object NotificationHelper {
    private const val CHANNEL_ID = "message_channel"
    private const val NOTIFICATION_ID = 1
    @SuppressLint("WrongConstant")
    fun createMessageNotification(
        context: Context,
        nickname: String?,
        message: String?,
        time: String?,
        avatar: Bitmap?,
        item: MessageListItem
    ) {
        val roundedAvatar = BitmapUtils.getRoundedCornerBitmap(avatar, PhoneMessage.dpToPx(24f))
        ChatActivity.messageListItem = item
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(MainActivity.OPEN_CHAT_ACTIVITY, true)
        intent.putExtra("userId", item.userId)
        intent.putExtra("username", item.username)
        intent.putExtra("avatar", item.avatar)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)
        val remoteViews = RemoteViews(context.packageName, R.layout.message_notification_layout)
        remoteViews.setImageViewBitmap(R.id.notification_icon, roundedAvatar)
        remoteViews.setTextViewText(R.id.notification_title, nickname)
        remoteViews.setTextViewText(R.id.notification_message, message)
        remoteViews.setTextViewText(R.id.notification_time, time)
        
        // 设置通知声音
        val soundUri = Settings.System.DEFAULT_NOTIFICATION_URI
        
        // 修改通知渠道设置，确保高优先级
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "消息通知"
            val description = "接收消息提醒"
            val importance = NotificationManager.IMPORTANCE_HIGH  // 使用HIGH确保弹出
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            channel.enableLights(true)
            channel.lightColor = context.getColor(R.color.themeColor)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 300, 300, 300)  // 明确设置震动模式
            channel.setShowBadge(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            
            // 添加声音设置
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.setSound(soundUri, audioAttributes)
            
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        // 修改通知构建器
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(nickname)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setCustomContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_MAX)  // 设置为MAX优先级
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)
            
        // 对于Android 8.0以下版本，需要单独设置声音和震动
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setDefaults(Notification.DEFAULT_ALL)
            builder.setSound(soundUri)
            builder.setVibrate(longArrayOf(0, 300, 300, 300))  // 设置震动模式
        }
            
        // 对于Android 7.0及以上版本，设置大图标
        if (avatar != null) {
            builder.setLargeIcon(roundedAvatar)
        }
            
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun isOpenNotification(context: Context?): Boolean {
        return NotificationManagerCompat.from(context!!).areNotificationsEnabled()
    }

    //申请通知权限
    fun getNotification(activity: Activity) {
        if (!isOpenNotification(activity)) {
            val myNotificationDialog = MyDialog(activity, R.style.MyDialog)
            myNotificationDialog.setCanceledOnTouchOutside(false)
            myNotificationDialog.setTitle("检测到未开启通知权限")
            myNotificationDialog.setMessage("用于弹窗提示新消息")
            //        myNotificationDialog.setNoHintOnclickListener("不再提醒", () -> {
//            StorageTool.put("noAlertNotificationDialog",true);
//            myNotificationDialog.dismiss();
//        });
            myNotificationDialog.setYesOnclickListener("去开启") {
                myNotificationDialog.dismiss()
                val intent = Intent()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS")
                    intent.putExtra("android.provider.extra.APP_PACKAGE", activity.packageName)
                } else { //5.0
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS")
                    intent.putExtra("app_package", activity.packageName)
                    intent.putExtra("app_uid", activity.applicationInfo.uid)
                    activity.startActivity(intent)
                }
                activity.startActivity(intent)
            }
            myNotificationDialog.setNoOnclickListener("暂不开启", myNotificationDialog::dismiss)
            myNotificationDialog.show()
        }
    }
}
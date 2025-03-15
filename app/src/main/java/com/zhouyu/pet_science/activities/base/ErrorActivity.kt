package com.zhouyu.pet_science.activities.base

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.application.Application
import com.zhouyu.pet_science.application.CatchException
import com.zhouyu.pet_science.manager.ActivityManager.Companion.instance
import com.zhouyu.pet_science.tools.CustomSys.MyToast
import com.zhouyu.pet_science.tools.FileTool
import com.zhouyu.pet_science.tools.utils.PhoneMessage
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.util.Date
import java.util.Objects
import kotlin.system.exitProcess

class ErrorActivity : AppCompatActivity() {
    private var errMessageStr: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("软件发生未知错误，正在获取错误信息，请稍等！")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        Application.executeThread {
            errMessageStr = handlerException(CatchException.errorThrowable)
            runOnUiThread { progressDialog.dismiss() }
        }
        findViewById<View>(R.id.btn_err_message).setOnClickListener {
            PhoneMessage.copy(errMessageStr)
            MyToast.show("复制成功", true)
        }
        findViewById<View>(R.id.btn_restart).setOnClickListener {
            finish()
            instance.finishApplication()
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }
    }

    //自定义错误处理器
    private fun handlerException(ex: Throwable?): String {
        if (ex == null) {  //如果已经处理过这个Exception,则让系统处理器进行后续关闭处理
            return "null"
        }
        val message = ex.message
        if (message != null) {
            if (message.contains("Context.startForegroundService() did not then call Service.startForeground()")
                || message.contains("android.os.DeadSystemException")
            ) {
                return message
            }
        }

        //获取错误原因
        val writer: Writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        var errMsg = writer.toString()
        try {
            val time = Date().time
            //保存本地
            val path = Application.appCachePath + "/error/" + time + ".err"
            errMsg = """
                $errMsg
                ${collectDeviceInfo(true)}
                """.trimIndent()
            FileTool.commonStream.write(errMsg, path)

//            //上传云端
//            boolean canUpdate = false;
//            if(StorageTool.contains("lastUploadErrorTime")){
//                long lastUploadErrorTime = StorageTool.get("lastUploadErrorTime");
//                //每一小时才能上传一次错误信息
//                long spaceTime = 1000 * 60 * 60;
//                if(lastUploadErrorTime < time - spaceTime){
//                    canUpdate = true;
//                }
////                    else {
////                        ConsoleUtils.logErr("还差"+(spaceTime - (time  - lastUploadErrorTime))+"毫秒才可以上传");
////                    }
//            }else {
//                canUpdate = true;
//            }
//            ConsoleUtils.logErr(canUpdate+"");
//            if(canUpdate && !StartActivity.isSkipUpdate){
//                String userName;
//                if(Application.loginInformation.isNotLogin()){
//                    userName = "未登录";
//                }else {
//                    userName = Application.loginInformation.getUsername();
//                }
//                String fileName = userName+time;
//                   /*String error = CloudDiskTool.addDoc(fileName, CloudDiskTool.errorDirID);
//                   if(error != null){
//                       JSONObject err = new JSONObject(error);
//                       JSONObject data = err.getJSONObject("data");
//                       String node_id = data.getString("node_id");
//                       String e = errMsg.replace("\n","</br>");
//                       CloudDiskTool.Edit.editFile(collectDeviceInfo(false)+ "</br>" + e,node_id,fileName);
//                       StorageTool.put("lastUploadErrorTime",time); //保存错误信息上传时间
//                   }*/
//            }
        } catch (ignored: Exception) {
        } catch (ignored: Error) {
        }
        return errMsg
    }

    companion object {
        /**
         * 收集设备参数信息
         */
        @JvmStatic
        fun collectDeviceInfo(isLocal: Boolean): String {
            val c: String
            c = if (isLocal) {
                "\n"
            } else {
                "</br>"
            }
            val stringBuilder = StringBuilder()
            stringBuilder.append("versionName: ").append(PhoneMessage.getAppVersionName()).append(c)
            stringBuilder.append("versionCode: ").append(PhoneMessage.getAppVersionCode()).append(c)
            stringBuilder.append("Android版本: ").append(Build.VERSION.RELEASE).append(c)
            val fields = Build::class.java.declaredFields
            for (field in fields) {
                try {
                    field.isAccessible = true
                    val name = field.name
                    if (name.contains("CPU")) {
                        stringBuilder.append(name).append(": ").append(
                            Objects.requireNonNull(
                                field[null]
                            )
                        ).append(c)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return stringBuilder.toString()
        }
    }
}

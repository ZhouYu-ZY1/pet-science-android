package com.zhouyu.android_create.activities.parent;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zhouyu.android_create.R;
import com.zhouyu.android_create.application.Application;
import com.zhouyu.android_create.application.CatchException;
import com.zhouyu.android_create.manager.ActivityManager;
import com.zhouyu.android_create.tools.CustomSys.MyToast;
import com.zhouyu.android_create.tools.FileTool;
import com.zhouyu.android_create.tools.utils.PhoneMessage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Objects;

public class ErrorActivity extends AppCompatActivity {
    private String errMessageStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);


        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("软件发生未知错误，正在获取错误信息，请稍等！");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        Application.executeThread(() -> {
            errMessageStr = handlerException(CatchException.errorThrowable);
            runOnUiThread(progressDialog::dismiss);
        });

        findViewById(R.id.btn_err_message).setOnClickListener(v -> {
            PhoneMessage.copy(errMessageStr);
            MyToast.show("复制成功",true);
        });

        findViewById(R.id.btn_restart).setOnClickListener(v -> {
                finish();
                ActivityManager.getInstance().finishApplication();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
        });

    }


    //自定义错误处理器
    private String handlerException(Throwable ex) {
        if (ex == null) {  //如果已经处理过这个Exception,则让系统处理器进行后续关闭处理
            return "null";
        }

        String message = ex.getMessage();
        if(message != null){
            if(message.contains("Context.startForegroundService() did not then call Service.startForeground()")
                    || message.contains("android.os.DeadSystemException")){
                return message;
            }
        }

        //获取错误原因
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null){
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String errMsg = writer.toString();
        try {
            long time = new Date().getTime();
            //保存本地
            String path = Application.appCachePath +"/error/"+ time +".err";
            errMsg = errMsg +"\n"+ collectDeviceInfo(true);
            FileTool.commonStream.write(errMsg,path);

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

        }catch (Exception | Error ignored){
        }
        return errMsg;
    }

    /**
     * 收集设备参数信息
     */
    public static String collectDeviceInfo(boolean isLocal) {
        String c;
        if(isLocal){
            c = "\n";
        }else {
            c = "</br>";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("versionName: ").append(PhoneMessage.getAppVersionName()).append(c);
        stringBuilder.append("versionCode: ").append(PhoneMessage.getAppVersionCode()).append(c);
        stringBuilder.append("Android版本: ").append(Build.VERSION.RELEASE).append(c);
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String name = field.getName();
                if(name.contains("CPU")){
                    stringBuilder.append(name).append(": ").append(Objects.requireNonNull(field.get(null))).append(c);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

}

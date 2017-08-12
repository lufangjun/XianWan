package com.xw.xianwan;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class DownLoadReceiver extends BroadcastReceiver {
    long mTaskId;
    String apkName = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        long myDwonloadID = intent.getLongExtra(
                DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        SharedPreferences sPreferences = context.getSharedPreferences(
                "xw", 0);
        mTaskId = sPreferences.getLong("taskid", 0);
        apkName = sPreferences.getString("apkname", "");
        if (mTaskId == myDwonloadID) {
            checkDownloadStatus(context);
//            String serviceString = Context.DOWNLOAD_SERVICE;
//            DownloadManager dManager = (DownloadManager) context
//                    .getSystemService(serviceString);
//            Intent install = new Intent(Intent.ACTION_VIEW);
//            Uri downloadFileUri = dManager
//                    .getUriForDownloadedFile(myDwonloadID);
//            install.setDataAndType(downloadFileUri,
//                    "application/vnd.android.package-archive");
//            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(install);
        }
    }

    //下载到本地后执行安装
    protected void installAPK(Context context, File file) {
        if (!file.exists()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri;

        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //坑 http://www.jianshu.com/p/c58d17073e65
            File newPath = new File(context.getFilesDir().getPath() + "/downloads");
            if (!newPath.exists()) {
                //通过file的mkdirs()方法创建 目录中包含却不存在的文件夹
                newPath.mkdirs();
            }
            String path = newPath.getPath() + "/" + apkName;
            String oldPath = file.getPath();
            copyFile(oldPath, path);
            File newfile = new File(path);
            // 即是在清单文件中配置的authorities
            uri = FileProvider.getUriForFile(context, "com.xw.xianwan.fileprovider", newfile);
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        } else {
            uri = Uri.parse("file://" + file.toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        //在服务中开启activity必须设置flag,后面解释

        context.startActivity(intent);
    }

    DownloadManager downloadManager;

    //检查下载状态
    private void checkDownloadStatus(Context context) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);//筛选下载任务，传入任务ID，可变参数
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    Log.i("DownLoadService", ">>>下载暂停");
                case DownloadManager.STATUS_PENDING:
                    Log.i("DownLoadService", ">>>下载延迟");
                case DownloadManager.STATUS_RUNNING:
                    Log.i("DownLoadService", ">>>正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.i("DownLoadService", ">>>下载完成");
                    //下载完成安装APK
                    String downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "51xianwan" + File.separator + apkName;
                    installAPK(context, new File(downloadPath));
                    break;
                case DownloadManager.STATUS_FAILED:
                    Log.i("DownLoadService", ">>>下载失败");
                    break;
            }
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }
}

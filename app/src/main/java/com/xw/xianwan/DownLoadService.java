package com.xw.xianwan;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownLoadService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.xw.xianwan.action.FOO";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.xw.xianwan.extra.PARAM1";

    public DownLoadService() {
        super("DownLoadService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1) {
        Intent intent = new Intent(context, DownLoadService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                handleActionFoo(param1);
            }
        }
    }

    private void handleActionFoo(String param1) {
        // TODO: Handle action Foo
        downloadAPK(param1);
    }

    DownloadManager downloadManager;
    String apkName = "";
    long mTaskId;

    //使用系统下载器下载
    private void downloadAPK(String versionUrl) {
        //新建一个File，传入文件夹目录
        File file = new File(Environment.getExternalStorageDirectory() + "/51xianwan");
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!file.exists()) {
            //通过file的mkdirs()方法创建<span style="color:#FF0000;">目录中包含却不存在</span>的文件夹
            file.mkdirs();
        }
        //注册广播接收者，监听下载状态
//        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        int last = versionUrl.lastIndexOf("/") + 1;
        apkName = versionUrl.substring(last);
        if (!apkName.contains(".apk")) {
            if (apkName.length() > 10) {
                apkName = apkName.substring(apkName.length() - 10);
            }
            apkName += ".apk";
        }
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(versionUrl));
        request.setAllowedOverRoaming(false);//漫游网络是否可以下载

        //设置文件类型，可以在下载结束后自动打开该文件
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(versionUrl));
        request.setMimeType(mimeString);

        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);

        //sdcard的目录下的download文件夹，必须设置
        request.setDestinationInExternalPublicDir("/51xianwan/", apkName);
//        request.setDestinationInExternalFilesDir(file.getAbsolutePath()),//也可以自己制定下载路径

        //将下载请求加入下载队列
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法
        mTaskId = downloadManager.enqueue(request);
        SharedPreferences sp = getSharedPreferences("xw", MODE_PRIVATE);
        sp.edit().putLong("taskid", mTaskId).commit();//保存此次下载ID
        sp.edit().putString("apkname", apkName).commit();
    }

}

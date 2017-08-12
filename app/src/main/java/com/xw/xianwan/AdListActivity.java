package com.xw.xianwan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.xw.xianwan.util.SystemUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class AdListActivity extends AppCompatActivity {
    private static final int REQUEST_PHONE_STATE = 100;
    private String imei;
    private WebView webView;
    private String appid, secret, appsign;
    private SwipeRefreshLayout swipeLayout;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_list);
        webView = findViewById(R.id.webview);
        initWebView();
        showBackBtn();
        appid = getIntent().getStringExtra("appid");
        secret = getIntent().getStringExtra("secret");
        appsign = getIntent().getStringExtra("appsign");

//Android6.0需要动态获取权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            toast("需要动态获取权限");
            ActivityCompat.requestPermissions(AdListActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PHONE_STATE);
        } else {
//            toast("不需要动态获取权限");
            TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            imei = TelephonyMgr.getDeviceId();
            openUrl();
        }

        swipeLayout = findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(R.color.holo_blue_bright,
                R.color.holo_green_light, R.color.holo_orange_light,
                R.color.holo_red_light);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                //重新刷新页面
                webView.loadUrl(webView.getUrl());
            }
        });

    }

    private void showBackBtn() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        //声明WebSettings子类
        WebSettings webSettings = webView.getSettings();

        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小


        //其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式


        webView.addJavascriptInterface(AdListActivity.this, "android");
    }

    private void openUrl() {
        String keycode = md5(appid + imei + "2" + appsign + secret);
        WebChromeClient webchromeclient = new WebChromeClient() {
            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {
                Toast.makeText(AdListActivity.this, message, Toast.LENGTH_LONG).show();
                result.confirm();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    //隐藏进度条
                    swipeLayout.setRefreshing(false);
                } else {
                    if (!swipeLayout.isRefreshing())
                        swipeLayout.setRefreshing(true);
                }

                super.onProgressChanged(view, newProgress);
            }
        };
        webView.setWebChromeClient(webchromeclient);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.loadUrl("http://h5.51xianwan.com/try/try_list_plus.aspx?ptype=2" + "&deviceid=" + imei + "&appid=" + appid + "&appsign=" + appsign + "&keycode=" + keycode);

    }


    // 定义JS需要调用的方法
    // 被JS调用的方法必须加入@JavascriptInterface注解
    @JavascriptInterface
    public void CheckInstall(String packageName) {
        boolean isInstalled = SystemUtil.isInstalled(AdListActivity.this, packageName);
        if (isInstalled) {

            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:CheckInstall_Return(1)");
                }
            });

        } else {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:CheckInstall_Return(0)");
                }
            });

        }


    }

    @JavascriptInterface
    public void OpenAPP(String packageName) {
//        Toast.makeText(AdListActivity.this, packageName, Toast.LENGTH_SHORT).show();
        doStartApplicationWithPackageName(packageName);
    }

    @JavascriptInterface
    public void InstallAPP(String url) {
//        Toast.makeText(AdListActivity.this, url, Toast.LENGTH_SHORT).show();

        int last = url.lastIndexOf("/") + 1;
        String apkName = url.substring(last);
        if (!apkName.contains(".apk")){
            if (apkName.length()>10){
                apkName=apkName.substring(apkName.length()-10);
            }
            apkName+=".apk";
        }

        checkDownloadStatus(url,apkName);

//        DownLoadService.startActionFoo(AdListActivity.this, url);
    }

    private void checkDownloadStatus(String url,String apkName) {
        boolean isLoading=false;
        DownloadManager.Query query = new DownloadManager.Query();
        DownloadManager downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            String LoadingUrl = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
            if (url.equals(LoadingUrl)){
                isLoading=true;
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch (status) {
                    case DownloadManager.STATUS_PAUSED:
                        Log.i("DownLoadService", ">>>下载暂停");
                    case DownloadManager.STATUS_PENDING:
                        Log.i("DownLoadService", ">>>下载延迟");
                    case DownloadManager.STATUS_RUNNING:
                        Toast.makeText(AdListActivity.this, "正在下载", Toast.LENGTH_SHORT).show();
                        Log.i("DownLoadService", ">>>正在下载");
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        Log.i("DownLoadService", ">>>下载完成");
                        //下载完成安装APK
                    String downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"51xianwan"+ File.separator + apkName;
                    installAPK(new File(downloadPath),apkName);
                        break;
                    case DownloadManager.STATUS_FAILED:
                        Log.i("DownLoadService", ">>>下载失败");
                        break;
                }
            }

        }

        if (!isLoading){
            DownLoadService.startActionFoo(AdListActivity.this, url);
        }
    }

    @JavascriptInterface
    public void Browser(String url) {
//        Toast.makeText(AdListActivity.this, url, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);
    }

    //下载到本地后执行安装
    protected void installAPK(File file,String apkName) {
        if (!file.exists()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri ;

        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //坑 http://www.jianshu.com/p/c58d17073e65
            File newPath =new File(getFilesDir().getPath()+"/downloads");
            if (!newPath.exists()) {
                //通过file的mkdirs()方法创建 目录中包含却不存在的文件夹
                newPath.mkdirs();
            }
            String path= newPath.getPath()+"/"+apkName;
            String oldPath =file.getPath();
            copyFile(oldPath,path);
            File newfile = new File(path);
            // 即是在清单文件中配置的authorities
            uri = FileProvider.getUriForFile(AdListActivity.this, "com.xw.xianwan.fileprovider", newfile);
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);

        } else {
            uri = Uri.parse("file://" + file.toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        //在服务中开启activity必须设置flag,后面解释

        startActivity(intent);
    }

    /**
     * 复制单个文件
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
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }

    /**
     * 加个获取权限的监听
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PHONE_STATE && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            imei = TelephonyMgr.getDeviceId();
            openUrl();
            onCallPermission();
        }

        if (requestCode == 1) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {//没有获得到权限
                Toast.makeText(this, "你不给权限我就不好干事了啦", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    private void doStartApplicationWithPackageName(String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            startActivity(intent);
        }
    }


    public void onCallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断当前系统的SDK版本是否大于23
            //如果当前申请的权限没有授权
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                //第一次请求权限的时候返回false,第二次shouldShowRequestPermissionRationale返回true
                //如果用户选择了“不再提醒”永远返回false。
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this, "Please grant the permission this time", Toast.LENGTH_LONG).show();
                }
                //请求权限
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {//已经授权了就走这条分支
                Log.i("wei", "onClick granted");
            }
        }
    }


}

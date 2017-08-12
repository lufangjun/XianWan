package com.xw.xianwan.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.Iterator;
import java.util.List;

public class SystemUtil
{  
    /** 
     * 检查系统中是否安装了某个应用 
     *  
     * @param context 
     *            你懂的 
     * @param packageName 
     *            应用的包名 
     * @return true表示已安装，否则返回false 
     */  
    public static boolean isInstalled(Context context, String packageName)
    {  
        PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> installedList = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        Iterator<PackageInfo> iterator = installedList.iterator();
  
        PackageInfo info;  
        String name;  
        while(iterator.hasNext())  
        {  
            info = iterator.next();  
            name = info.packageName;  
            if(name.equals(packageName))  
            {  
                return true;  
            }  
        }  
        return false;  
    }




}  
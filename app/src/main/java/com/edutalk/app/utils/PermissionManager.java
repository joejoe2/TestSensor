package com.edutalk.app.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import java.util.ArrayList;

import androidx.core.app.ActivityCompat;

public class PermissionManager {
    private static final String[] requiredPermissions=new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public static ArrayList<String> getLackingPermissions(Context context) {
        ArrayList<String> lackingPermissions=new ArrayList<>();
        for (String permission : requiredPermissions) {
            if(ActivityCompat.checkSelfPermission(context, permission)!= PackageManager.PERMISSION_GRANTED){
                lackingPermissions.add(permission);
            }
        }
        return lackingPermissions;
    }

    public static void requestPermissions(Activity activity, ArrayList<String> permissions){
        if (permissions!=null&&permissions.size()>0){
            ActivityCompat.requestPermissions(activity, permissions.toArray(new String[]{}),1);
        }
    }

    public static void requestPermissionsInSetting(Activity activity, ArrayList<String> permissions){
        if (permissions!=null&&permissions.size()>0){
            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(activity);
            alertDialogBuilder.setTitle("請在設定中許可權限");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    openSettingPage(activity);
                }
            });
            alertDialogBuilder.create().show();
        }
    }

    private static void openSettingPage(Activity activity){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        activity.startActivity(intent);
    }
}

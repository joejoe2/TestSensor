package com.edutalk.app.utils;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VersionManager {
    private static final double currentVersion = 0.41;
    private static boolean hasNewVersion;
    private static final String host="https://iottalk.github.io/applink/edutalk/apk";
    private static String downloadUrl=host+"/index.html";

    public static boolean hasNewVersion(){
        Thread thread = new Thread(()->{
            Request request = new Request.Builder().url(host+"/version.json").get().build();
            try {
                Response response = new OkHttpClient().newBuilder().build().newCall(request).execute();
                JSONObject newVersion = new JSONObject(response.body().string());
                System.out.println(newVersion);
                response.close();
                if (newVersion.optDouble("version", currentVersion)>currentVersion){
                    downloadUrl = newVersion.optString("apk", downloadUrl);
                    hasNewVersion = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return hasNewVersion;
    }

    public static String getDownloadUrl(){
        return downloadUrl;
    }
}

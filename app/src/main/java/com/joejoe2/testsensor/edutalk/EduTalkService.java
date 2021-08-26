package com.joejoe2.testsensor.edutalk;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EduTalkService {
    private static OkHttpClient httpClient;
    private static final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    static {
        httpClient=new OkHttpClient().newBuilder()
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(@NotNull HttpUrl url, @NotNull List cookies) {
                        Log.d("response","cookies = " + cookies);
                        cookieStore.put(url.host(), cookies);
                    }

                    @NotNull
                    @Override
                    public List loadForRequest(@NotNull HttpUrl url) {
                        List cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList();
                    }
                })
                .build();
    }

    public static EduTalkRCConfig getRCConfig(String url){
        //ex. http://phyedu.iottalk.tw/lecture/6/rc/?token=99d42d77825346e4ad24a7653c06ffdb
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = httpClient.newCall(request).execute();
            EduTalkRCConfig eduTalkRCConfig = new EduTalkRCConfig(response.body().string());
            response.close();
            return eduTalkRCConfig;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean bindRC(String url, String deviceID){
        Request request = new Request.Builder().url(url+deviceID).post(RequestBody.create(null, new byte[0])).build();
        try {
            Response response = httpClient.newCall(request).execute();
            String res = response.body().string();
            response.close();
            System.out.println(res);
            return res.equals("{\"state\":\"ok\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean unBindRC(String url){
        Request request = new Request.Builder().url(url).post(RequestBody.create(null, new byte[0])).build();
        try {
            Response response = httpClient.newCall(request).execute();
            String res = response.body().string();
            response.close();
            System.out.println(res);
            return res.equals("{\"state\":\"ok\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

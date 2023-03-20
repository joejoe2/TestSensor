package com.edutalk.app.edutalk;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EduTalkService {
    private static OkHttpClient httpClient;
    private static final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    static {
        /*
        * store cookies statically
        * */
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

    public static EduTalkRCConfig fetchRCConfig(String url) throws IOException, JSONException {
        //ex. http://phyedu.iottalk.tw/lecture/6/rc/?token=99d42d77825346e4ad24a7653c06ffdb
        Request request = new Request.Builder().url(url).build();
        Response response = httpClient.newCall(request).execute();
        EduTalkRCConfig eduTalkRCConfig = new EduTalkRCConfig(new JSONObject(response.body().string()));
        response.close();
        return eduTalkRCConfig;
    }

    public static void bindRC(String url, String deviceID) throws IOException{
        HashMap<String, String> headers=new HashMap<>();
        headers.put("x-csrf-token", getCsrfToken(url));
        headers.put("referer", url+deviceID);
        Request request = new Request.Builder().url(url+deviceID).headers(Headers.of()).headers(Headers.of(headers)).post(RequestBody.create(null, new byte[0])).build();
        System.out.println(request.headers());
        Response response = httpClient.newCall(request).execute();
        String res = response.body().string();
        response.close();
        System.out.println("rc_bind: "+res);
    }

    public static void bind(String url) throws IOException{
        HashMap<String, String> headers=new HashMap<>();
        headers.put("x-csrf-token", getCsrfToken(url));
        headers.put("referer", url);
        Request request = new Request.Builder().url(url).headers(Headers.of()).headers(Headers.of(headers)).post(RequestBody.create(null, new byte[0])).build();
        System.out.println(request.headers());
        Response response = httpClient.newCall(request).execute();
        String res = response.body().string();
        response.close();
        System.out.println(url+" bind: "+res);
    }

    private static String getCsrfToken(String url)throws IOException{
        HttpUrl URL = HttpUrl.parse(url);
        Request request = new Request.Builder().url(URL.scheme()+"://"+HttpUrl.parse(url).host()+"/edutalk/csrf_refresh").get().build();
        Response response = httpClient.newCall(request).execute();
        String token = response.body().string();
        token=token.substring(token.indexOf("\"")+1, token.lastIndexOf("\""));
        return token;
    }

    public static boolean unBindRC(String url) throws IOException {
        HashMap<String, String> headers=new HashMap<>();
        headers.put("x-csrf-token", getCsrfToken(url));
        headers.put("referer", url);
        Request request = new Request.Builder().url(url).headers(Headers.of(headers)).post(RequestBody.create(null, new byte[0])).build();
        try {
            Response response = httpClient.newCall(request).execute();
            String res = response.body().string();
            response.close();
            System.out.println("rc_unbind: "+res);
            return res.equals("{\"state\":\"ok\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

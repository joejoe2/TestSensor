package com.edutalk.app.edutalk;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EduTalkRCConfig implements Serializable {
    public int lectureID;
    public String deviceName;
    public String deviceModel;
    public String csm_url;
    public String rc_bind;
    public List<String> bindCallbacks = new ArrayList<>();
    private String idfs;
    private String iv_list;

    /**
     * read json of rc.index and parse rc config
     * @param jsonResponse
     * @throws JSONException
     */
    public EduTalkRCConfig(JSONObject jsonResponse) throws JSONException {
        lectureID = jsonResponse.getInt("lecture");
        deviceName = jsonResponse.getString("dev");
        deviceModel = jsonResponse.getString("dm_name");
        idfs = jsonResponse.getJSONArray("idfs").toString();

        JSONArray iv_list=new JSONArray();
        JSONArray raw_iv_list = new JSONArray(jsonResponse.getString("iv_list"));
        Set<String> givNames = new HashSet<>();
        for (int i=0;i<raw_iv_list.length();i++){
            JSONObject iv = raw_iv_list.getJSONObject(i);
            String index = ""+(iv.get("index") instanceof String?"":iv.getInt("index"));
            if (!givNames.contains(iv.getString("giv_name")+index)){
                iv_list.put(iv);
                givNames.add(iv.getString("giv_name")+index);
            }
        }
        this.iv_list = iv_list.toString();
        Log.i("test", "EduTalkRCConfig: "+this.iv_list);

        csm_url = jsonResponse.getString("csm_url");
        rc_bind = jsonResponse.getString("rc_bind");
        JSONArray callbacks = jsonResponse.optJSONArray("bind_callbacks");
        if (callbacks!=null)
            for (int i=0;i<callbacks.length();i++){
                bindCallbacks.add(callbacks.getString(i));
            }
    }

    public JSONArray getIdfs() throws JSONException {
        return new JSONArray(idfs);
    }

    public JSONArray getIv_list() throws JSONException {
        return new JSONArray(iv_list);
    }
}

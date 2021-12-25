package com.edutalk.app.edutalk;

import android.content.Context;

import com.edutalk.app.sensor.BaseSensor;
import com.edutalk.app.sensor.DFInfo;
import com.edutalk.app.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import iottalk.AppID;
import iottalk.DAN;
import iottalk.DeviceFeature;

public class EduTalkDAI {
    private boolean aliveFlag;
    private String csmEndpoint;
    private String[] acceptProtos = {"mqtt"};
    private String deviceName;
    private String deviceModel;
    private String BIND_RC_URL;
    private AppID deviceAddr;
    private String userName = null;
    private DAN dan;

    private BaseSensor[] sensors;

    public EduTalkDAI(Context context, String csmEndpoint, String BIND_RC_URL, String deviceName, String deviceModel, BaseSensor[] sensors) {
        this.csmEndpoint = csmEndpoint;
        this.deviceName = deviceName;
        this.deviceModel = deviceModel;
        this.deviceAddr = Utils.getDeviceAddress(context, csmEndpoint, deviceModel, deviceName, true);
        this.BIND_RC_URL = BIND_RC_URL;
        this.sensors = sensors;
    }

    public void start() throws Exception{
        //dai profile
        setupDAN();

        //set consumer of StreamingSensor
        for (BaseSensor sensor : sensors) {
            sensor.setSensorDataConsumerCallBack((float[] sensorData) -> {
                long sendAt=0;
                try {
                    JSONArray data = new JSONArray(sensorData);
                    sendAt = System.currentTimeMillis();
                    if (((DFInfo)sensor.getBaseSensorType()).isNeedTimeStamp())data.put(sensorData.length, sendAt);
                    dan.push(sensor.getId(), data);  //push data
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return sendAt;
            });
        }

        //start sensor and dai to work
        new Thread(()->{
            try {
                System.out.println("DAI started");
                dan.register(); //register and connect
                EduTalkService.bindRC(BIND_RC_URL, deviceAddr.getUUID().toString());
                for (BaseSensor sensor : sensors) {
                    sensor.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupDAN() throws Exception {
        //set idf
        ArrayList<DeviceFeature> inputDeviceFeatures = new ArrayList<>();
        for (BaseSensor sensor: sensors) {
            inputDeviceFeatures.add(new DeviceFeature(sensor.getId(), "idf"));
        }
        //set df
        DeviceFeature[] dfList = new DeviceFeature[inputDeviceFeatures.size()];
        dfList = inputDeviceFeatures.toArray(dfList);
        //set profile
        JSONObject registerProfile = new JSONObject();
        registerProfile.put("model", deviceModel);
        registerProfile.put("u_name", userName);
        dan = new DAN(csmEndpoint, acceptProtos, dfList, deviceAddr, deviceName, registerProfile) {
            @Override
            public boolean onSignal(String command, String df) {
                //when device feature connect or disconnect on joins (first connect or last disconnect only)!
                System.out.println(df + ":" + command);
                return true;
            }
        };
        //avoid mqtt too many in progress problem
        dan.setMaxInflight(sensors.length*200);
    }

    public void stop() {
        Thread t=new Thread(()->{
            try {
                for (BaseSensor sensor : sensors) {
                    sensor.stop();
                }
                EduTalkService.unBindRC(BIND_RC_URL.replace("bind", "unbind")); //useless ?
                dan.disconnect();
                System.out.println("DAI stopped");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
    }
}

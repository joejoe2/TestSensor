package com.edutalk.app.edutalk;

import android.content.Context;

import com.edutalk.app.sensor.BaseSensor;
import com.edutalk.app.sensor.DFInfo;
import com.edutalk.app.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

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

    private HashMap<String, BaseSensor> sensors;

    public EduTalkDAI(Context context, String csmEndpoint, String BIND_RC_URL, String deviceName, String deviceModel, HashMap<String, BaseSensor> sensors) {
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

        //set consumer of sensor
        for (BaseSensor sensor : sensors.values()) {
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

        Thread thread=new Thread(()->{
            try {
                System.out.println("DAI started");
                //register and connect
                dan.register();
                EduTalkService.bindRC(BIND_RC_URL, deviceAddr.getUUID().toString());
                //start sensor
                /*for (BaseSensor sensor : sensors) {
                    sensor.start();
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        //thread.join();
        //if (ex.get()!=null)throw new Exception();
    }

    private void setupDAN() throws Exception {
        //set idf
        ArrayList<DeviceFeature> inputDeviceFeatures = new ArrayList<>();
        for (BaseSensor sensor: sensors.values()) {
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
                sensors.get(df).start();
                return true;
            }
        };
        //avoid mqtt too many in progress problem
        dan.setMaxInflight(sensors.size()*200);
    }

    public void stop() {
        Thread t=new Thread(()->{
            try {
                for (BaseSensor sensor : sensors.values()) {
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

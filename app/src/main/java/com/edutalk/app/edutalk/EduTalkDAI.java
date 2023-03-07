package com.edutalk.app.edutalk;

import android.content.Context;
import android.util.Log;

import com.edutalk.app.sensor.BaseSensor;
import com.edutalk.app.sensor.DFInfo;
import com.edutalk.app.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import iottalk.AppID;
import iottalk.DAN;
import iottalk.DeviceFeature;

public class EduTalkDAI {
    private String csmEndpoint;
    private String[] acceptProtos = {"mqtt"};
    private String deviceName;
    private String deviceModel;
    private String BIND_RC_URL;
    private String BIND_M2_URL;
    private AppID deviceAddr;
    private String userName = null;
    private DAN dan;
    private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

    private HashMap<String, BaseSensor> sensors;

    public EduTalkDAI(Context context, String csmEndpoint, String BIND_RC_URL, String BIND_M2_URL, String deviceName, String deviceModel, HashMap<String, BaseSensor> sensors) {
        this.csmEndpoint = csmEndpoint;
        this.deviceName = deviceName;
        this.deviceModel = deviceModel;
        this.deviceAddr = Utils.getDeviceAddress(context, csmEndpoint, deviceModel, deviceName, true);
        this.BIND_RC_URL = BIND_RC_URL;
        this.BIND_M2_URL = BIND_M2_URL;
        this.sensors = sensors;
    }

    public void start() throws Exception{
        //dai profile
        setupDAN();

        //set consumer of sensor
        for (BaseSensor sensor : sensors.values()) {
            sensor.setSensorDataConsumerCallBack((List<Object> sensorData) -> {
                long sendAt=System.currentTimeMillis();
                try {
                    JSONArray data = new JSONArray();
                    for (Object o : sensorData){
                        data.put(o);
                    }
                    DFInfo dfInfo = (DFInfo)sensor.getBaseSensorType();

                    if (dfInfo.isNeedTimeStamp())
                        data.put(sendAt);
                    if (dfInfo.isNeedDfName())
                        data.put(sensor.getId().replace("-", "_"));
                    Log.i("dai", sensor.getId()+" "+data);
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
                if (BIND_M2_URL!=null) EduTalkService.bindM2(BIND_M2_URL);
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
                //when device feature connect or disconnect on joins
                System.out.println(df + ":" + command);
                if (command.equals("CONNECT"))
                    scheduledExecutor.schedule(()->sensors.get(df).start(), 2, TimeUnit.SECONDS);
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

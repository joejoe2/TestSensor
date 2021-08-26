package com.joejoe2.testsensor.iottalk;

import com.joejoe2.testsensor.sensor.streamsensor.StreamSensor;
import com.joejoe2.testsensor.utils.Callable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import iottalk.AppID;
import iottalk.DAN;
import iottalk.DeviceFeature;

public class IoTTalkDAI {
    private boolean aliveFlag;
    private String csmEndpoint = "http://iottalk2.tw/csm";
    private String[] acceptProtos = {"mqtt"};
    private String deviceName = "Dummy_Test_java";
    private String deviceModel = "Dummy_Device";
    private AppID deviceAddr = new AppID();
    private String userName = null;
    private DAN dan;

    public StreamSensor[] sensors;

    public IoTTalkDAI(String csmEndpoint, String deviceName, String deviceModel, StreamSensor[] sensors) {
        this.csmEndpoint = csmEndpoint;
        this.deviceName = deviceName;
        this.deviceModel = deviceModel;
        this.sensors = sensors;
    }

    public void start() throws Exception{
        //dai profile
        setupDAN();

        //set consumer of StreamingSensor
        for (StreamSensor sensor : sensors) {
            if (sensor.getSensorType().isNeedTimeStamp()){
                sensor.setSensorDataConsumerCallBack((float[] sensorData) -> {
                    try {
                        JSONArray r = new JSONArray(sensorData);
                        r.put(sensorData.length, System.currentTimeMillis());
                        dan.push(sensor.getId(), r, 0, false);  //push data
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }else {
                sensor.setSensorDataConsumerCallBack((float[] sensorData) -> {
                    try {
                        JSONArray r = new JSONArray(sensorData);
                        dan.push(sensor.getId(), r, 0, false);  //push data
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        //start sensor and dai to work
        new Thread(()->{
            try {
                System.out.println("DAI started");
                dan.register(); //register and connect
                for (StreamSensor sensor : sensors) {
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
        for (StreamSensor sensor: sensors) {
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
                if (command.equals("CONNECT")) {
                    System.out.println(df + ":" + command);
                } else if (command.equals("DISCONNECT")) {
                    System.out.println(df + ":" + command);
                }
                return true;
            }
        };
        //avoid mqtt too many in progress problem
        dan.setMaxflight(sensors.length*200);
    }

    public void stop() {
        new Thread(()->{
            try {
                for (StreamSensor sensor : sensors) {
                    sensor.stop();
                }
                dan.disconnect();
                System.out.println("DAI stopped");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

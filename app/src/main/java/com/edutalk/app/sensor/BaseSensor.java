package com.edutalk.app.sensor;

import java.util.List;

public abstract class BaseSensor {
    protected String id;
    public String getId(){
        return id;
    }
    protected BaseSensorType baseSensorType;
    public BaseSensorType getBaseSensorType() {
        return baseSensorType;
    }
    protected OnSignalCallBack onSignalCallback;
    protected OnConsumeCallback onConsumeCallback;

    public BaseSensor(String id, BaseSensorType baseSensorType) {
        this.id = id;
        this.baseSensorType = baseSensorType;
        this.onSignalCallback = (float[] sensorData) -> {}; //do nothing...
        this.onConsumeCallback = (List<Object> sensorData) -> {return 0;}; //do nothing...
    }

    public void setOnSensorSignalCallBack(OnSignalCallBack onSignalCallback){
        this.onSignalCallback = onSignalCallback;
    };
    public void setSensorDataConsumerCallBack(OnConsumeCallback onConsumeCallback){
        this.onConsumeCallback = onConsumeCallback;
    };
    abstract public void start();
    public abstract void startSensing();
    abstract protected void startConsuming();
    abstract public void stop();
    abstract public void stopSensing();
    abstract protected void stopConsuming();
}

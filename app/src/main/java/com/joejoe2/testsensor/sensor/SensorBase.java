package com.joejoe2.testsensor.sensor;

public abstract class SensorBase {
    protected String id;
    public String getId(){
        return id;
    }
    protected SensorType sensorType;
    public SensorType getSensorType() {
        return sensorType;
    }
    protected OnSignalCallBack onSignalCallback;
    protected OnConsumeCallback onConsumeCallback;

    public SensorBase(String id, SensorType sensorType) {
        this.id = id;
        this.sensorType = sensorType;
        this.onSignalCallback = (float[] sensorData) -> {}; //do nothing...
        this.onConsumeCallback = (float[] sensorData) -> {}; //do nothing...
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

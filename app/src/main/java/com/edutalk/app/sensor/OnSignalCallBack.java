package com.edutalk.app.sensor;

/**
 * this is a interface for Sensor, the doOnSignal() is when the sensor
 * get a new value, this is used to do some in-app action (ex. update value on ui)
 * <br><br>
 * !!! notice that if you want to do some heavy work in the doOnSignal (ex. update ui) ,
 * please use asyn or thread manner, otherwise the sensor cannot get a new value util your work done.
 */
public interface OnSignalCallBack {
    public void doOnSignal(float[] sensorData);
}

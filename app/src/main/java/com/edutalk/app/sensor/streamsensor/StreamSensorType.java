package com.edutalk.app.sensor.streamsensor;

import android.hardware.Sensor;

import com.edutalk.app.sensor.BaseSensorType;
import com.edutalk.app.sensor.DFInfo;

public enum StreamSensorType implements BaseSensorType, DFInfo {
    Acceleration(Sensor.TYPE_ACCELEROMETER),
    Gyroscope(Sensor.TYPE_GYROSCOPE),
    Orientation(Sensor.TYPE_ORIENTATION),
    Magnetometer(Sensor.TYPE_MAGNETIC_FIELD),
    ;

    private final int nativeSensorCode;
    StreamSensorType(int sensorCode) {
        this.nativeSensorCode = sensorCode;
    }
    public int getNativeSensorCode() { return nativeSensorCode; }

    public String getAcceptUnit(){
        switch (this){
            case Acceleration:
                return "m/s^2";
            case Gyroscope:
                return "degree/s";
            case Orientation:
                return "degree";
            case Magnetometer:
                return "μT";
            default:
                throw new Error("undefined unit of SensorType !");
        }
    }

    public String[] getDataDimensionNames(){
        switch (this){
            case Acceleration:
            case Magnetometer:
                return new String[]{"x", "y", "z"};
            case Gyroscope:
            case Orientation:
                return new String[]{"α", "β", "γ"};
            default:
                throw new Error("undefined DimensionNames of SensorType !");
        }
    }

    public float[] convertToAcceptUnit(float[] data){
        float[] res=new float[data.length];

        switch (this){
            case Acceleration: //same, m/s^2
            case Orientation: //same, degree
            case Magnetometer: //same, μT
                for (int i=0;i<3;i++){
                    res[i] = data[i];
                }
                break;
            case Gyroscope: // rad/s to degree/s
                for (int i=0;i<3;i++){
                    res[i] = (float)(data[i]*180/Math.PI);
                }
                break;
            default: //same
                for (int i=0;i<3;i++){
                    res[i] = data[i];
                }
                //throw new Error("undefined convert of SensorType !");
        }

        return res;
    }

    private boolean needTimeStamp = false;
    @Override
    public boolean isNeedTimeStamp() {
        return needTimeStamp;
    }

    @Override
    public void setNeedTimestamp(boolean needTimeStamp) {
        this.needTimeStamp = needTimeStamp;
    }

    @Override
    public String getAlias() {
        return name();
    }
}

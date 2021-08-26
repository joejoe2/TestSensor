package com.joejoe2.testsensor.sensor.streamsensor;

import android.hardware.Sensor;

import java.util.Arrays;

/**
 * this enum.toString() is same as iottalk device feature name (inconsistent with edutalk)
 * , and getValue() is same as android sensor type
 * , and featureSubName() is semantic feature name ex. Acceleration, Gyroscope, ...
 */
public enum StreamSensorType {
    Acceleration_I(Sensor.TYPE_ACCELEROMETER),
    Gyroscope_I(Sensor.TYPE_GYROSCOPE),
    Orientation_I(Sensor.TYPE_ORIENTATION),
    ;

    private final int id;
    StreamSensorType(int sensorType) {
        this.id = sensorType;
    }
    public int getValue() { return id; }

    @Override
    public String toString() {
        return this.name().replace("_", "-");
    }

    public String semanticAlias(){
        return this.name().substring(0, this.name().indexOf("_"));
    }

    public String getUnit(){
        switch (this){
            case Acceleration_I:
                return "m/s^2";
            case Gyroscope_I:
                return "degree/s";
            case Orientation_I:
                return "degree";
            default:
                throw new Error("undefined unit of SensorType !");
        }
    }

    public String[] getDataDimensions(){
        switch (this){
            case Acceleration_I:
                return new String[]{"x", "y", "z"};
            case Gyroscope_I:
            case Orientation_I:
                return new String[]{"α", "β", "γ"};
            default:
                throw new Error("undefined output of SensorType !");
        }
    }

    public float[] convertToAcceptUnit(float[] data){
        float[] res=new float[data.length];

        switch (this){
            case Acceleration_I:
            case Orientation_I:
                for (int i=0;i<data.length;i++){
                    res[i] = data[i];
                }
                break;
            case Gyroscope_I: //iottalk need degree/s
                for (int i=0;i<data.length;i++){
                    res[i] = (float)(data[i]*180/Math.PI);
                }
                break;
            default:
                throw new Error("undefined convert of SensorType !");
        }

        return res;
    }
}

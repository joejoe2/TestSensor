package com.joejoe2.testsensor.sensor;

import com.joejoe2.testsensor.sensor.streamsensor.StreamSensorType;
import com.joejoe2.testsensor.sensor.triggersensor.RangeSensorType;

import java.io.Serializable;

public class SensorType implements Serializable {
    private StreamSensorType streamSensorType;
    private RangeSensorType rangeSensorType;
    private boolean needTimeStamp;
    public boolean isNeedTimeStamp() {
        return needTimeStamp;
    }

    public SensorType(StreamSensorType streamSensorType, boolean needTimeStamp) {
        this.streamSensorType = streamSensorType;
        this.needTimeStamp = needTimeStamp;
    }
    public boolean isStreamSensor(){
        return streamSensorType!=null;
    }
    public StreamSensorType getStreamSensorType(){
        if (streamSensorType!=null)return streamSensorType;
        throw new Error("this is not StreamSensorType !");
    }

    public SensorType(RangeSensorType rangeSensorType) { this.rangeSensorType = rangeSensorType; }
    public boolean isRangeSensor(){
        return rangeSensorType !=null;
    }
    public RangeSensorType getRangeSensorType(){
        if (rangeSensorType !=null)return rangeSensorType;
        throw new Error("this is not TriggerSensorType !");
    }
}

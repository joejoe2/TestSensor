package com.edutalk.app.sensor.unsupportsensor;

import com.edutalk.app.sensor.BaseSensor;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UnSupportSensor extends BaseSensor {
    private ArrayList<Float> defaultVals;

    private ConcurrentLinkedQueue<float[]> dataQueue;

    public UnSupportSensor(String id, UnSupportSensorType sensorType) {
        super(id, sensorType);
        defaultVals = new ArrayList<>();
        dataQueue = new ConcurrentLinkedQueue<>();
    }

    public ArrayList<Float> getDefaultVals() {
        return defaultVals;
    }

    @Override
    public void start() {
        dataQueue.clear();
        startSensing();
        startConsuming();
    }

    @Override
    public void startSensing() {
        float[] data=new float[defaultVals.size()];
        for(int i=0;i<defaultVals.size();i++){
            data[i]=defaultVals.get(i);
        }

        dataQueue.add(data);
    }

    @Override
    protected void startConsuming() {
        float[] data;
        if ((data = dataQueue.poll())!=null)
            onConsumeCallback.consume(data);
    }

    @Override
    public void stop() {
        stopSensing();
        stopConsuming();
    }

    @Override
    public void stopSensing() {
    }

    @Override
    protected void stopConsuming() {
        dataQueue.clear();
    }
}

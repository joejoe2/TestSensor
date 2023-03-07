package com.edutalk.app.sensor.unsupportsensor;

import com.edutalk.app.sensor.BaseSensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UnSupportSensor extends BaseSensor {
    private ArrayList<Object> defaultVals;

    private ConcurrentLinkedQueue<Object[]> dataQueue;

    public UnSupportSensor(String id, UnSupportSensorType sensorType) {
        super(id, sensorType);
        defaultVals = new ArrayList<>();
        dataQueue = new ConcurrentLinkedQueue<>();
    }

    public ArrayList<Object> getDefaultVals() {
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
        Object[] data=new Object[defaultVals.size()];
        for(int i=0;i<defaultVals.size();i++){
            data[i]=defaultVals.get(i);
        }

        dataQueue.add(data);
    }

    @Override
    protected void startConsuming() {
        Object[] data;
        if ((data = dataQueue.poll())!=null)
            onConsumeCallback.consume(IntStream.range(0, data.length).mapToObj(i->data[i]).collect(Collectors.toList()));
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

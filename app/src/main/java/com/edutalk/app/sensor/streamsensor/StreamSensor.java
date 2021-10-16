package com.edutalk.app.sensor.streamsensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.edutalk.app.sensor.BaseSensor;
import com.edutalk.app.utils.Utils;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

/**
 * this class implements android streaming sensor with producer-consumer manner<br>
 * the android streaming sensor produce values into dataQueue, and dataConsumer consume
 * it on another thread
 * <br><br>
 * sensorType is bind with iottalk input device feature<br>
 * onSignalCallback is called when android sensor get a value<br>
 * onConsumeCallback is called to process a value retrieve from dataQueue
 */
public class StreamSensor extends BaseSensor {
    private SensorManager sensorManager;
    private Sensor sensor;
    private StreamSensorType streamSensorType;
    private SensorEventListener sensorEventListener;
    private int sampleRate;
    private long consumeIntervalNS;

    private ConcurrentLinkedQueue<float[]> dataQueue;
    private Thread dataConsumer;
    private boolean isDataConsumerExit;

    private ArrayList<Long> sampleTime=new ArrayList<>();
    private ArrayList<Long> sendTime=new ArrayList<>();

    public StreamSensor(String id, SensorManager sensorManager, StreamSensorType streamSensorType, int sampleRate) {
        super(id, streamSensorType);
        this.streamSensorType = (StreamSensorType) super.baseSensorType;
        this.sensorManager = sensorManager;
        this.sampleRate = sampleRate>=100?sampleRate+10:sampleRate; // avoid sensor lazy(a little bit smaller than 200) on 5z
        this.consumeIntervalNS = getConsumeIntervalNS(this.sampleRate);
        this.sensor = this.sensorManager.getDefaultSensor(this.streamSensorType.getNativeSensorCode());
    }

    @Override
    public void start(){
        this.dataQueue = new ConcurrentLinkedQueue<>();
        startSensing();
        startConsuming();
    }

    @Override
    public void startSensing(){
        this.sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                sampleTime.add(System.currentTimeMillis());
                float[] data = streamSensorType.convertToAcceptUnit(sensorEvent.values);
                onSignalCallback.doOnSignal(data);
                dataQueue.add(data);
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        sensorManager.registerListener(this.sensorEventListener, sensor, getSamplePeriodUS(sampleRate));
    }

    @Override
    protected void startConsuming(){
        dataConsumer = new Thread(() -> {
            long t;
            while (!isDataConsumerExit) {
                float[] data;
                if ((data = dataQueue.poll())!=null){
                    t = onConsumeCallback.consume(data);
                    sendTime.add(t);
                }else {
                    LockSupport.parkNanos(consumeIntervalNS);
                }
            }
        });
        dataConsumer.start();
    }

    @Override
    public void stop(){
        stopSensing();
        stopConsuming();
        if (sampleTime.size() - sendTime.size() > 0) {
            sampleTime.subList(0, sampleTime.size() - sendTime.size()).clear();
        }
        Utils.saveTimeStampData(sampleTime, sendTime, System.currentTimeMillis()+"_"+getId());
    }

    @Override
    public void stopSensing(){
        sensorManager.unregisterListener(this.sensorEventListener, sensor);
    }

    @Override
    protected void stopConsuming(){
        isDataConsumerExit = true;
        dataConsumer = null;
        dataQueue.clear();
    }

    private int getSamplePeriodUS(int sampleRate){
        if (sampleRate<=0)return SensorManager.SENSOR_DELAY_FASTEST;
        return 1000000/sampleRate;
    }

    private int getConsumeIntervalNS(int sampleRate){
        if (sampleRate<=0)return 1000;
        return 1000000000/sampleRate/1000; // ex. 200 hz will get 5000 ns( 5 us )
    }
}

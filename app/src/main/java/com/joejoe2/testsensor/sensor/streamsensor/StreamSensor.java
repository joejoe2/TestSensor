package com.joejoe2.testsensor.sensor.streamsensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.joejoe2.testsensor.sensor.SensorBase;
import com.joejoe2.testsensor.sensor.SensorType;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

/**
 * this class implements android streaming sensor with producer-consumer manner<br>
 * the android streaming sensor produce values into dataQueue, and dataConsumer consume
 * it on another thread
 * <br><br>
 * sensorType is bind with iottalk input device feature<br>
 * onSignalCallback is called when android sensor get a value<br>
 * consumable is a interface to defined how to process a value retrieve from dataQueue
 */
public class StreamSensor extends SensorBase {
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;
    private int sampleRate;
    private long consumeIntervalNS;

    private ConcurrentLinkedQueue<float[]> dataQueue;
    private Thread dataConsumer;
    private boolean isDataConsumerExit;

    public StreamSensor(String id, SensorManager sensorManager, SensorType sensorType, int sampleRate) {
        super(id, sensorType);
        this.sensorManager = sensorManager;
        this.sampleRate = sampleRate>=100?sampleRate+10:sampleRate; // avoid sensor lazy(a little bit smaller than 200) on 5z
        this.consumeIntervalNS = getConsumeIntervalNS(sampleRate);
        this.sensor = this.sensorManager.getDefaultSensor(this.sensorType.getStreamSensorType().getValue());
    }

    @Override
    public void start(){
        dataQueue = new ConcurrentLinkedQueue<>();
        startSensing();
        startConsuming();
        System.out.println(id+"("+sensorType.getStreamSensorType().semanticAlias()+") stared");
    }

    @Override
    public void startSensing(){
        this.sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float[] data = sensorType.getStreamSensorType().convertToAcceptUnit(sensorEvent.values);
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
            while (!isDataConsumerExit) {
                float[] data;
                if ((data = dataQueue.poll())!=null){
                    onConsumeCallback.consume(data);
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
        System.out.println(id+"("+sensorType.getStreamSensorType().semanticAlias()+") stopped");
    }

    @Override
    public void stopSensing(){
        sensorManager.unregisterListener(this.sensorEventListener, sensor);
    }

    @Override
    protected void stopConsuming(){
        isDataConsumerExit = true;
        dataConsumer = null;
        dataQueue = null;
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

package com.edutalk.app.sensor.triggersensor;

import android.util.Log;
import android.view.View;

import com.edutalk.app.customUI.SeekBar;
import com.edutalk.app.sensor.BaseSensor;

import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RangeSensor extends BaseSensor {
    private View uiComponent;
    private float min, max,  step, defaultVal;
    private int stepPrecision;

    private boolean isSensing;
    private long consumeIntervalMS = 50;
    private ConcurrentLinkedQueue<float[]> dataQueue;
    private Thread dataConsumer;
    private boolean dataConsumerExit;

    public RangeSensor(String id, View uiComponent , TriggerSensorType triggerSensorType, float min, float max, float step, float defaultVal) {
        super(id, triggerSensorType);
        this.uiComponent = uiComponent;
        this.min = min;
        this.max = max;
        this.step = step;
        this.defaultVal = defaultVal;
        this.stepPrecision = (""+step).length()-1-(""+step).indexOf(".");
        dataQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void start() {
        dataQueue.clear();
        startSensing();
        startConsuming();
    }

    @Override
    public void startSensing() {
        if (uiComponent.getClass().getCanonicalName().equals(TriggerSensorType.RangeSlider.getCorrespondingUI())){
            SeekBar seekBar = ((SeekBar) uiComponent);
            seekBar.setSeekBarRange((int) (1.0/step*(max-min)), (int) ((defaultVal-min)/step));
            seekBar.setValue(String.format("%."+stepPrecision+"f", defaultVal));
            seekBar.setSeekBarListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(android.widget.SeekBar seekBar, int i, boolean b) { }
                @Override
                public void onStartTrackingTouch(android.widget.SeekBar seekBar) { }
                @Override
                public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
                    if (!isSensing)return;
                    float[] data = new float[]{seekBar.getProgress()*step+min};
                    Log.i("test", "onStopTrackingTouch: "+data[0]);
                    onSignalCallback.doOnSignal(data);
                    dataQueue.add(data);
                }
            });
        }
        dataQueue.add(new float[]{defaultVal});
        isSensing=true;
    }

    @Override
    protected void startConsuming() {
        dataConsumer = new Thread(() -> {
            while (!dataConsumerExit) {
                float[] data;
                if ((data = dataQueue.poll())!=null){
                    onConsumeCallback.consume(IntStream.range(0, data.length).mapToObj(i->data[i]).collect(Collectors.toList()));
                }else {
                    try {
                        Thread.sleep(consumeIntervalMS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        dataConsumer.start();
    }

    @Override
    public void stop() {
        stopSensing();
        stopConsuming();
    }

    @Override
    public void stopSensing() {
        isSensing=false;
    }

    @Override
    protected void stopConsuming() {
        dataConsumerExit = true;
        dataConsumer = null;
        dataQueue.clear();
    }
}

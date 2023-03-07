package com.edutalk.app.sensor;

import java.util.List;

/**
 * this is a interface for StreamingSensor, the consume() is to defined
 * dataConsumer in StreamingSensor how to process each data from dataQueue,
 * this is used to do some out-app action (ex. write value to file or send value via web request)
 * <br><br>
 * !!! notice that if you want to do some heavy work in the consume() (ex. web request) ,
 * please use asyn or thread manner, otherwise the dataConsumer will be blocked util your work done.
 */
public interface OnConsumeCallback {
    public long consume(List<Object> sensorData);
}

package com.edutalk.app.sensor.unsupportsensor;

import com.edutalk.app.sensor.BaseSensorType;
import com.edutalk.app.sensor.DFInfo;

public enum UnSupportSensorType implements BaseSensorType, DFInfo {
    InputBox("Input Box"), Morsensor("Morsensor");
    private boolean needTimeStamp = false;

    private final String alias;
    UnSupportSensorType(String s) {
        alias=s;
    }

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
        return alias;
    }
}

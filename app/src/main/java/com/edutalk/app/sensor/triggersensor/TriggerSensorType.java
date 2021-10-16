package com.edutalk.app.sensor.triggersensor;

import com.edutalk.app.customUI.SeekBarWithLabel;
import com.edutalk.app.sensor.BaseSensorType;
import com.edutalk.app.sensor.DFInfo;


public enum TriggerSensorType implements BaseSensorType, DFInfo {
    RangeSlider(SeekBarWithLabel.class.getCanonicalName()),
    ;

    private final String uiClassName;
    TriggerSensorType(String viewClass) {
        this.uiClassName =viewClass;
    }
    public String getCorrespondingUI() { return uiClassName; }

    @Override
    public String getDFAlias() {
        return name();
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
}

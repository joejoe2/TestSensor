package com.edutalk.app.sensor.triggersensor;

import com.edutalk.app.customUI.SeekBar;
import com.edutalk.app.sensor.BaseSensorType;
import com.edutalk.app.sensor.DFInfo;


public enum TriggerSensorType implements BaseSensorType, DFInfo {
    RangeSlider(SeekBar.class.getCanonicalName()),
    ;

    private final String uiClassName;
    TriggerSensorType(String viewClass) {
        this.uiClassName = viewClass;
    }
    public String getCorrespondingUI() { return uiClassName; }

    private boolean needTimeStamp = false;
    @Override
    public boolean isNeedTimeStamp() {
        return needTimeStamp;
    }

    @Override
    public void setNeedTimestamp(boolean needTimeStamp) {
        this.needTimeStamp = needTimeStamp;
    }

    private boolean needDfName = false;

    @Override
    public boolean isNeedDfName() {
        return needDfName;
    }

    @Override
    public void setNeedDfName(boolean needDfName) {
        this.needDfName = needDfName;
    }

    @Override
    public String getAlias() {
        switch (this){
            case RangeSlider:
                return "Range Slider";
            default:
                return this.name();
        }
    }
}

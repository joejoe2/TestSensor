package com.joejoe2.testsensor.sensor;

public interface DFInfo {
    String getDFAlias();

    boolean isNeedTimeStamp();

    void setNeedTimestamp(boolean needTimeStamp);
}

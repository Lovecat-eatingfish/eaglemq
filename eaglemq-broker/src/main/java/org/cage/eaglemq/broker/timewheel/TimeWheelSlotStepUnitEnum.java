package org.cage.eaglemq.broker.timewheel;

public enum TimeWheelSlotStepUnitEnum {

    SECOND("second"),
    MINUTE("minute"),
    HOUR("hour"),
    ;

    TimeWheelSlotStepUnitEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    String code;
}

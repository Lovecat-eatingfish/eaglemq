package org.cage.eaglemq.common.enums;

public enum MessageSendWay {

    SYNC(1),
    ASYNC(2),
    ;

    int code;

    MessageSendWay(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

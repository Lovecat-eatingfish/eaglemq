package org.cage.eaglemq.common.enums;

import lombok.Getter;

@Getter
public enum SendMessageToBrokerResponseStatus {

    SUCCESS(0, "发送成功"),
    FAIL(1, "发送失败"),
    ;

    int code;
    String desc;

    SendMessageToBrokerResponseStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

package org.cage.eaglemq.common.enums;

import lombok.Getter;

@Getter
public enum AckStatus {

    SUCCESS(1),
    FAIL(0),
    ;

    AckStatus(int code) {
        this.code = code;
    }

    int code;
}

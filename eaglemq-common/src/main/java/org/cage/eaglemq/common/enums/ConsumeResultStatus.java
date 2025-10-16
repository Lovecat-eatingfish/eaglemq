package org.cage.eaglemq.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConsumeResultStatus {

    CONSUME_SUCCESS(1),
    CONSUME_LATER(2),
    ;

    int code;
}

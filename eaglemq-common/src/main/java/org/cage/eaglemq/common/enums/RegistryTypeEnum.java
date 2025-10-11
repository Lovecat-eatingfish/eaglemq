package org.cage.eaglemq.common.enums;

import lombok.Getter;

@Getter
public enum RegistryTypeEnum {

    PRODUCER("producer"),
    CONSUMER("consumer"),
    BROKER("broker");
    private final String code;

    RegistryTypeEnum(String code) {
        this.code = code;
    }

}

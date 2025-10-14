package org.cage.eaglemq.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BrokerRegistryRoleEnum {

    MASTER("master"),
    SLAVE("slave"),
    SINGLE("single"),
    ;

    String code;

    public static BrokerRegistryRoleEnum of(String code) {
        for (BrokerRegistryRoleEnum brokerRegistryEnum : BrokerRegistryRoleEnum.values()) {
            if (brokerRegistryEnum.getCode().equals(code)) {
                return brokerRegistryEnum;
            }
        }
        return null;
    }
}

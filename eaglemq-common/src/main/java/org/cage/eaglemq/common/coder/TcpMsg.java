package org.cage.eaglemq.common.coder;

import lombok.Data;
import org.cage.eaglemq.common.constants.BrokerConstants;

/**
 * ClassName: TcpMsg
 * PackageName: org.cage.eaglemq.common.coder
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午12:51
 * @Version: 1.0
 */
@Data
public class TcpMsg {

    //魔数
    private short magic;
    //表示请求包的具体含义
    private int code;
    //消息长度
    private int len;
    private byte[] body;

    public TcpMsg(int code, byte[] body) {
        this.magic = BrokerConstants.DEFAULT_MAGIC_NUM;
        this.code = code;
        this.body = body;
        this.len = body.length;
    }
}

package com.debugers.alltv.enumType;

import lombok.Getter;

/**
 *bilibili操作码
 */
@Getter
public enum BiliBiliOperationEnum {
    HANDSHAKE(0),
    HANDSHAKE_REPLY(1),
    HEARTBEAT(2),
    HEARTBEAT_REPLY(3),
    SEND_MSG(4),
    SEND_MSG_REPLY(5),
    DISCONNECT_REPLY(6),
    AUTH(7),
    AUTH_REPLY(8),
    RAW(9),
    PROTO_READY(10),
    PROTO_FINISH(11),
    CHANGE_ROOM(12),
    CHANGE_ROOM_REPLY(13),
    REGISTER(14),
    REGISTER_REPLY(15),
    UNREGISTER(16),
    UNREGISTER_REPLY(17),
    MinBusinessOp(1000),
    MaxBusinessOp(10000),
    ;
    int value;

    BiliBiliOperationEnum(int i) {
        this.value = i;
    }
}

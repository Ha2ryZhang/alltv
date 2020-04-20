package com.debugers.alltv.enumType;

/**
 * 斗鱼官方api，但目前开放品台但api都是有验证但，也不知道什么时候会失效。
 * @author harryzhang
 */
public enum DouYuOpenApi {
    //统一后面+roomId
    ROOM_INFO("http://open.douyucdn.cn/api/RoomApi/room/"),
    TOP_ROOM("http://open.douyucdn.cn/api/RoomApi/live/"),
    ;

    private String url;

    DouYuOpenApi(String url) {
        this.url = url;
    }

    public String getValue() {
        return url;
    }

    @Override
    public String toString() {
        return getValue();
    }
}

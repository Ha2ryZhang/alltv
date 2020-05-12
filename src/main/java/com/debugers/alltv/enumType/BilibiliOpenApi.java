package com.debugers.alltv.enumType;

/**
 * bilibili官方api
 * @author harryzhang
 * https://api.live.bilibili.com/room/v1/RoomRecommend/biliIndexRecList
 */
public enum BilibiliOpenApi {
    //统一后面+roomId
    SERVER_CONFIG("https://api.live.bilibili.com/room/v1/Danmu/getConf?id="),
    ROOM_INIT("https://api.live.bilibili.com/room/v1/Room/room_init?id="),
    PLAY_URL("https://api.live.bilibili.com/room/v1/Room/playUrl?cid=")
    ;

    private String url;

    BilibiliOpenApi(String url) {
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

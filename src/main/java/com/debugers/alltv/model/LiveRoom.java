package com.debugers.alltv.model;

import lombok.Data;

import java.util.Date;

@Data
public class LiveRoom implements Comparable<LiveRoom> {
    private String roomId;//房间号
    private String com;//哪个平台
    private String cateId;//分类id
    private String roomThumb;//房间缩略图
    private String cateName;//分类名
    private String roomName;//房间名
    private Integer roomStatus; //1 开播 2 未开播
    private Date startTime;
    private String ownerName;//主播名
    private String avatar;//主播头像
    private Long online; //热度
    private String realUrl; //真是直播地址

    @Override
    public int compareTo(LiveRoom room) {
        return (int) (room.getOnline()-online);
    }
}

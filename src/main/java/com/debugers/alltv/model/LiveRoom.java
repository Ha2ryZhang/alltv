package com.debugers.alltv.model;

import lombok.Data;

import java.util.Date;

@Data
public class LiveRoom {
    private String roomId;
    private String com;//哪个平台
    private String cateId;
    private String roomThumb;
    private String cateName;//分类名
    private String roomName;
    private Integer roomStatus; //1 开播 2 未开播
    private Date startTime;
    private String ownerName;
    private String avatar;
    private Long online; //热度
    private String realUrl; //真是直播地址
}

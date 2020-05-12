package com.debugers.alltv.model.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

@Data
public class DouYuDTO {
    @JSONField(name = "room_id")
    private String roomId;
    @JSONField(name = "cate_id")
    private String cateId;
    @JSONField(name = "room_src")
    private String roomThumb;
    @JSONField(name = "game_name")
    private String cateName;//分类名
    @JSONField(name = "room_name")
    private String roomName;
    @JSONField(name = "show_status")
    private Integer roomStatus; //1 开播 2 未开播
    @JSONField(name = "show_time")
    private Date startTime;
    @JSONField(name = "nickname")
    private String ownerName;
    @JSONField(name = "avatar")
    private String avatar;
    @JSONField(name = "online")
    private Long online; //热度
    private String realUrl; //真是直播地址
}

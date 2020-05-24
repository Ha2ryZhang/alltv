package com.debugers.alltv.model.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class BilibiliDTO {
    @JSONField(name = "roomid")
    private String roomId;
    @JSONField(name = "area")
    private String area;
    @JSONField(name = "area_v2_id")
    private String area_v2_id;
    @JSONField(name = "cover")
    private String roomThumb;
    @JSONField(name = "area_v2_name")
    private String cateName;//分类名
    @JSONField(name = "title")
    private String roomName;
    @JSONField(name = "uname")
    private String ownerName;
    @JSONField(name = "face")
    private String avatar;
    @JSONField(name = "online")
    private Long online; //热度
}

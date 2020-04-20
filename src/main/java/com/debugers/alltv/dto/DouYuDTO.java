package com.debugers.alltv.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(description = "斗鱼房间信息")
public class DouYuDTO {
    @ApiModelProperty("房间id")
    private String roomId;
    @ApiModelProperty(value = "直播分类id",example = "1")
    private String cateId;
    @ApiModelProperty("房间预览缩略图")
    private String roomThumb;
    @ApiModelProperty("分类名")
    private String cateName;//分类名
    @ApiModelProperty("房间名")
    private String roomName;
    @ApiModelProperty(value = "开播状态",notes = "1 开播 2 未开播")
    private Integer roomStatus; //1 开播 2 未开播
    @ApiModelProperty("本次开播时间,如果没开播则是上一次开播时间")
    private Date startTime;
    @ApiModelProperty("主播名")
    private String ownerName;
    @ApiModelProperty("主播头像")
    private String avatar;
    @ApiModelProperty("斗鱼热度")
    private Long online; //斗鱼叫热度
    @ApiModelProperty("真实直播源地址")
    private String realUrl; //真是直播地址
}

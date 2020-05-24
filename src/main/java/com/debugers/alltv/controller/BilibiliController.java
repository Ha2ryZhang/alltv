package com.debugers.alltv.controller;

import com.debugers.alltv.model.LiveRoom;
import com.debugers.alltv.result.Result;
import com.debugers.alltv.service.BilibiliService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "Bilibili直播")
@RestController
@RequestMapping("api/bilibili")
public class BilibiliController {
    public final BilibiliService bilibiliService;

    public BilibiliController(BilibiliService bilibiliService) {
        this.bilibiliService = bilibiliService;
    }
    @ApiOperation("获取直播真实地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roomId", value = "房间号,.com后面的那一串数字", required = true, dataType = "String"),
    })
    @GetMapping("real_url/{roomId}")
    public Result<Map<String, String>> getRealUrl(@PathVariable(value = "roomId") String roomId){
        Map<String, String> result = new HashMap<>();
        result.put("realUrl", bilibiliService.getRealUrl(roomId));
        return Result.success(result);
    }
    @ApiOperation("获取bilibili热门房间")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "areaId", value = "B站区域id", required = true, dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "pageSize", required = true, dataType = "int"),
            @ApiImplicitParam(name = "page", value = "page", required = true, dataType = "int")
    })
    @GetMapping("rank")
    public Result<List<LiveRoom>> getTopRoomsByAreaId(Integer areaId,Integer pageSize,Integer page){
        List<LiveRoom> topRooms = bilibiliService.getTopRooms(areaId, pageSize, page);
        return Result.success(topRooms);
    }
    @ApiOperation("获取热门房间")
    @GetMapping("top_rooms")
    public Result<List<LiveRoom>> getTopRooms(Integer pageSize,Integer pageNum){
        //默认0为综合
        List<LiveRoom> topRooms = bilibiliService.getTopRooms(0, pageSize, pageNum);
        return Result.success(topRooms);
    }
    @ApiOperation("github bilibili api")
    @GetMapping("getmore")
    public String getMoreApi(){
        return "更多请参见：https://github.com/lovelyyoshino/Bilibili-Live-API";
    }
    @GetMapping("checkLiveStatus")
    public Result<Boolean> getLiveStatus(String roomId){
        Boolean liveStatus = bilibiliService.getLiveStatus(roomId);
        return Result.success(liveStatus);
    }
}

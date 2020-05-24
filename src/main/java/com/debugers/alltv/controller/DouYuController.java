package com.debugers.alltv.controller;

import com.alibaba.fastjson.JSONArray;
import com.debugers.alltv.enumType.DouYuOpenApi;
import com.debugers.alltv.exception.RoomNotFondException;
import com.debugers.alltv.model.LiveRoom;
import com.debugers.alltv.model.dto.DouYuDTO;
import com.debugers.alltv.result.CodeMsg;
import com.debugers.alltv.result.Result;
import com.debugers.alltv.service.DouYuService;
import com.debugers.alltv.util.http.HttpContentType;
import com.debugers.alltv.util.http.HttpRequest;
import com.debugers.alltv.util.http.HttpResponse;
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

@Api(tags = "斗鱼直播")
@RestController
@RequestMapping("api/douyu")
public class DouYuController {
    private final DouYuService douYuService;

    public DouYuController(DouYuService douYuService) {
        this.douYuService = douYuService;
    }

    @ApiOperation("获取房间信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roomId", value = "房间号", required = true, dataType = "String"),
    })
    @GetMapping("room/{roomId}")
    public Result<DouYuDTO> getRoomInfo(@PathVariable(value = "roomId", required = true) String roomId) {
        return Result.success(douYuService.getRoomInfo(roomId));
    }

    @ApiOperation("获取直播真实地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roomId", value = "房间号", required = true, dataType = "String"),
    })
    @GetMapping("real_url/{roomId}")
    public Result<Map<String, String>> getRealUrl(@PathVariable(value = "roomId", required = true) String roomId) {
        Map<String, String> result = new HashMap<>();
        result.put("realUrl", douYuService.getRealUrl(roomId));
        return Result.success(result);
    }

    @ApiOperation("获取房间信息附带直播源真实地址(可能有点慢)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roomId", value = "房间号", required = true, dataType = "String"),
    })
    @GetMapping("room_with_url/{roomId}")
    public Result<DouYuDTO> getRoomInfoWithUrl(@PathVariable(value = "roomId", required = true) String roomId) {
        DouYuDTO roomInfo = douYuService.getRoomInfo(roomId);
        //由于获取真实直播源网络请求多次对api对请求数独有一定影响，这里分开写
        if (roomInfo.getRoomStatus() == 1)
            //最后获取真实地址
            roomInfo.setRealUrl(douYuService.getRealUrl(roomId));
        else
            roomInfo.setRealUrl("未开播");
        return Result.success(roomInfo);
    }

    @ApiOperation(value = "获取某个分类下热门房间", notes = "比如live/0 获取全部，live/1 获取英雄联盟热门房间")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cateId", value = "分类id（房间信息有分类id）", required = true, dataType = "String"),
    })
    @GetMapping("live/{cateId}")
    public Result<JSONArray> getTopRoomsByCateId(@PathVariable(value = "cateId", required = true) String cateId) {
        HttpResponse response = HttpRequest.create(DouYuOpenApi.TOP_ROOM + cateId)
                .setContentType(HttpContentType.FORM).get();
        if (404 == response.getCode())
            throw new RoomNotFondException(CodeMsg.PAGE_ERROR);
        JSONArray data = response.getBodyJson().getJSONArray("data");
        return Result.success(response.getBodyJson().getJSONArray("data"));

    }

    @GetMapping("top_rooms")
    public Result<List<LiveRoom>> getTopRooms(Integer pageNum, Integer pageSize) {
        return Result.success(douYuService.getTopRoomsByCid("0", pageSize, pageNum));
    }

    @GetMapping("checkLiveStatus")
    public Result<Boolean> checkLiveStatus(String roomId) {
        Boolean live = douYuService.isLive(roomId);
        return Result.success(live);
    }
}

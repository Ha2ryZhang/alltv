package com.debugers.alltv.controller;

import com.debugers.alltv.model.LiveRoom;
import com.debugers.alltv.result.Result;
import com.debugers.alltv.service.BilibiliService;
import com.debugers.alltv.service.DouYuService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Api(tags = "综合各个平台")
@RestController
@RequestMapping("api/top")
public class TopController {
    private final DouYuService douYuService;
    private final BilibiliService bilibiliService;

    public TopController(DouYuService douYuService, BilibiliService bilibiliService) {
        this.douYuService = douYuService;
        this.bilibiliService = bilibiliService;
    }
    @GetMapping("live/{cid}")
    public Result<List<LiveRoom>> getTopRooms(@PathVariable("cid") String cid, @RequestParam(defaultValue = "1") Integer pageNum){
        List<LiveRoom> rooms = douYuService.getTopRoomsByCid(cid, 20, pageNum);
        //暂时不知道怎么映射 各个平台的分类关系
        if ("0".equals(cid)){
            List<LiveRoom> topRooms = bilibiliService.getTopRooms(0, 10, pageNum);
            rooms.addAll(topRooms);
        }
        Collections.sort(rooms);
        return Result.success(rooms);
    }
}

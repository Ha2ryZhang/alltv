package com.debugers.alltv.controller;

import com.debugers.alltv.model.LiveRoom;
import com.debugers.alltv.result.Result;
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

    public TopController(DouYuService douYuService) {
        this.douYuService = douYuService;
    }
    @GetMapping("live/{cid}")
    public Result<List<LiveRoom>> getTopRooms(@PathVariable("cid") String cid, @RequestParam(defaultValue = "0") Integer pageNum){
        List<LiveRoom> rooms = douYuService.getTopRoomsByCid(cid, 20, pageNum);
        Collections.sort(rooms);
        return Result.success(rooms);
    }
}

package com.debugers.alltv.controller;

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
    public Result<Map<String, String>> getRealUrl(@PathVariable(value = "roomId", required = true) String roomId){
        Map<String, String> result = new HashMap<>();
        result.put("realUrl", bilibiliService.getRealUrl(roomId));
        return Result.success(result);
    }
    @ApiOperation("github bilibili api")
    @GetMapping("getmore")
    public String getMoreApi(){
        return "更多请参见：https://github.com/lovelyyoshino/Bilibili-Live-API";
    }
}

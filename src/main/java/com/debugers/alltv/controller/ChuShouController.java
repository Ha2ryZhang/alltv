package com.debugers.alltv.controller;

import com.alibaba.fastjson.JSONObject;
import com.debugers.alltv.result.Result;
import com.debugers.alltv.service.ChuShouService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "触手直播")
@RestController
@RequestMapping("api/chushou")
public class ChuShouController {
    private final ChuShouService chuShouService;

    public ChuShouController(ChuShouService chuShouService) {
        this.chuShouService = chuShouService;
    }

    @ApiOperation("获取直播真实地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roomId", value = "房间号", required = true, dataType = "String"),
    })
    @GetMapping("real_url/{roomId}")
    public Result<JSONObject> getRealUrl(@PathVariable(value = "roomId", required = true) String roomId) {
        return Result.success(chuShouService.getRealUrl(roomId));
    }
}

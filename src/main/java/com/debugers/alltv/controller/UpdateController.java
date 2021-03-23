package com.debugers.alltv.controller;

import com.debugers.alltv.model.VersionInfo;
import com.debugers.alltv.result.Result;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "客户端更新使用")
@RestController
@RequestMapping("api/version")
public class UpdateController {
    @GetMapping("/latest")
    public Result<VersionInfo> getLatestVersion() {
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setClientVersionCode(1);
        versionInfo.setClientVersionName("1.3.3");
        versionInfo.setServerVersion("1.3.3");
        versionInfo.setLastUpdateTime("2021-03-23");
        versionInfo.setNewVersionUrl("https://alltv.lanzous.com/b01bexnha");
        return Result.success(versionInfo);
    }
}

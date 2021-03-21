package com.debugers.alltv.model;

import lombok.Data;

import java.util.Date;

@Data
public class VersionInfo {
    private Integer clientVersionCode;
    private String clientVersionName;
    private String serverVersion;
    private String lastUpdateTime;
    private String newVersionUrl;

}

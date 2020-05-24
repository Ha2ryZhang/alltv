package com.debugers.alltv.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class BilibiliServerConfig {
    @Data
    public static class HostServerInfo{
        String host;
        int port;
        @JSONField(name="ws_port")
        int wsPort;
        @JSONField(name="wss_port")
        int wssPort;
    }

    @Data
    public static class ServerInfo{
        String host;
        int port;
    }

    @JSONField(name="refresh_row_factor")
    float refreshRowFactor;

    @JSONField(name="refresh_rate")
    int refreshRate;

    @JSONField(name="max_delay")
    int maxDelay;

    @JSONField(name="port")
    int port;

    @JSONField(name="host")
    String host;

    @JSONField(name="host_server_list")
    List<HostServerInfo> hostServerList;

    @JSONField(name="server_list")
    List<ServerInfo> serverList;

    @JSONField(name="token")
    String token;
}

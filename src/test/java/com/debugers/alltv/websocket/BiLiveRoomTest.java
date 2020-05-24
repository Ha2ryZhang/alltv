package com.debugers.alltv.websocket;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.debugers.alltv.enumType.BiLiveCmd;
import com.debugers.alltv.handler.CmdHandler;
import com.debugers.alltv.handler.HandlerHolder;
import com.debugers.alltv.handler.UserCountHandler;
import com.debugers.alltv.websocket.bilibili.BiLiveRoom;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Date;

class BiLiveRoomTest {
    @SneakyThrows
    public static void simplePrint(long roomId) {
        // 需要使用一个HandlerHolder对象,在这个对象里定义响应弹幕的操作
        // HandlerHolder由两部分组成,一部分是处理观众信息的UserCountHandler,一部分是处理其他事件的CmdHandler
        HandlerHolder handlerHolder = new HandlerHolder();

        // 收到房间观众数信息时打印房间观众数
        handlerHolder.addUserCountHandler(new UserCountHandler() {
            @Override
            public void handle(int hot) {
                System.out.println("【直播人气】 "+hot);
            }
        });

        // 收到事件时打印事件(字符串)
        handlerHolder.addCmdHandler(new CmdHandler() {
            @Override
            public void handle(String json) {
                JSONObject cmd = JSONObject.parseObject(json);
                JSONObject data = cmd.getJSONObject("data");
                switch (BiLiveCmd.getByValue(cmd.getString("cmd"))){
                    case LIVE:
                        System.out.println("开播了");
                        break;
                    case DANMU_MSG:
                        JSONArray info = cmd.getJSONArray("info");
                        System.out.println(String.format("【弹幕消息】%s(%s):  %s",info.getJSONArray(2).getString(1),new Date(info.getJSONObject(9).getLong("ts")*1000),info.getString(1)));
                        break;
                    case ROOM_REAL_TIME_MESSAGE_UPDATE:
                        System.out.println(String.format("【粉丝更新】当前粉丝数 %d",cmd.getJSONObject("data").getLong("fans")));
                        break;
                    case WELCOME:
                        System.out.println(String.format("【用户进入】欢迎 %s(%s) 进入直播间",data.getString("uname"),data.getString("uid")));
                        break;
                    default:
                        break;

                }
            }
        });

        // 创建BiliLiveClient对象,使用房间号和HandlerHolder作为实例化参数
        BiLiveRoom client = new BiLiveRoom(roomId, handlerHolder);


        client.start();
    }
    @Test
    public void simplePrintTest() {
        long roomId = 5170L;
        simplePrint(roomId);

    }
}
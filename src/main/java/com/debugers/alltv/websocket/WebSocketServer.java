package com.debugers.alltv.websocket;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.debugers.alltv.enumType.BiLiveCmd;
import com.debugers.alltv.handler.CmdHandler;
import com.debugers.alltv.handler.HandlerHolder;
import com.debugers.alltv.handler.UserCountHandler;
import com.debugers.alltv.websocket.bilibili.BiLiveRoom;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author
 */
@ServerEndpoint("/bilibili/{roomId}")
@Component
@Slf4j
public class WebSocketServer {

    /**静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。*/
    private static int onlineCount = 0;
    /**concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。*/
    private static ConcurrentHashMap<String,WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;
    private String roomId="";
    BiLiveRoom client;
    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session,@PathParam("roomId") String roomId) {
        this.session = session;
        this.roomId=roomId;
        if(webSocketMap.containsKey(roomId)){
            webSocketMap.remove(roomId);
            webSocketMap.put(roomId,this);
            //加入set中
        }else{
            webSocketMap.put(roomId,this);
            //加入set中
            addOnlineCount();
            //在线数加1
        }

        log.info("当前在线人数为:" + getOnlineCount());

        try {
            sendMessage("如果觉得对你有所帮助的话，可以给个star吗?");
            sendMessage("项目地址：https://github.com/Ha2ryZhang/alltv || 个人博客：https://debugers.com");
            sendMessage("连接成功,正在获取信息...");
            // 需要使用一个HandlerHolder对象,在这个对象里定义响应弹幕的操作
            // HandlerHolder由两部分组成,一部分是处理观众信息的UserCountHandler,一部分是处理其他事件的CmdHandler
            HandlerHolder handlerHolder = new HandlerHolder();

            // 收到房间观众数信息时打印房间观众数
            handlerHolder.addUserCountHandler(new UserCountHandler() {
                @SneakyThrows
                @Override
                public void handle(int hot) {
                    sendMessage("【直播人气】"+hot);
                }
            });

            // 收到事件时打印事件(字符串)
            handlerHolder.addCmdHandler(new CmdHandler() {
                @SneakyThrows
                @Override
                public void handle(String json) {
                    JSONObject cmd = JSONObject.parseObject(json);
                    JSONObject data = cmd.getJSONObject("data");
                    switch (BiLiveCmd.getByValue(cmd.getString("cmd"))){
                        case LIVE:
                            sendMessage("开播了！");
                            break;
                        case DANMU_MSG:
                            JSONArray info = cmd.getJSONArray("info");
                            Date ts = new Date(info.getJSONObject(9).getLong("ts") * 1000);
                            sendMessage(String.format("【弹幕消息】%s(%s):  %s",info.getJSONArray(2).getString(1),new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(ts),info.getString(1)));
                            break;
                        case ROOM_REAL_TIME_MESSAGE_UPDATE:
                            sendMessage(String.format("【粉丝更新】当前粉丝数 %d",cmd.getJSONObject("data").getLong("fans")));
                            break;
                        case WELCOME:
                            sendMessage(String.format("【用户进入】欢迎 %s(%s) 进入直播间",data.getString("uname"),data.getString("uid")));
                            break;
                        default:
                            break;

                    }
                }
            });

            client = new BiLiveRoom(Long.parseLong(roomId), handlerHolder);
            client.start();

        } catch (IOException e) {
            log.error("网络异常!!!!!!");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if(webSocketMap.containsKey(roomId)){
            webSocketMap.remove(roomId);
            //从set中删除
            subOnlineCount();
        }
        client.stop();
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("用户消息:"+message);
    }

    /**
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
        client.stop();
    }
    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    /**
     * 发送自定义消息
     * */
    public static void sendInfo(String message,@PathParam("userId") String userId) throws IOException {
        log.info("发送消息到:"+userId+"，报文:"+message);
        if(StringUtils.isNotBlank(userId)&&webSocketMap.containsKey(userId)){
            webSocketMap.get(userId).sendMessage(message);
        }else{
            log.error("用户"+userId+",不在线！");
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}

package com.debugers.alltv.websocket.bilibili;

import com.alibaba.fastjson.JSONObject;
import com.debugers.alltv.exception.BiliClientException;
import com.debugers.alltv.handler.HandlerHolder;
import com.debugers.alltv.model.BilibiliServerConfig;
import com.debugers.alltv.service.BilibiliService;
import com.debugers.alltv.util.ThreadUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * bilibili 直播间
 */
@Slf4j
public class BiLiveRoom {
    private static int START_COUNT = -1;
    private long tmpRoomId, uId, roomId;

    //弹幕服务器host
    private List<BilibiliServerConfig.HostServerInfo> hostServerList;
    //token
    private String hostServerToken;

    //暂时new 不注入
    private BilibiliService bilibiliService = new BilibiliService();
    //定时
    private ScheduledExecutorService heartbeatThreadPool;
    private ScheduledFuture heartbeatTask;
    private CompletableFuture<Boolean> hdpTask;

    private static final Random RANDOM = new Random();
    private static final int RECEIVE_BUFFER_SIZE = 10 * 1024;

    // 单位: 秒
    private static final int HEARTBEAT_INTERVAL = 20;

    // 单位: 毫秒
    // 发送heartBeat 20s发一次, 50s都没发过认为发送失败掉线
    private static final int MAX_SEND_HEARTBEAT_INTERVAL = 50000;
    // 接收heartBeat 正常应该几秒收到一次, 20s都没收到认为接收掉线
    private static final int MAX_RECEIVE_HEARTBEAT_INTERVAL = 20000;
    private static final int CHECK_HEARTBEAT_INTERVAL = 10000;

    private static final int RECONNECT_IDLE = 10000;

    //处理器
    private HandlerHolder handlerHolder;

    private AtomicLong sendedHeartBeatSuccessedTime;
    private AtomicLong receivedHeartBeatSuccessedTime;

    private Socket socket;
    private BiliBiliWSClient wsClient;
    private Boolean isGoingOn = true;

    public BiLiveRoom(long roomId, long uId, HandlerHolder handlerHolder) {
        this.tmpRoomId = roomId;
        this.uId = uId;
        this.handlerHolder = handlerHolder;

        this.hostServerToken = null;

        heartbeatTask = null;
        hdpTask = null;
        heartbeatThreadPool = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setName("heartbeat thread");
            return t;
        });

        this.sendedHeartBeatSuccessedTime = new AtomicLong(System.currentTimeMillis());
        this.receivedHeartBeatSuccessedTime = new AtomicLong(System.currentTimeMillis());

    }

    public BiLiveRoom(long roomId, HandlerHolder handlerHolder) {
        this(roomId, (long) (1e14 + 2e14 * RANDOM.nextDouble()), handlerHolder);
    }

    private void initRoom() throws IOException, BiliClientException {
        //get real id
        JSONObject room = bilibiliService.getRealRoomId(String.valueOf(tmpRoomId));
        this.roomId = room.getLongValue("room_id");

        //get room config
        BilibiliServerConfig roomConfig = bilibiliService.getRoomConfig(String.valueOf(tmpRoomId));
        this.hostServerList = roomConfig.getHostServerList();
        this.hostServerToken = roomConfig.getToken();

        if (this.hostServerList == null || this.hostServerList.isEmpty()) {
            throw new BiliClientException("initRoom error, hostServerList为null");
        }
    }

    private Socket connect() {
        if (hostServerToken == null) {
            while (true) {
                try {
                    this.initRoom();
                    break;
                } catch (IOException | BiliClientException ignored) {
                }
            }
        }

        socket = null;

        // for循环用来从后往前选择hostServer
        for (int i = 0; i < this.hostServerList.size(); i++) {
            int hostServerNo = this.hostServerList.size() - 1 - i;

            BilibiliServerConfig.HostServerInfo hostServer = this.hostServerList.get(hostServerNo);

            InetSocketAddress address = new InetSocketAddress(hostServer.getHost(), hostServer.getPort());

            socket = new Socket();
            try {
                socket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
                socket.connect(address);
                wsClient = new BiliBiliWSClient(socket);
                wsClient.sendAuth(this.uId, this.roomId, hostServerToken);
                BiliBiliWSClient finalWsClient = wsClient;
                heartbeatTask = heartbeatThreadPool.scheduleAtFixedRate(() -> {
                    ThreadUtil.setThreadName("heart_beat_thread_" + roomId + "_" + START_COUNT);
                    try {
                        finalWsClient.sendHeartBeat();
                        sendedHeartBeatSuccessedTime.set(System.currentTimeMillis());
                    } catch (IOException e) {
                        log.error(e.toString());
                        cleanHeartBeatTask();
                    }
                }, 2, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
                return socket;
            } catch (IOException e) {
                log.error("error about heartbeatTask " + e.toString());
                cleanHeartBeatTask();
                try {
                    socket.close();
                    socket = null;
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    /**
     * async
     */
    public void start() {
        START_COUNT += 1;
        String threadName = String.format("room_thread_%d_%d", roomId, START_COUNT);
        ThreadUtil.setThreadName(threadName);

        Socket socket = connect();

        if (socket == null || socket.isClosed()) {
            throw new BiliClientException("连接socket失败");
        }
        HandleDataLoop hdp = new HandleDataLoop(socket, roomId, handlerHolder, receivedHeartBeatSuccessedTime);

        hdpTask = CompletableFuture.supplyAsync(() -> {
            String hdpTaskThreadName = "hdp_thread_in_" + threadName;
            ThreadUtil.setThreadName(hdpTaskThreadName);
            try {
                hdp.start();
            } catch (IOException e) {
                log.error(e.toString());
                cleanHeartBeatTask();
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return false;
            }
            return true;
        }, Executors.newSingleThreadExecutor());

    }

    @SneakyThrows
    public void stop() {
        //关闭 先停止心跳 关闭handleDataLoop 最后关闭socket
        heartbeatThreadPool.shutdown();
        heartbeatTask.cancel(false);
        hdpTask.cancel(false);
        socket.close();
    }

    void cleanHeartBeatTask() {
        boolean cancelSendHeartBeatRes = true, cancelReceiveHeartBeatRes = true;
        if (heartbeatTask != null && !heartbeatTask.isCancelled()) {
            cancelSendHeartBeatRes = heartbeatTask.cancel(true);
            heartbeatTask = null;
        }
        if (hdpTask != null && !hdpTask.isCancelled()) {
            cancelReceiveHeartBeatRes = hdpTask.cancel(true);
            hdpTask = null;
        }
    }
}

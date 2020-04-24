package com.debugers.alltv.websocket.bilibili;

import com.debugers.alltv.handler.HandlerHolder;
import com.debugers.alltv.util.ReadStreamUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Slf4j
public class HandleDataLoop {
    private Socket socket;
    private long roomId;
    private AtomicLong receivedHeartBeatTime;

    private HandlerHolder handlerHolder;

    public HandleDataLoop(Socket socket, long roomId, HandlerHolder handlerHolder, AtomicLong receivedHeartBeatTime) {
        this.socket = socket;
        this.roomId = roomId;
        this.handlerHolder = handlerHolder;
        this.receivedHeartBeatTime = receivedHeartBeatTime;
    }

    public void start() throws IOException {
        if (socket != null) {
//            int bufferSize = socket.getReceiveBufferSize();
            int bufferSize = 10 * 1024;

            log.info("connect successed. real roomId：" + roomId);
            InputStream socketInputStream;
            while ((socketInputStream = socket.getInputStream()) != null) {
                DataInputStream input = new DataInputStream(socketInputStream);
                byte[] ret = ReadStreamUtil.readStream(input, bufferSize);
                    CompletableFuture.supplyAsync(()->{
                        try {
                            analyzeData(ret);
                        } catch (IOException | DataFormatException e) {
                            log.error("error in analyzeData: " + e.toString());
                        }

                        return null;
                    });
            }
        }
    }

    private void analyzeData(byte[] data) throws IOException, DataFormatException {
        int dataLength = data.length;
        if (dataLength < 16) {
            log.info("wrong data");
        } else {
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data));
            int msgLength = inputStream.readInt();
            if (msgLength < 16) {
                log.info("maybe need expand size of cache");
            } else if (msgLength > 16 && msgLength == dataLength) {

                short headerLength = inputStream.readShort();
                short version = inputStream.readShort();

                int action = inputStream.readInt() - 1;
                // 直播间在线用户数目
                if (action == 2) {
                    this.receivedHeartBeatTime.set(System.currentTimeMillis());
                    inputStream.readInt();
                    int userCount = inputStream.readInt();
                    handlerHolder.handleUserCount(userCount);
                } else if (action == 4) {
                    int param = inputStream.readInt();
                    int msgBodyLength = dataLength - 16;
                    byte[] msgBody = new byte[msgBodyLength];
                    inputStream.read(msgBody, 0, msgBodyLength);
                    if (version != 2) {
                        String jsonStr = new String(msgBody, StandardCharsets.UTF_8);
                        handlerHolder.handleCmd(jsonStr);
                    } else {
                        Inflater inflater = new Inflater();
                        inflater.setInput(msgBody);
                        while (!inflater.finished()) {
                            byte[] header = new byte[16];
                                inflater.inflate(header, 0, 16);
                                DataInputStream headerStream  = new DataInputStream(new ByteArrayInputStream(header));
                                int innerMsgLen = headerStream.readInt();
                                short innerHeaderLength = headerStream.readShort();
                                short innerVersion = headerStream.readShort();
                                int innerAction = headerStream.readInt() - 1;
                                int innerParam = headerStream.readInt();
                                byte[] innerData = new byte[innerMsgLen - 16];
                                inflater.inflate(innerData, 0, innerData.length);
                                if (innerAction == 4) {
                                    String jsonStr = new String(innerData, StandardCharsets.UTF_8);
                                    handlerHolder.handleCmd(jsonStr);
                                } else if (innerAction == 2) {
                                    // pass
                                }
                        }
                    }
                }
            } else if (msgLength > 16 && msgLength < dataLength) {
                byte[] singleData = new byte[msgLength];
                System.arraycopy(data, 0, singleData, 0, msgLength);
                analyzeData(singleData);
                int remainLen = dataLength - msgLength;
                byte[] remainDate = new byte[remainLen];
                System.arraycopy(data, msgLength, remainDate, 0, remainLen);
                analyzeData(remainDate);
            }
        }
    }
}

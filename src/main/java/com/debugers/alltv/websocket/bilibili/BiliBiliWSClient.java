package com.debugers.alltv.websocket.bilibili;

import com.alibaba.fastjson.JSON;
import com.debugers.alltv.enumType.BiliBiliOperationEnum;
import com.debugers.alltv.model.BiliBiliAuth;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class BiliBiliWSClient {
    private static final short PROTOCOL_VERSION = 1, HEAD_LEN = 16;
    private static final int PARAM = 1;

    private final Socket socket;

    public BiliBiliWSClient(Socket socket) {
        this.socket = socket;
    }

    public void sendSocketData(int totalLen, int headLen, int protocolVersion, BiliBiliOperationEnum operation, int param, byte[] data) throws IOException {
        if (socket.isClosed()) {
            throw new IOException("socket closed");
        }
        synchronized (socket) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeInt(totalLen);
            out.writeShort(headLen);
            out.writeShort(protocolVersion);
            out.writeInt(operation.getValue());
            out.writeInt(param);
            if (data != null && data.length > 0) {
                out.write(data);
            }
            out.flush();
        }
    }

    public void sendSocketData(BiliBiliOperationEnum operation, int totalLen, byte[] data) throws IOException {
        sendSocketData(totalLen, HEAD_LEN, PROTOCOL_VERSION, operation, PARAM, data);
    }

    public void sendSocketData(BiliBiliOperationEnum operation, String data) throws IOException {
        int totalLen = data.length() + 16;
        sendSocketData(operation, totalLen, data.getBytes(StandardCharsets.UTF_8));
    }

    public void sendSocketData(BiliBiliOperationEnum operation, Object obj) throws IOException {
        sendSocketData(operation, JSON.toJSONString(obj));
    }

    public void sendHeartBeat() throws IOException {
        sendSocketData(BiliBiliOperationEnum.HEARTBEAT, 16, null);
    }

    public void sendAuth(long uId, long roomId, String token) throws IOException {
        BiliBiliAuth authData = new BiliBiliAuth(uId, roomId, token);
        sendSocketData(BiliBiliOperationEnum.AUTH, authData);
    }
}
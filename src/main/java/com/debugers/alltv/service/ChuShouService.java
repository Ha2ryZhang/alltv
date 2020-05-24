package com.debugers.alltv.service;

import com.alibaba.fastjson.JSONObject;
import com.debugers.alltv.exception.RoomNotFondException;
import com.debugers.alltv.result.CodeMsg;
import com.debugers.alltv.util.http.HttpRequest;
import org.springframework.stereotype.Service;

@Service
public class ChuShouService {
    public JSONObject getRealUrl(String roomId){
        JSONObject bodyJson = HttpRequest.create("https://chushou.tv/h5player/video/get-play-url.htm?roomId=" + roomId + "&protocols=2&callback=")
                .get().getBodyJson();
        if (bodyJson.getIntValue("code")==404)
            throw new RoomNotFondException(CodeMsg.ROOM_ERROR);
        return bodyJson.getJSONArray("data").getJSONObject(0);
    }

    public static void main(String[] args) {
        ChuShouService service=new ChuShouService();
        JSONObject realUrl = service.getRealUrl("3901753");
        System.out.println(realUrl);
    }
}

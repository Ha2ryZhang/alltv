package com.debugers.alltv.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.debugers.alltv.enumType.BilibiliOpenApi;
import com.debugers.alltv.model.BilibiliServerConfig;
import com.debugers.alltv.model.LiveRoom;
import com.debugers.alltv.model.dto.BilibiliDTO;
import com.debugers.alltv.util.http.HttpRequest;
import com.debugers.alltv.util.http.HttpResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BilibiliService {
    public JSONObject getRealRoomId(String rid) {
        String room_url = BilibiliOpenApi.ROOM_INIT + rid;
        JSONObject response = HttpRequest.create(room_url).get().getBodyJson();
        JSONObject data = response.getJSONObject("data");
        if (data == null) {
            return null;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("live_status", data.getBoolean("live_status"));
        jsonObject.put("room_id", data.getLongValue("room_id"));
        return jsonObject;

    }

    public BilibiliServerConfig getRoomConfig(String rid) {
        String configUrl = BilibiliOpenApi.SERVER_CONFIG + rid;
        JSONObject response = HttpRequest.create(configUrl).get().getBodyJson();
        return response.getObject("data",BilibiliServerConfig.class);
    }
    public List<LiveRoom> getTopRooms(Integer areaId,Integer pageSize,Integer page){
        //?areaId=0&sort=online&pageSize=10&page=1
        JSONObject bodyJson = HttpRequest.create(BilibiliOpenApi.RANK.getValue())
                .appendParameter("sort","online")
                .appendParameter("areaId",areaId)
                .appendParameter("pageSize",pageSize)
                .appendParameter("page",page)
                .get().getBodyJson();
        List<BilibiliDTO> data = bodyJson.getJSONArray("data").toJavaList(BilibiliDTO.class);
       return data.stream().map(this::convertToLiveRoom).collect(Collectors.toList());
    }
    private LiveRoom convertToLiveRoom(BilibiliDTO dto){
        LiveRoom liveRoom = new LiveRoom();
        BeanUtils.copyProperties(dto, liveRoom);
        liveRoom.setCom("bilibili");
        liveRoom.setRoomStatus(1);
        liveRoom.setCateId(dto.getArea()+"-"+dto.getArea_v2_id());
        return liveRoom;

    }
    public Boolean getLiveStatus(String roomId){
        HttpResponse response = HttpRequest.create(BilibiliOpenApi.ROOM_INIT.getValue()+roomId).get();
        int room_status = response.getBodyJson().getJSONObject("data").getIntValue("live_status");
        //默认斗鱼1为开播，2为没开播状态
        return room_status==1;
    }
    public String getRealUrl(String rid) {
        JSONObject roomInfo = getRealRoomId(rid);

        if (roomInfo == null) {
            return "直播间不存在";
        }
        if (!roomInfo.getBoolean("live_status")) {
            return "未开播";
        }
        String room_url = BilibiliOpenApi.PLAY_URL.toString() + roomInfo.getLongValue("room_id") + "&platform=h5&otype=json&quality=4";
        JSONObject response = HttpRequest.create(room_url).get().getBodyJson();
        JSONArray durl = response.getJSONObject("data").getJSONArray("durl");
        if (durl != null) {
            String result = durl.getJSONObject(0).getString("url");
            String pattern_result = result.substring(result.indexOf("/live_"), result.indexOf(".m3u8") + ".m3u8".length());
            return "https://cn-hbxy-cmcc-live-01.live-play.acgvideo.com/live-bvc" + pattern_result;
        } else {
            return "疑似部分国外IP无法GET到正确数据，待验证";
        }
    }
}

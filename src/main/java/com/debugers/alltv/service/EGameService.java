package com.debugers.alltv.service;

import com.alibaba.fastjson.JSONObject;
import com.debugers.alltv.model.LiveRoom;
import com.debugers.alltv.util.http.HttpRequest;
import com.debugers.alltv.util.http.HttpResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 企鹅电竞
 */
@Service
public class EGameService {
    private final StringRedisTemplate redisTemplate;

    public EGameService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取真实直播链接
     * @param roomId roomId
     * @return url
     */
    public String getRealUrl(String roomId){
        String room_url = "https://share.egame.qq.com/cgi-bin/pgg_async_fcgi";
        HttpResponse response = HttpRequest.create(room_url)
                .appendParameter("param", "{\"0\":{\"module\":\"pgg_live_read_svr\",\"method\":\"get_live_and_profile_info\",\"param\":{\"anchor_id\":"+roomId+",\"layout_id\":\"hot\",\"index\":1,\"other_uid\":0}}}")
                .get();
        JSONObject data = response.getBodyJson().getJSONObject("data");
        if (data==null){
           return "直播间不存在";
        }
        JSONObject video_info = data.getJSONObject("0").getJSONObject("retBody").getJSONObject("data").getJSONObject("video_info");
//        JSONObject pid = video_info.getJSONObject("pid");
//        if (pid==null){
//            return "直播间未启用";
//        }
        int isLive = data.getJSONObject("0").getJSONObject("retBody").getJSONObject("data").getJSONObject("profile_info").getIntValue("is_live");
        if (isLive!=1){
            return "直播间未开播";
        }
        return video_info.getJSONArray("stream_infos").getJSONObject(1).getString("play_url");
    }

    public List<LiveRoom> getTopRooms(Integer pageNum, Integer pageSize) {
        List<String> list = redisTemplate.opsForList().range("Qie", (pageNum - 1) * pageNum, pageSize);
        List<LiveRoom> liveRooms = new ArrayList<>();
        if (list != null) {
            liveRooms = list.stream().map(s -> {
                LiveRoom liveRoom = JSONObject.parseObject(s, LiveRoom.class);
                String trim = liveRoom.getCateName().replace("\n", "").trim();
                liveRoom.setCateName(trim);
                return liveRoom;
            }).collect(Collectors.toList());
        }
        return liveRooms;
    }

}

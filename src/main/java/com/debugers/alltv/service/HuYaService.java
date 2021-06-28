package com.debugers.alltv.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.debugers.alltv.model.LiveRoom;
import com.debugers.alltv.util.http.HttpContentType;
import com.debugers.alltv.util.http.HttpRequest;
import lombok.Data;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class HuYaService {
    private final RedisTemplate<String, LiveRoom> redisTemplate;
    private static final Pattern PATTERN = Pattern.compile("\"liveLineUrl\":\"([\\s\\S]*?)\"");

    public HuYaService(RedisTemplate<String, LiveRoom> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public String getRealUrl(String roomId) {
        String room_url = "https://m.huya.com/" + roomId;
        String response = HttpRequest.create(room_url)
                .setContentType(HttpContentType.FORM)
                .putHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .get().getBody();
        Matcher matcher = PATTERN.matcher(response);
        if (!matcher.find()) {
            return "未开播或直播间不存在";
        }
        String result = matcher.group(1);
        result=new String(Base64.decodeBase64(result),StandardCharsets.UTF_8);
        if (result.contains("replay")|| !result.contains("//")){
            return "";
        }

        result = result.substring(result.indexOf("//"));
        return "http:" + result; //默认清晰度
    }

    public List<LiveRoom> getTopRooms(Integer pageNum, Integer pageSize) {
        return redisTemplate.opsForList().range("huya", (long) (pageNum - 1) * pageSize, ((long) pageNum * pageSize) - 1);
    }

    public List<LiveRoom> search(String keyword) {
        JSONObject bodyJson = HttpRequest.create("https://search.cdn.huya.com")
                .appendParameter("m", "Search")
                .appendParameter("do", "getSearchContent")
                .appendParameter("plt", "m")
                .appendParameter("q", keyword)
                .appendParameter("uid", 0)
                .appendParameter("app", 11)
                .appendParameter("v", 1)
                .appendParameter("typ", -5)
                .appendParameter("start", 0)
                .appendParameter("rows", 8)
                .get().getBodyJson();
        //主播
        JSONArray data = bodyJson.getJSONObject("response").getJSONObject("1").getJSONArray("docs");
        List<SearchResult> results = data.toJavaList(SearchResult.class);
        //房间
        JSONArray roomData = bodyJson.getJSONObject("response").getJSONObject("3").getJSONArray("docs");
        List<SearchResultRoom> searchResultRooms = roomData.toJavaList(SearchResultRoom.class);
        List<LiveRoom> person = results.stream().map(result -> {
            LiveRoom room = new LiveRoom();
            room.setRoomId(result.getRoom_id());
            room.setRoomStatus(result.getGameLiveOn() ? 1 : 2);
            room.setCom("huya");
            room.setOwnerName(result.getGame_nick());
            room.setAvatar(result.getGame_avatarUrl180());
            room.setRoomThumb("");
            room.setCateName(result.getGame_name());
            room.setOnline(0L);
            room.setRoomName(result.getLive_intro());
            return room;
        }).collect(Collectors.toList()).subList(0, 2);//截取前两个
        List<LiveRoom> rooms = searchResultRooms.stream().map(result -> {
            LiveRoom room = new LiveRoom();
            room.setRoomId(result.getRoom_id());
            room.setRoomStatus(1);
            room.setCom("huya");
            room.setOwnerName(result.getGame_nick());
            room.setAvatar(result.getGame_imgUrl());
            room.setRoomThumb(result.getGame_screenshot());
            room.setCateName(result.getGameName());
            room.setOnline(result.getGame_total_count());
            room.setRoomName(result.getGame_roomName());
            return room;
        }).collect(Collectors.toList());
        List<LiveRoom> finalRooms = new ArrayList<>(rooms);
        //去重复 只要2条主播数据
        for (LiveRoom liveRoom : rooms) {
            for (LiveRoom room : person) {
                if (!finalRooms.contains(room)) {
                    if (!liveRoom.getRoomId().equals(room.getRoomId())) {
                        finalRooms.add(room);
                    }
                }
            }
            break;
        }
        if (rooms.size() == 0) {
            finalRooms.addAll(person);
        }
        return finalRooms;
    }

    /**
     * 虎牙搜索返回结果
     */
    @Data
    static class SearchResult {
        private String room_id;
        private Boolean gameLiveOn;
        private Long game_longChannel;
        private String game_profileLink;
        private String rec_game_name;
        private Integer screen_type;
        private Long yyid;
        private String game_avatarUrl52;
        private Long game_activityCount;
        private String recommended_text;
        private String game_liveLink;
        private String game_name;
        private Long uid;
        private Long rec_live_time;
        private String game_avatarUrl180;
        private Long game_channel;
        private Integer game_level;
        private String game_nick;
        private Integer game_subChannel;
        private String sTagName;
        private String live_intro;
        private Integer aid;
        private Integer game_recommendStatus;
        private Integer game_id;
    }

    @Data
    static class SearchResultRoom {
        private Long gameId;
        private String room_id;
        private String game_privateHost;
        private Integer screen_type;
        private Long yyid;
        private String tag_name;
        private String game_imgUrl;
        private Integer game_shortChannel;
        private String game_introduction;
        private String game_screenshot;
        private Long uid;
        private String gameName;
        private Long game_total_count;
        private Integer game_channel;
        private String game_nick;
        private Integer game_subChannel;
        private Integer aid;
        private String game_roomName;
        private String liveSourceType;
    }
}

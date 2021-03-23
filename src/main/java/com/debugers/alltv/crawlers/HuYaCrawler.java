package com.debugers.alltv.crawlers;

import com.alibaba.fastjson.JSONObject;
import com.debugers.alltv.model.LiveRoom;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HuYaCrawler {
    private final RedisTemplate<String, LiveRoom> redisTemplate;

    public HuYaCrawler(RedisTemplate<String, LiveRoom> serializableRedisTemplate) {
        this.redisTemplate = serializableRedisTemplate;
    }


    /**
     * 获取首页热门的120个房间
     * @throws IOException 网络异常
     */
    @Scheduled(fixedRate = 60000) //固定速率
    public void getTopRooms() throws IOException {
        Document doc = Jsoup.connect("https://www.huya.com/l").get();
        Elements elements = doc.select("#js-live-list>.game-live-item");
        List<LiveRoom> liveRooms = elements.stream().map(element -> {
            String href = element.getElementsByClass("title").first().attr("href");
            String gameTypeUrl = element.select(".txt>.game-type>a").first().attr("href");
            LiveRoom room = new LiveRoom();
            room.setCom("huya");
            room.setRoomName(element.getElementsByClass("title").first().attr("title"));
            room.setRoomId(href.substring(href.indexOf("com/") + 4));
            room.setRoomStatus(1);
            room.setAvatar(element.select(".txt>.avatar>img").attr("data-original"));
            room.setOwnerName(element.select(".txt>.avatar>.nick").attr("title"));
            room.setOnline((long) (Double.parseDouble(element.select(".txt>.num>.js-num").html().replaceAll("万", "").replaceAll(",", "")) * 10000));
            room.setRoomThumb(element.select(".video-info >.pic").attr("data-original"));
            if(room.getRoomThumb().startsWith("//")){
                room.setRoomThumb("http:"+room.getRoomThumb());
            }
            room.setCateName(element.select(".txt>.game-type>a").attr("title"));
            room.setCateId(gameTypeUrl.substring(gameTypeUrl.indexOf(("g/")) + 2));
            return room;
        }).sorted().collect(Collectors.toList());
        Collections.reverse(liveRooms);
        redisTemplate.delete("huya");
        redisTemplate.opsForList().leftPushAll("huya",liveRooms);
    }
}

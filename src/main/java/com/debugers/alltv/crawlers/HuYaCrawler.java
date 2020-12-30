package com.debugers.alltv.crawlers;

import com.debugers.alltv.model.LiveRoom;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HuYaCrawler {
    /**
     * 获取首页热门的120个房间
     * @return roomList
     * @throws IOException 网络异常
     */
    public static List<LiveRoom> getTopRooms() throws IOException {
        Document doc = Jsoup.connect("https://www.huya.com/l").get();
        Elements elements = doc.select("#js-live-list>.game-live-item");
        return elements.stream().map(element -> {
            String href = element.getElementsByClass("title").first().attr("href");
            String gameTypeUrl = element.select(".txt>.game-type>a").first().attr("href");
            LiveRoom room = new LiveRoom();
            room.setCom("huya");
            room.setRoomName(element.getElementsByClass("title").first().attr("title"));
            room.setRoomId(href.substring(href.indexOf("com/") + 4));
            room.setRoomStatus(1);
            room.setAvatar(element.select(".txt>.avatar>img").attr("data-original"));
            room.setOwnerName(element.select(".txt>.avatar>.nick").attr("title"));
            room.setOnline((long) (Double.parseDouble(element.select(".txt>.num>.js-num").html().replaceAll("万", "")) * 10000));
            room.setRoomThumb(element.select(".video-info >.pic").attr("data-original"));
            room.setCateName(element.select(".txt>.game-type>a").attr("title"));
            room.setCateId(gameTypeUrl.substring(gameTypeUrl.indexOf(("g/")) + 2));
            return room;
        }).collect(Collectors.toList());
    }
}

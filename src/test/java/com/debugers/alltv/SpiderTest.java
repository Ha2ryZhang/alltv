package com.debugers.alltv;

import com.debugers.alltv.model.LiveRoom;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpiderTest {
    @SneakyThrows
    @Test
    public void simplePrintTest() {
        Document doc = Jsoup.connect("https://www.huya.com/l").get();
        Elements newsHeadlines = doc.select("#js-live-list>.game-live-item");
        List<LiveRoom> roomList = new ArrayList<>();
        for (Element element : newsHeadlines) {
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
            roomList.add(room);
        }
        System.out.println(roomList.size());
    }
}

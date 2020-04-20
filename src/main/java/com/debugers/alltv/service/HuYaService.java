package com.debugers.alltv.service;

import com.debugers.alltv.util.http.HttpContentType;
import com.debugers.alltv.util.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HuYaService {
    private static final Pattern PATTERN = Pattern.compile("(?<=hasvedio: ')(.*\\.m3u8)");
    public String getRealUrl(String roomId) {
        String room_url = "https://m.huya.com/" + roomId;
        String response = HttpRequest.create(room_url)
                .setContentType(HttpContentType.FORM)
                .putHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .get().getBody();
        Matcher matcher = PATTERN.matcher(response);
        if (!matcher.find()) {
            return null;
        }
        String result = matcher.group();

        if (StringUtils.isBlank(result)) {
            return "未开播或直播间不存在";
        }
        if (result.startsWith("//"))
            result="https:"+result;
//        return result.replaceAll("_\\d{3,4}\\.m3u8", ".flv");
        return result; //默认清晰度
    }
}

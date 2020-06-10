package com.debugers.alltv.service;

import com.alibaba.fastjson.JSONObject;
import com.debugers.alltv.enumType.DouYuOpenApi;
import com.debugers.alltv.exception.RoomNotFondException;
import com.debugers.alltv.model.LiveRoom;
import com.debugers.alltv.model.dto.DouYuDTO;
import com.debugers.alltv.result.CodeMsg;
import com.debugers.alltv.util.MD5Util;
import com.debugers.alltv.util.http.HttpContentType;
import com.debugers.alltv.util.http.HttpRequest;
import com.debugers.alltv.util.http.HttpResponse;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 斗鱼直播
 *
 * @author harryzhang
 */
@Service
public class DouYuService {
    private static final Pattern PATTERN = Pattern.compile("(function ub9.*)[\\s\\S](var.*)");
    private static final Pattern PATTERN2 = Pattern.compile("(?<=/live/).*(?=/playlist)");
    private static final Pattern PATTERN3 = Pattern.compile("^[0-9a-zA-Z]*");

    public String getTimeStr(long time, String format) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    private JSONObject getTT() {
        long nowTime = System.currentTimeMillis();
        String tt1 = String.valueOf(nowTime / 1000);
        String tt2 = String.valueOf(nowTime);
        String today = getTimeStr(nowTime, "yyyyMMdd");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tt1", tt1);
        jsonObject.put("tt2", tt2);
        jsonObject.put("today", today);
        return jsonObject;
    }

    private JSONObject getHomeJs(String rid) {
        String roomUrl = "https://m.douyu.com/" + rid;
        String response = HttpRequest.create(roomUrl).get().getBody();
        String realRid = response.substring(response.indexOf("{\"rid\":") + "{\"rid\":".length());
        realRid = realRid.substring(0, realRid.indexOf(","));

        if (!rid.equals(realRid)) {
            roomUrl = "https://m.douyu.com/" + realRid;
            response = HttpRequest.create(roomUrl).get().getBody();
        }

        Matcher matcher = PATTERN.matcher(response);
        if (!matcher.find()) {
            return null;
        }

        String result[] = matcher.group().split("\n");
        String str1 = result[0].replaceAll("eval.*;", "strc;");
        String homejs = str1 + result[1];

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("homejs", homejs);
        jsonObject.put("real_rid", realRid);
        return jsonObject;
    }

    private String getSign(String rid, String post_v, String tt, String ub9) throws ScriptException, NoSuchAlgorithmException {
        ub9 += "ub98484234();";
        ScriptEngine docjs = new ScriptEngineManager().getEngineByName("javascript");
        String res2 = docjs.eval(ub9).toString();
        String str3 = res2.replaceAll("\\(function[\\s\\S]*toString\\(\\)", "\'");
        String md5rb = MD5Util.md5String(rid + "10000000000000000000000000001501" + tt + "2501" + post_v);
        String str4 = "function get_sign(){var rb=\'" + md5rb + str3;
        String str5 = str4.replaceAll("return rt;}[\\s\\S]*", "return re;};");
        String str6 = str5.replaceAll("\"v=.*&sign=\"\\+", "");
        str6 += "get_sign(" + rid + ",\"10000000000000000000000000001501\",\"" + tt + "\")";
        String sign = docjs.eval(str6).toString();

        return sign;
    }

    private String mixRoom(String rid) {
        return "PKing";
    }

    private String getPreUrl(String rid, String tt) throws NoSuchAlgorithmException {
        String requestUrl = "https://playweb.douyucdn.cn/lapi/live/hlsH5Preview/" + rid;

        String auth = MD5Util.md5String(rid + tt);

        JSONObject response = HttpRequest.create(requestUrl)
                .setContentType(HttpContentType.FORM)
                .putHeader("rid", rid)
                .putHeader("time", tt)
                .putHeader("auth", auth)
                .appendParameter("rid", rid)
                .appendParameter("did", "10000000000000000000000000001501")
                .post()
                .getBodyJson();

        String preUrl = "";
        if (response.getIntValue("error") == 0) {
            String real_url = (response.getJSONObject("data")).getString("rtmp_live");
            if (real_url.contains("mix=1")) {
                preUrl = mixRoom(rid);
            } else {
                Matcher matcher = PATTERN3.matcher(real_url);
                if (!matcher.find()) {
                    return null;
                }
                preUrl = matcher.group();
            }
        }

        return preUrl;
    }

    private String getSignUrl(String post_v, String rid, String tt, String ub9) throws ScriptException, NoSuchAlgorithmException {
        String sign = getSign(rid, post_v, tt, ub9);
        String requestUrl = "https://m.douyu.com/api/room/ratestream";

        JSONObject response = HttpRequest.create(requestUrl)
                .setContentType(HttpContentType.FORM)
                .putHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .appendParameter("v", "2501" + post_v)
                .appendParameter("did", "10000000000000000000000000001501")
                .appendParameter("tt", tt)
                .appendParameter("sign", sign)
                .appendParameter("ver", "219032101")
                .appendParameter("rid", rid)
                .appendParameter("rate", "-1")
                .post()
                .getBodyJson();

        if (response.getIntValue("code") != 0) {
            return null;
        }
        String realUrl = (response.getJSONObject("data")).getString("url");
        if (realUrl.contains("mix=1")) {
            return mixRoom(rid);
        } else {
            Matcher matcher = PATTERN2.matcher(realUrl);
            if (!matcher.find()) {
                return null;
            }
            return matcher.group();
        }
    }

    public String getRealUrl(String rid) {
        JSONObject tt = getTT();
        String today = tt.getString("today");
        String tt1 = tt.getString("tt1");
        String tt2 = tt.getString("tt2");
        String realUrl = null;
        try {
            String url = getPreUrl(rid, tt2);
            if (StringUtils.isNotBlank(url)) {
                realUrl = "http://tx2play1.douyucdn.cn/live/" + url + ".flv";
            } else {
                JSONObject result = getHomeJs(rid);
                String real_rid = result.getString("real_rid");
                String homejs = result.getString("homejs");
                String real_url = getSignUrl(today, real_rid, tt1, homejs);
                if (StringUtils.isBlank(real_url)) {
                    realUrl = "未开播";
                }
                realUrl = "http://tx2play1.douyucdn.cn/live/" + real_url + ".flv";
            }
        } catch (NoSuchAlgorithmException | ScriptException e) {
            e.printStackTrace();
        } finally {
            if ("http://tx2play1.douyucdn.cn/live/null.flv".equals(realUrl))
                return "未开播或房间不存在";
            else
                return realUrl;
        }
    }

    public DouYuDTO getRoomInfo(String roomId) {
        String url = DouYuOpenApi.ROOM_INFO + roomId;
        HttpResponse response = HttpRequest.create(url)
                .setContentType(HttpContentType.FORM).get();
        if (404 == response.getCode())
            throw new RoomNotFondException(CodeMsg.ROOM_ERROR);

        return getDTO(response.getBodyJson());
    }

    /**
     * 获取斗鱼推荐直播间
     *
     * @param cid      分类id
     * @param pageSize 页数大小
     * @param pageNum  页数
     * @return liveRoom
     */
    public List<LiveRoom> getTopRoomsByCid(String cid, Integer pageSize, Integer pageNum) {
        HttpResponse response = HttpRequest.create(DouYuOpenApi.TOP_ROOM + cid)
                .appendParameter("offset", (pageNum - 1) * pageSize)
                .appendParameter("limit", pageSize)
                .setContentType(HttpContentType.FORM).get();
        if (404 == response.getCode())
            throw new RoomNotFondException(CodeMsg.PAGE_ERROR);
        List<DouYuDTO> data = response.getBodyJson().getJSONArray("data").toJavaList(DouYuDTO.class);
        return data.stream().map(this::convertToLiveRoom).collect(Collectors.toList());
    }

    public Boolean isLive(String roomId) {
        HttpResponse response = HttpRequest.create(DouYuOpenApi.ROOM_INFO.getValue() + roomId).get();
        int room_status = response.getBodyJson().getJSONObject("data").getIntValue("room_status");
        //默认斗鱼1为开播，2为没开播状态
        return room_status == 1;
    }

    public List<LiveRoom> search(String keyWords) {
        HttpResponse response = HttpRequest.create(DouYuOpenApi.SEARCH.getValue())
                .appendParameter("sk", keyWords).post();
        List<SearchResult> results = response.getBodyJson().getJSONObject("data").getJSONArray("room").toJavaList(SearchResult.class);

        return results.stream().map(result -> {
            LiveRoom room = new LiveRoom();
            BeanUtils.copyProperties(result, room);
            //1 开播 2 未开播
            room.setRoomStatus(result.getIsLive() == 0 ? 2 : 1);
            room.setRoomThumb(result.getRoomSrc());
            room.setOwnerName(result.getNickname());
            room.setRoomId(result.getRoomId() + "");
            room.setCom("douyu");
            room.setCateId(result.getCid2() + "");
            if (result.getHn().contains("万")) {
                Double online = Double.parseDouble(result.getHn().replaceAll("万", ""))*10000;
                room.setOnline(online.longValue());
            } else {
                room.setOnline(Long.parseLong(result.getHn()));
            }
            return room;
        }).collect(Collectors.toList());
    }

    private LiveRoom convertToLiveRoom(DouYuDTO douYuDTO) {
        LiveRoom liveRoom = new LiveRoom();
        BeanUtils.copyProperties(douYuDTO, liveRoom);
        liveRoom.setCom("douyu");
        return liveRoom;
    }

    private DouYuDTO getDTO(JSONObject jsonObject) {
        JSONObject data = jsonObject.getJSONObject("data");
        DouYuDTO douYuDTO = new DouYuDTO();
        douYuDTO.setAvatar(data.getString("avatar"));
        douYuDTO.setCateName(data.getString("cate_name"));
        douYuDTO.setOnline(data.getLong("online"));
        douYuDTO.setOwnerName(data.getString("owner_name"));
        douYuDTO.setRoomId(data.getString("room_id"));
        douYuDTO.setRoomName(data.getString("room_name"));
        douYuDTO.setRoomThumb(data.getString("room_thumb"));
        douYuDTO.setStartTime(data.getDate("start_time"));
        douYuDTO.setRoomStatus(data.getInteger("room_status"));
        douYuDTO.setCateId(data.getString("cate_id"));
        //由于获取真实直播源网络请求多次对api对请求数独有一定影响，这里分开写
//        if (douYuDTO.getRoomStatus()==1)
//            //最后获取真实地址
//            douYuDTO.setRealUrl(getRealUrl(douYuDTO.getRoomId()));
//        else
//            douYuDTO.setRealUrl("未开播");
        return douYuDTO;
    }

    /**
     * 斗鱼搜索结果
     */
    @Data
    static class SearchResult {
        private String cateName;
        private String roomName;
        private int isVertical;
        private String verticalSrc;
        private String hn;
        private int isLive;
        private String nickname;
        private int ownerUID;
        private String roomSrc;
        private int roomId;
        private int vipId;
        private int cid1;
        private int cid2;
        private String avatar;
        private String official;
    }
}

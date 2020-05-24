package com.debugers.alltv.enumType;

public enum BiLiveCmd {
    /**
     * 开播
     */
    LIVE("LIVE"),
    /**
     * 结束
     */
    END("END"),
    /**
     * 系统消息
     */
    SYS_MSG("SYS_MSG"),
    /**
     * 房间信息变更
     */
    ROOM_CHANGE("ROOM_CHANGE"),
    /**
     * 欢迎用户
     */
    WELCOME("WELCOME"),
    /**
     * 欢迎舰长
     */
    WELCOME_GUARD("WELCOME_GUARD"),
    /**
     * 弹幕消息
     */
    DANMU_MSG("DANMU_MSG"),
    /**
     * 送出礼物
     */
    SEND_GIFT("SEND_GIFT"),
    /**
     * 粉丝数更新
     */
    ROOM_REAL_TIME_MESSAGE_UPDATE("ROOM_REAL_TIME_MESSAGE_UPDATE"),
    /**
     * 小时榜
     */
    ROOM_RANK("ROOM_RANK"),
    /**
     * OTHER 并不是官方的
     */
    OTHER("OTHER");

    public String getCmd() {
        return cmd;
    }

    String cmd;

    BiLiveCmd(String cmd) {
        this.cmd = cmd;
    }

    public static BiLiveCmd getByValue(String val) {
        // 根据value返回枚举类型,主要在switch中使用
        for (BiLiveCmd cmd : values()) {
            if (val.equals(cmd.getCmd())) return cmd;
        }
        return OTHER;
    }
}

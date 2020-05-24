package com.debugers.alltv.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class HandlerHolder {
    private Set<UserCountHandler> userCountHandlerSet;
    private Set<CmdHandler> cmdHandlerSet;

    public HandlerHolder() {
        userCountHandlerSet = new HashSet<>();
        cmdHandlerSet = new HashSet<>();
    }

    public HandlerHolder(CmdHandler cmdHandler){
        this();
        this.addCmdHandler(cmdHandler);
    }

    public HandlerHolder(UserCountHandler userCountHandler, CmdHandler cmdHandler){
        this(cmdHandler);
        this.addUserCountHandler(userCountHandler);
    };

    public void addUserCountHandler(UserCountHandler o) {
        userCountHandlerSet.add(o);
    }

    public boolean removeUserCountHandler(UserCountHandler handler){
        return userCountHandlerSet.remove(handler);
    }

    public boolean addCmdHandler(CmdHandler handler){
        return cmdHandlerSet.add(handler);
    }

    public boolean removeCmdHandler(CmdHandler handler){
        return cmdHandlerSet.remove(handler);
    }

    public void handleUserCount(int userCount){
        for (UserCountHandler userCountHandler : userCountHandlerSet) {
            try {
                userCountHandler.handle(userCount);
            }catch (Exception e){
                log.error("error while handling userCount: " + userCount + ", handler: " + userCountHandler.getClass().getName() +"\n" + e);
            }
        }
    }
    public void handleCmd(String cmdJson){
        for (CmdHandler cmdHandler : cmdHandlerSet) {
            try {
                cmdHandler.handle(cmdJson);
            }catch (Exception e){
                log.error("error while handling cmd: " + cmdJson + ", handler: " + cmdHandler.getClass().getName() +"\n" + e);
            }
        }
    }
}

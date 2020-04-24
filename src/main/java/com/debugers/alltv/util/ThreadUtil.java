package com.debugers.alltv.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadUtil {
    public static void setThreadName(String threadName){
        Thread.currentThread().setName(threadName);
        log.info("[set thread name: " + threadName + "]");
    }
}

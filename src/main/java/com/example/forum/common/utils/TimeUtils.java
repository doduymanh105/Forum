package com.example.forum.common.utils;

import java.util.concurrent.ThreadLocalRandom;

public class TimeUtils {
    public static long generateJitterTtl(int baseMinutes, int maxJitterMinutes){
        long jitter = ThreadLocalRandom.current().nextInt(maxJitterMinutes+1);
        return baseMinutes + jitter;
    }
}

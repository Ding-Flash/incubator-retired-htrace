package org.apache.htrace.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class SpeedSampler extends Sampler{
    public static ConcurrentHashMap<String, AtomicLong> records = new ConcurrentHashMap<String, AtomicLong>();
    public static ConcurrentHashMap<String, Item> precords = new ConcurrentHashMap<String, Item>();
    public final Long interval;
    public final static String SAMPLER_INTERVAL_CONF_KEY = "sampler.interval";

    public SpeedSampler(HTraceConfiguration conf) {
        this.interval = Long.parseLong(conf.get(SAMPLER_INTERVAL_CONF_KEY));
    }

    @Override
    public boolean next(String description) {
        Long curTime = System.currentTimeMillis();
        AtomicLong curNum = records.get(description);
        if (curNum == null) {
            AtomicLong newNum = new AtomicLong(0);
            curNum = records.putIfAbsent(description, newNum);
            if (curNum == null) {
                curNum = newNum;
            }
        }
        Long num = curNum.incrementAndGet();
        Item func = precords.get(description);
        if (func == null){
            Item newFunc = new Item(curTime);
            func = precords.putIfAbsent(description, new Item(curTime));
            if (func == null){
                func = newFunc;
            }
        }
        Double probability;
        if (num % interval == 0){
            probability = func.updateSpeed(curTime);
        }
        else {
            probability = func.getProbability();
        }
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    @Override
    public boolean next() {
        return false;
    }
}

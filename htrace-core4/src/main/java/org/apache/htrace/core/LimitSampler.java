package org.apache.htrace.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class LimitSampler extends Sampler {

    public static ConcurrentHashMap<String, AtomicLong> records = new ConcurrentHashMap<String, AtomicLong>();
    public final static String SAMPLER_Limit_CONF_KEY = "sampler.limit";
    public final static String SAMPLER_FRACTION_CONF_KEY = "sampler.fraction";
    public static long limit;
    public static double threshold;

    public static Timer t;

    class Task extends TimerTask{
        @Override
        public void run() {
            for(String key: records.keySet()){
                AtomicLong newNum = new AtomicLong(0);
                records.put(key, newNum);
            }
        }
    }

    public LimitSampler(HTraceConfiguration conf){
        this.limit = Long.parseLong(conf.get(SAMPLER_Limit_CONF_KEY));
        this.threshold = Double.parseDouble(conf.get(SAMPLER_FRACTION_CONF_KEY));
        t = new Timer(true);
        t.schedule(new Task(),1000,1000);
    }

    public Double updata(String description){
        AtomicLong curNum = records.get(description);
        if (curNum == null) {
            AtomicLong newNum = new AtomicLong(0);
            curNum = records.putIfAbsent(description, newNum);
            if (curNum == null) {
                curNum = newNum;
            }
        }
        Long num = curNum.incrementAndGet();
        if(num > limit){
            return threshold;
        }
        return 1.0;
    }

    @Override
    public boolean next(String description) {
        return ThreadLocalRandom.current().nextDouble() < updata(description);
    }

    @Override
    public boolean next() {
        return false;
    }
}

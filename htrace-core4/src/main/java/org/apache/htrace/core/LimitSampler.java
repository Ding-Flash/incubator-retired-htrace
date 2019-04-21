package org.apache.htrace.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class LimitSampler extends Sampler {

    private static ConcurrentHashMap<String, AtomicLong> records = new ConcurrentHashMap<String, AtomicLong>();
    private final static String SAMPLER_LIMIT_CONF_KEY = "sampler.limit";
    private final static String SAMPLER_FRACTION_CONF_KEY = "sampler.fraction";
    private final long limit;
    private final double threshold;

    public static Timer t;

    class Task extends TimerTask {
        @Override
        public void run() {
            for (String key : records.keySet()) {
                AtomicLong newNum = new AtomicLong(0);
                records.put(key, newNum);
            }
        }
    }

    public LimitSampler(HTraceConfiguration conf) {
        this.limit = Long.parseLong(conf.get(SAMPLER_LIMIT_CONF_KEY));
        this.threshold = Double.parseDouble(conf.get(SAMPLER_FRACTION_CONF_KEY));
        t = new Timer(true);
        t.schedule(new Task(), 1000, 1000);
    }

    private double updata(String description) {
        AtomicLong curNum = records.get(description);
        if (curNum == null) {
            AtomicLong newNum = new AtomicLong(0);
            curNum = records.putIfAbsent(description, newNum);
            if (curNum == null) {
                curNum = newNum;
            }
        }
        long num = curNum.incrementAndGet();
        if (num > limit) {
            return this.threshold;
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

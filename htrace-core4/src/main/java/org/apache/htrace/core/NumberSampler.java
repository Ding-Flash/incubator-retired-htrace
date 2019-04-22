package org.apache.htrace.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class NumberSampler extends Sampler {
    private static ConcurrentHashMap<String, AtomicLong> records = new ConcurrentHashMap<String, AtomicLong>();
    public final double number;
    private final double threshold;
    private final static String SAMPLER_FRACTION_CONF_KEY = "sampler.fraction";
    private final static String SAMPLER_NUMBER_CONF_KEY = "sampler.number";

    public NumberSampler(HTraceConfiguration conf) {
        this.number = Math.pow(Double.parseDouble(conf.get(SAMPLER_NUMBER_CONF_KEY)), 2);
        this.threshold = Double.parseDouble(conf.get(SAMPLER_FRACTION_CONF_KEY));
    }

    public boolean update(String description) {
        AtomicLong curNum = records.get(description);
        if (curNum == null) {
            AtomicLong newNum = new AtomicLong(0);
            curNum = records.putIfAbsent(description, newNum);
            if (curNum == null) {
                curNum = newNum;
            }
        }
        Long num = curNum.incrementAndGet();
        double probability = Math.exp(-(number / (num * num)));
        if (probability < threshold) {
            probability = threshold;
        }
        boolean res = ThreadLocalRandom.current().nextDouble() > probability;
        return res;
    }

    @Override
    public boolean next(String description) {
        return update(description);

    }

    public boolean next() {
        return true;
    }
}

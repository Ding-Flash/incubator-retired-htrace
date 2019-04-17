package org.apache.htrace.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class NumberSampler extends Sampler {
    public static ConcurrentHashMap<String, AtomicLong> records = new ConcurrentHashMap<String, AtomicLong>();
    public final double number;
    public static double threshold;
    public final static String SAMPLER_FRACTION_CONF_KEY = "sampler.fraction";
    public final static String SAMPLER_NUMBER_CONF_KEY = "sampler.number";
    public NumberSampler(HTraceConfiguration conf) {
        this.number = Double.parseDouble(conf.get(SAMPLER_NUMBER_CONF_KEY));
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
        Double probability = Math.exp(-(number/(num*num)));
        if(probability < threshold){
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

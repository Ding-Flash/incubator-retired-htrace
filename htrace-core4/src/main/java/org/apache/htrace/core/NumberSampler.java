package org.apache.htrace.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class NumberSampler extends Sampler {
    public static ConcurrentHashMap<String, AtomicLong> records = new ConcurrentHashMap<String, AtomicLong>();

    public NumberSampler(HTraceConfiguration conf) {
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
        Long num = curNum.get();
        Double probability = Math.log(Math.log(num) + 1) / Math.log(100);
        boolean res = ThreadLocalRandom.current().nextDouble() > probability;
        if(res){
            curNum.incrementAndGet();
        }
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

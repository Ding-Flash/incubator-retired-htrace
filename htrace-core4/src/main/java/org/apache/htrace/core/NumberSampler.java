package org.apache.htrace.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class NumberSampler extends Sampler {
    public static ConcurrentHashMap<String, AtomicLong> records = new ConcurrentHashMap<String, AtomicLong>();
    public static Long MAX = 100L;

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
        Long num = curNum.incrementAndGet();
        MAX = Math.max(num, MAX);
        boolean res = ThreadLocalRandom.current().nextLong() % MAX > num;
        LOG.info(description+": "+res);
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

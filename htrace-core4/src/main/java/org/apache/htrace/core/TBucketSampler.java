package org.apache.htrace.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;

public class TBucketSampler extends Sampler {
    public static ConcurrentHashMap<String, List<Long>> records = new ConcurrentHashMap<>();
    public static final String BUCKET_SIZE = "sampler.bucketSize";
    public static final String INCREASE_STEP = "sampler.increaseStep";
    public static Long bktSize;
    public static Long incStep;
//    public static final Log LOG = LogFactory.getLog("MyLog");


    public TBucketSampler(HTraceConfiguration conf) {
        bktSize = Long.parseLong(conf.get(BUCKET_SIZE));
        incStep = Long.parseLong(conf.get(INCREASE_STEP));
    }

    private static Long newtok(String description) {
        Long newTime = System.currentTimeMillis();
        List Func = records.get(description);
        Long lastTime = (Long) Func.get(2);
        Long tok = (newTime - lastTime) / incStep;
        if (tok > 0) {
            Func.set(2, newTime);
            records.put(description, Func);
        }

        return tok;
    }

    private static synchronized boolean update(String description) {
        if (records.get(description) == null) {
            List<Long> TandN = new ArrayList<>();
//          Data Format:token,number,last timestamp
            TandN.add(bktSize - 1);
            TandN.add(1L);
            TandN.add(System.currentTimeMillis());
            records.put(description, TandN);
//            LOG.info("new trace \""+description+"\" added"+"with tokens left "+TandN.get(0)+" and counting "+TandN.get(1));
            return true;

        }

        List<Long> f = records.get(description);
        if (f.get(0) > 0) {
            Long tok = newtok(description);
            f.set(0, f.get(0) + tok > bktSize ? bktSize - 1 : f.get(0) + tok - 1);
            f.set(1, f.get(1) + 1);
            records.put(description, f);
//            if(f.get(1)%100 == 0){
//                bktSize = f.get(1)/10;
//            }
//            LOG.info("trace \""+description+"\" sampled "+"with token left "+f.get(0)+" and counting "+f.get(1));
            return true;
        }
//        LOG.info("trace \""+description+"\" not sampled "+"with token left "+f.get(0)+" and counting "+f.get(1));
        if (f.get(0) == 0) {
            Long tok = newtok(description);
            f.set(0, tok);
            records.put(description, f);
//            if(f.get(1)%100 == 0){
//                bktSize = f.get(1)/10;
//            }
//            LOG.info("trace \""+description+"\" sampled "+"with token left "+f.get(0)+" and counting "+f.get(1));
            return false;
        }
        return false;
    }

    @Override
    public boolean next(String description) {
        return update(description);

    }

    @Override
    public boolean next() {
        return false;
    }
}

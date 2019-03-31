package org.apache.htrace.core;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;

public class TBucketSampler extends Sampler {
    public static ConcurrentHashMap<String, List<Long>> records = new ConcurrentHashMap<>();
    Long curTime = System.currentTimeMillis();
    public final static String BUCKET_SIZE = "sampler.bucketSize";
    public final static String INCREASE_STEP = "sampler.increaseStep";
    private Long bktSize;
    private Long incStep;

    public TBucketSampler(HTraceConfiguration conf) {
        this.bktSize = Long.parseLong(conf.get(BUCKET_SIZE));
        this.incStep = Long.parseLong(conf.get(INCREASE_STEP));
    }

    private Long newtok(){
        Long newTime = System.currentTimeMillis();
        Long tok = (newTime - curTime)/incStep;
        if (tok>0){
            curTime = newTime;
        }

        return tok;
    }
    private boolean update(String description){
        if(records.get(description)==null) {
            List<Long> TandN = new ArrayList<>();
            TandN.add(49L);
            TandN.add(1L);
            records.put(description, TandN);
            return true;
        }

        List<Long> f = records.get(description);
        if(f.get(0) > 0){
            Long tok = newtok();
            f.set(0,f.get(0)+tok>bktSize?bktSize-1:f.get(0)+tok-1);
            f.set(1,f.get(1)+1);
            if(f.get(1)%100 == 0){
                bktSize = f.get(1)/10;
            }
            return true;
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

package org.apache.htrace.core;

public class SpeedSampler extends Sampler{

    public SpeedSampler(HTraceConfiguration conf) {
    }



    @Override
    public boolean next(String description) {
        return false;
    }

    @Override
    public boolean next() {
        return false;
    }
}

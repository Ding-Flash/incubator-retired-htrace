package org.apache.htrace.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Item {

    private Long preTime;
    private Double probability;
    private Long preInterval;
    public static final Log LOG = LogFactory.getLog("MyLog");

    Item(Long time){
        preTime = time;
        probability = 1.0;
        preInterval = 1L;
    }

    public Double getProbability(){
        return probability;
    }

    public synchronized Double updateSpeed(Long curTime){
        Long interval = curTime - preTime;
        if (interval > 50 * preInterval){
            probability = 1.0;
            LOG.info("SpeedSampler +++++p timestamp: " + curTime + " interval: "+ interval + " p: "+ probability);
        }
        else{
            if(probability > 0.4) {
                probability -= 0.05;
                LOG.info("SpeedSampler -p "+ curTime + " interval: "+ interval + " p: "+ probability);
            }
        }
        preInterval = interval;
        preTime = curTime;
        return probability;
    }
}

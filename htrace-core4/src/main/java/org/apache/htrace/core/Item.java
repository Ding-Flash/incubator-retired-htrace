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
        }
        else{
            if(probability > 0.01) {
                probability -= 0.2;
            }
        }
        preInterval = interval;
        preTime = curTime;
        return probability;
    }
}

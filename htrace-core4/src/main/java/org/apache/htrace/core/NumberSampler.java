package org.apache.htrace.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NumberSampler extends Sampler{
	static Map<String, Long> numOfEach = new HashMap<String, Long>(); 
	static long max = 1;
	Random random = new Random();
	
	public NumberSampler(HTraceConfiguration conf) {
	  }
	
	public synchronized boolean update(String description) {
		long num = 1;
		if (numOfEach.get(description) != null) {
			num = numOfEach.get(description) + 1;
			numOfEach.put(description,num);
			if (num > max) {
				max = num;
			}
		} else {
			numOfEach.put(description, num);
		}
		return (random.nextLong() % max > num);
	}

	@Override
	public boolean next(String description) {
		return update(description);
		
	}
	public boolean next() {
		return true;

	}
}

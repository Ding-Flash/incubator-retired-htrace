package org.apache.htrace.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Iterator;
import java.lang.Long;

public class NumberSampler_multithread extends Sampler{
	Map<String, Long> numOfEach = new HashMap<String, Long>();
	static long max = 1;
	static boolean unlocked = true;
	static Map<String, Long> numOfTotal = new HashMap<String, Long>();
	Random random = new Random();

	public NumberSampler_multithread(HTraceConfiguration conf) {
	  }
	static private synchronized boolean getlock() {
		if (unlocked){
			unlocked = false;
			return true;
		}else {
			return false;
		}

	}
	private void releaselock(){
		unlocked = true;
	}
	private void merge(){
		if(random.nextInt(10) < 5)
			return;
		else if (getlock()){
			Iterator<Map.Entry<String, Long>> iterator = numOfEach.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Long> entry = iterator.next();
				String key = entry.getKey();
				Long value = entry.getValue();
				numOfTotal.put(key,value+(numOfTotal.containsKey(key)?numOfTotal.get(key):0));
			}
			numOfEach.clear();
			releaselock();
		}

	}
	private boolean update(String description) {

		long num_tol = numOfTotal.containsKey(description)?numOfTotal.get(description):1;

			numOfEach.put(description,numOfEach.containsKey(description)?numOfEach.get(description) + 1:1);

			if (num_tol > max) {
				max = num_tol;
			}

		merge();
		return (random.nextLong() % max > num_tol);
	}

	@Override
	public boolean next(String description) {
		return update(description);
		
	}
	public boolean next() {
		return true;

	}
}

package org.apache.htrace.core;

import java.util.Random;

import java.lang.Thread;


public class TestDemoThread extends Thread{
	Tracer tracer = new MyTracerConfigration().configTracer("LimitSampler");
	Random random = new Random();
//	public void Func_0() throws InterruptedException {
//		try(TraceScope scope = tracer.newScope("TestDemo#Func_0")) {
//			
//			int t = random.nextInt(100);
//			Thread.sleep(t>0?t:-t);	
//			if (random.nextDouble() > 0.2) {
//				Func_1();
//			} else {
//				Func_2();
//			}
//		}
//		
//	}
	public void Func_1() throws InterruptedException {
		try(TraceScope scope = tracer.newScope("TestDemo#Func_1")) {
//			Thread.sleep(100);
			int t = random.nextInt(100);
			Thread.sleep(t);	
			if (random.nextDouble() > 0.2) {
				Func_1_1();
			} else {
				Func_1_2();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
	public void Func_2() throws InterruptedException {
		try(TraceScope scope = tracer.newScope("TestDemo#Func_2")) {
//			Thread.sleep(10);
			int t = random.nextInt(100);
			Thread.sleep(t);	
			if (random.nextDouble() > 0.2) {
				Func_2_1();
			} else {
				Func_2_2();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	public void Func_1_1() throws InterruptedException {
		try(TraceScope scope = tracer.newScope("TestDemo#Func_1_1")) {
	
			int t = random.nextInt(10);
			Thread.sleep(t);
		}
	}
	public void Func_1_2() throws InterruptedException {
		try(TraceScope scope = tracer.newScope("TestDemo#Func_1_2")) {

			int t = random.nextInt(100);
			Thread.sleep(t);
		}
	}
	public void Func_2_1() throws InterruptedException {
		try(TraceScope scope = tracer.newScope("TestDemo#Func_2_1")) {

			int t = random.nextInt(100);
			Thread.sleep(t);
		}
	}
	public void Func_2_2() throws InterruptedException {
		try(TraceScope scope = tracer.newScope("TestDemo#Func_2_2")) {

			int t = random.nextInt(100);
			Thread.sleep(t);
		}
	}
	public void run() {
		try {
			if (random.nextDouble()>0.2) {
				Func_1();
			} else {
				Func_2();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		for(int i = 0;i<100;i++) {
			new TestDemoThread().start();
		}

	}
}

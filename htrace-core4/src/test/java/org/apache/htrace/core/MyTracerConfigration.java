package org.apache.htrace.core;


public class MyTracerConfigration {
	
	
	
	public Tracer configTracer(String sampler) {
		String traceFileName = "F:\\JavaProject\\htrace-4.3.0-incubating\\result\\result_"+sampler+".json";
		Tracer tracer = new Tracer.Builder().
	        name("testWriteToLocalFileTracer").
	        tracerPool(new TracerPool("testWriteToLocalFile")).
	        conf(HTraceConfiguration.fromKeyValuePairs(
	            "sampler.classes", sampler,
	            "sampler.fraction","0.2",
	            "span.receiver.classes", LocalFileSpanReceiver.class.getName(),
	            "local.file.span.receiver.path", traceFileName,
	            "tracer.id", "%{tname}")).
	        build();
		return tracer;
	}
	
	public Tracer configTracer() {
		return configTracer("AlwaysSampler");
	}
	
}
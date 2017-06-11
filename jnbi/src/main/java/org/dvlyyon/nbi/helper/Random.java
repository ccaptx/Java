package org.dvlyyon.nbi.helper;

public class Random extends HelperObject{
	Double start = 0d;
	Double end = new Double(Long.MAX_VALUE);
	Integer step = 1;

	public Random() {
	}
	
	public HString nextLong() {
		long startL = start.longValue();
		long endL = end.longValue();
		return new HString(new Long(startL + step * (long)((end-start)/step*Math.random())).toString());
	}
	
	public HString nextDouble() {
		double startD = start;
		double endD = end;
		return new HString(new Double(start + step * Math.floor((endD-startD)/step*Math.random())).toString());
	}

	public Double getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = Double.parseDouble(start);
	}

	public Double getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = Double.parseDouble(end);
	}

	public Integer getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = Integer.parseInt(step);
	}
	
	public static void main(String argv[]) {
		Random  r = new Random();
		r.setStart("191350000");
		r.setEnd("196100000");
		r.setStep("100");
		for (int i=0;i <100; i++) {
			System.out.println(i+": "+r.nextLong());
		}
		for (int i=0;i <100; i++) {
			System.out.println(i+": "+r.nextDouble());
		}
		r = new Random();
		r.setStart("-123");
		r.setEnd("128");
		for (int i=0; i<100; i++) System.out.println(r.nextLong());
		r = new Random();
		for (int i=0; i<100; i++) System.out.println(r.nextLong());
		for (int i=0; i<100; i++) System.out.println(r.nextDouble());
	}
}

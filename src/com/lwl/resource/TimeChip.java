package com.lwl.resource;

import java.io.Serializable;

import com.lwl.task.Step;

/**
 * 时间片 时间片的长短代表一个Step的执行时间
 * 
 * @author LWL
 * 
 */
public class TimeChip implements Cloneable {
	// 开始时间
	private double start;
	// 结束时间
	private double end;
	// 该时间片用于完成哪个Step
	private Step step;

	public TimeChip(double start, double end) {
		this(start, end, null);
	}

	public TimeChip(double start, double end, Step step) {
		setEnd(end);
		setStart(start);
		setStep(step);
	}

	public void setEnd(double end) {
		this.end = end;
	}

	public void setStart(double start) {
		this.start = start;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public Step getStep() {
		return step;
	}

	public double getEnd() {
		return end;
	}

	public double getStart() {
		return start;
	}

	public double getPeriod() {
		return end - start;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String s = step == null ? "null" : step.toString();

		return "[ " + getStart() + ", " + getEnd() + " ]-->" + s;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

}

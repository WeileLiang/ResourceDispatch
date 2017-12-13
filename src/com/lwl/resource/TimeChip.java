package com.lwl.resource;

import java.io.Serializable;

import com.lwl.task.Step;

/**
 * ʱ��Ƭ ʱ��Ƭ�ĳ��̴���һ��Step��ִ��ʱ��
 * 
 * @author LWL
 * 
 */
public class TimeChip implements Cloneable {
	// ��ʼʱ��
	private double start;
	// ����ʱ��
	private double end;
	// ��ʱ��Ƭ��������ĸ�Step
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

package com.lwl.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 作业类
 * 
 * @author LWL
 * 
 */
public class Job implements Cloneable{

	// 包含所有Step的集合
	private List<Step> allSteps;

	//Job的Id，例如参与调度的JOb有Job0和Job5，那么两者的id分别是0和1
	private int id;

	//Job的名字，例如参与调度的JOb有Job0和Job5，那么两者的nameId分别是0和5
	private int nameId;
	//Job的开始时间
	private double beginTime=0;
	
	public Job(int id,int nameId) {
		setId(id);
		setNameId(nameId);
		allSteps = new ArrayList<Step>();
	}

	public void addNewStep(Step step) {
		allSteps.add(step);
		
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void setBeginTime(double beginTime) {
		this.beginTime = beginTime;
	}

	public int getId() {
		return id;
	}

	/**
	 * 返回Job的入口点
	 * 
	 * @return
	 */
	public Step getStart() {
		return allSteps.get(0);
	}
	
	public List<Step> getAllSteps() {
		return allSteps;
	}
	
	//获取该Job的Step数目
	public int getJobStepsCoubt() {
		return allSteps.size();
	}
	
	public double getBeginTime() {
		return beginTime;
	}
	
	public int getNameId() {
		return nameId;
	}
	
	public void setNameId(int nameId) {
		this.nameId = nameId;
	}
	
}

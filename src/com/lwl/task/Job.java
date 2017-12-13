package com.lwl.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * ��ҵ��
 * 
 * @author LWL
 * 
 */
public class Job implements Cloneable{

	// ��������Step�ļ���
	private List<Step> allSteps;

	//Job��Id�����������ȵ�JOb��Job0��Job5����ô���ߵ�id�ֱ���0��1
	private int id;

	//Job�����֣����������ȵ�JOb��Job0��Job5����ô���ߵ�nameId�ֱ���0��5
	private int nameId;
	//Job�Ŀ�ʼʱ��
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
	 * ����Job����ڵ�
	 * 
	 * @return
	 */
	public Step getStart() {
		return allSteps.get(0);
	}
	
	public List<Step> getAllSteps() {
		return allSteps;
	}
	
	//��ȡ��Job��Step��Ŀ
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

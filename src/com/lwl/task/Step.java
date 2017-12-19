package com.lwl.task;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.lwl.resource.Machine;

/**
 * ������ ��ΪJob�µĸ����ڵ�
 * 
 * @author LWL
 * 
 */
public class Step implements Cloneable {
	// �ý����������Job
	private Job job;
	// ����Job��id
	private int id;
	// ��������Graph��id
	private int graphId;

	public static final int AND = 0; // ����
	public static final int OR = 1; // ����
	public static final int JOIN = 2; // ��۽�㣬���Step�Ļ㼯���
	public static final int GENERAL = 3;
//	public static final int END = 4; //������㣬������������Ѿ��ӹ����

	// ���������
	private int type = GENERAL;
	// �ھӽ�����������ýڵ������Ľ��ļ��ϣ���������Ϊ�ýڵ�
	private List<Step> children;
	// ���������OR����ʱ���������ֵܽ��
	private List<Step> orBrothers;
	// ���ý����JOIN���ʱ����Ҫ�������Step���ܵ��ȸý��
	private List<Step> finishedBeforeDone;
	// ����Step��General����ʱ�򣬼�¼����ɸ�Step���豸�Լ���Ӧ��ʱ��
	private List<Integer> suitableMachines;
	private List<Integer> finishedTimes;

	//�Ƿ��OR����Ӧ��JOIN���
	public boolean isJoinOrStep=false;
	
	public Step(Job job, int id, int type, int graphId) {
		setId(id);
		setJob(job);
		setType(type);
		setGraphId(graphId);

	}
	
	/**
	 * ���ظ�Step���ض�machineId��Machine�ϵĲ���ʱ��
	 * @param machineID
	 * @return
	 */
	public int getFinishedTimeBaseMachineId(int machineID) {
		if (suitableMachines != null && suitableMachines.size() > 0)
			for (int i = 0; i < suitableMachines.size(); i++)
				if (suitableMachines.get(i) == machineID)
					return finishedTimes.get(i);

		return Integer.MAX_VALUE;
	}

	public void setSuitableMachines(List<Integer> suitableMachines) {
		this.suitableMachines = suitableMachines;
	}

	public void setFinishedTimes(List<Integer> finishedTimes) {
		this.finishedTimes = finishedTimes;
	}

	public void setGraphId(int graphId) {
		this.graphId = graphId;
	}

	public void setOrBrothers(List<Step> orBrothers) {
		this.orBrothers = orBrothers;
	}

	public void setChildren(List<Step> children) {
		this.children = children;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public void setFinishedBeforeDone(List<Step> finishedBeforeDone) {
		this.finishedBeforeDone = finishedBeforeDone;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getGraphId() {
		return graphId;
	}

	public int getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public Job getJob() {
		return job;
	}

	public List<Step> getChildren() {
		return children;
	}

	public List<Step> getOrBrothers() {
		return orBrothers;
	}

	public List<Step> getFinishedBeforeDone() {
		return finishedBeforeDone;
	}

	public List<Integer> getSuitableMachines() {
		return suitableMachines;
	}

	public List<Integer> getFinishedTimes() {
		return finishedTimes;
	}

	// ���ظ�Step������λ�ã�������Job��id, �����id��
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "( " + job.getNameId() + ", " + getId() + " )";
	}

}

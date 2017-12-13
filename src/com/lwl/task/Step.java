package com.lwl.task;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.lwl.resource.Machine;

/**
 * 步骤类 作为Job下的各个节点
 * 
 * @author LWL
 * 
 */
public class Step implements Cloneable {
	// 该结点所隶属的Job
	private Job job;
	// 隶属Job的id
	private int id;
	// 隶属整个Graph的id
	private int graphId;

	public static final int AND = 0; // 与结点
	public static final int OR = 1; // 或结点
	public static final int JOIN = 2; // 汇聚结点，多个Step的汇集结点
	public static final int GENERAL = 3;

	// 步骤的类型
	private int type = GENERAL;
	// 邻居结点的链表，即与该节点相连的结点的集合，且起点必须为该节点
	private List<Step> children;
	// 当父结点是OR类型时，保存其兄弟结点
	private List<Step> orBrothers;
	// 当该结点是JOIN结点时，需要完成以下Step才能调度该结点
	private List<Step> finishedBeforeDone;
	// 当该Step是General类型时候，记录能完成该Step的设备以及对应的时间
	private List<Integer> suitableMachines;
	private List<Integer> finishedTimes;

	public Step(Job job, int id, int type, int graphId) {
		setId(id);
		setJob(job);
		setType(type);
		setGraphId(graphId);

	}
	
	/**
	 * 返回该Step在特定machineId的Machine上的操作时间
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

	// 返回该Step的坐标位置（所属的Job的id, 自身的id）
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "( " + job.getNameId() + ", " + getId() + " )";
	}

}

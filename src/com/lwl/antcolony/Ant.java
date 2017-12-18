package com.lwl.antcolony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lwl.aco.StepMapMachine;
import com.lwl.resource.Resource;
import com.lwl.resource.TimeChip;
import com.lwl.task.Step;
import com.lwl.task.TaskGraph;

public class Ant {

	private int id;

	// 蚂蚁的禁忌表，表示已经走过且不可再走的Step
	private Set<Step> taboo;

	// 蚂蚁的可选池
	private Set<Step> accessiblePool;

	// 蚂蚁当前所处的Step
	private Step currStep;

	// 蚂蚁当前经历的时间
	private double time;

	private Resource resource;

	private Set<Step> path;

	// 记录可选池中每个Step可开始的最早时间点
	private Map<Step, Double> startStepAfterTime;

	private double[] lastFinishedTimeForJobs;

	// 保存各个Job已经调度了的Step的时间片
	private List<List<TimeChip>> timeLinesForEachJob;

	// 记录各个Job里各个Step的预计完成时间以及应该被插入到timeLine的哪个位置
	private List<Map<StepMapMachine, double[]>> arrangedFinishedTimeForStepsOfEachJob;

	public Ant(int id, Resource resource) {
		setId(id);
		setResource(resource);

		// 已经走过了start,即初始点
		currStep = TaskGraph.start;

		taboo = new HashSet<Step>();
		taboo.add(currStep);
		// 接下来可达的Step就是TaskGraph入口点的各个孩子
		List<Step> children = TaskGraph.start.getChildren();
		accessiblePool = new HashSet<Step>(children);
		// 记录每个Step的可开始执行时间，最初每个Step均为0
		startStepAfterTime = new HashMap<Step, Double>();
		for (Step child : children)
			startStepAfterTime.put(child, child.getJob().getBeginTime());

		path = new LinkedHashSet<Step>();
		path.add(TaskGraph.start);

		lastFinishedTimeForJobs = new double[TaskGraph.JOBS_COUNT];

		// 为每个Job创建存放已经调度好了的Step的时间片
		timeLinesForEachJob = new ArrayList<List<TimeChip>>();
		for (int i = 0; i < TaskGraph.JOBS_COUNT; i++)
			timeLinesForEachJob.add(new ArrayList<TimeChip>());

		arrangedFinishedTimeForStepsOfEachJob= new ArrayList<Map<StepMapMachine, double[]>>();
		for (int i = 0; i < TaskGraph.JOBS_COUNT; i++)
			arrangedFinishedTimeForStepsOfEachJob.add(new HashMap<StepMapMachine, double[]>());

	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean inTaboo(Step step) {
		return taboo.contains(step);
	}

	public void setCurrStep(Step currStep) {
		this.currStep = currStep;
	}

	public void setTime(double time) {
		if (time > this.time)
			this.time = time;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}

	public Step getCurrStep() {
		return currStep;
	}

	public int getId() {
		return id;
	}

	public double getTime() {
		return time;
	}

	public void addToAccessiblePool(Step step) {
		accessiblePool.add(step);
	}

	public void addToTaboo(Step step) {
		taboo.add(step);
	}

	public Set<Step> getAccessiblePool() {
		return accessiblePool;
	}

	public boolean isAccessiblePoolEmpty() {
		return accessiblePool.size() == 0;
	}

	public Step[] getPahtArray() {
		Step[] way = new Step[path.size()];
		path.toArray(way);
		return way;
	}

	public void addToPath(Step step) {
		path.add(step);
	}

	public Way getWay() {
		return new Way(getPahtArray(), getTime(), getResource());
	}

	/**
	 * 把可选池中的所有非General类型的Step转换为General
	 */
	public void changeAllToGeneralType() {
		Step[] steps = new Step[accessiblePool.size()];
		accessiblePool.toArray(steps);
		for (int i = 0; i < steps.length; i++)
			changeStepToGeneralType(steps[i]);

//		Step[] temps = new Step[accessiblePool.size()];
//		accessiblePool.toArray(temps);
		// for (int i = 0; i < temps.length; i++) {
		// System.out.print(temps[i].getId()+" ");
		// }
		// System.out.println();
	}

	private void changeStepToGeneralType(Step step) {
		if (step.getType() == Step.GENERAL)
			return;

		List<Step> mChildren = step.getChildren();
		if (mChildren != null && mChildren.size() > 0) {
			for (Step child : mChildren) {
				// 把父结点的最早完成时间赋给儿子结点
				Double beginTimeOfChild = startStepAfterTime.get(child);
				Double beginTimeOfParent = startStepAfterTime.get(step);
				if (beginTimeOfChild == null
						|| beginTimeOfChild < beginTimeOfParent)
					startStepAfterTime.put(child, beginTimeOfParent);
			}
		}

		if (step.getType() == Step.OR || step.getType() == Step.AND) {
			taboo.add(step);
			accessiblePool.remove(step);
			List<Step> children = step.getChildren();
			if (children != null && children.size() > 0) {
				accessiblePool.addAll(children);
				for (Step child : children)
					changeStepToGeneralType(child);

			}
		} else if (step.getType() == Step.JOIN) {
			// 如果child的类型是JOIN，那么在把该child添加到备选池之前必须所有前提Step都已经完成，即添加到taboo中
			List<Step> finishedBeforeDone = step.getFinishedBeforeDone();
			boolean allFinished = true;
			for (Step s : finishedBeforeDone)
				if (!inTaboo(s)) {
					allFinished = false;
					break;
				}

			if (allFinished) {
				taboo.add(step);
				List<Step> children = step.getChildren();
				if (children != null && children.size() > 0) {
					accessiblePool.addAll(children);
					for (Step child : children)
						changeStepToGeneralType(child);

				}
			}

			accessiblePool.remove(step);

		}

	}

	public Set<Step> getTaboo() {
		return taboo;
	}

	public Map<Step, Double> getStartStepAfterTime() {
		return startStepAfterTime;
	}

	public void setLastFinishedTimeForOneJob(Step step, double finishedTime) {
		int jobId = step.getJob().getId();
		if (lastFinishedTimeForJobs[jobId] < finishedTime)
			lastFinishedTimeForJobs[jobId] = finishedTime;
	}

	public double[] getLastFinishedTimeForJobs() {
		return lastFinishedTimeForJobs;
	}

	public List<TimeChip> getTimeLineForKthJob(int jobId) {
		if (jobId < timeLinesForEachJob.size())
			return timeLinesForEachJob.get(jobId);

		return null;
	}

	public Map<StepMapMachine, double[]> getArrangedFinishedTimeForKthJob(int jobId) {
		if (jobId < arrangedFinishedTimeForStepsOfEachJob.size())
			return arrangedFinishedTimeForStepsOfEachJob.get(jobId);

		return null;
	}

}

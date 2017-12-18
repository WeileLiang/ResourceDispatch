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

	// ���ϵĽ��ɱ���ʾ�Ѿ��߹��Ҳ������ߵ�Step
	private Set<Step> taboo;

	// ���ϵĿ�ѡ��
	private Set<Step> accessiblePool;

	// ���ϵ�ǰ������Step
	private Step currStep;

	// ���ϵ�ǰ������ʱ��
	private double time;

	private Resource resource;

	private Set<Step> path;

	// ��¼��ѡ����ÿ��Step�ɿ�ʼ������ʱ���
	private Map<Step, Double> startStepAfterTime;

	private double[] lastFinishedTimeForJobs;

	// �������Job�Ѿ������˵�Step��ʱ��Ƭ
	private List<List<TimeChip>> timeLinesForEachJob;

	// ��¼����Job�����Step��Ԥ�����ʱ���Լ�Ӧ�ñ����뵽timeLine���ĸ�λ��
	private List<Map<StepMapMachine, double[]>> arrangedFinishedTimeForStepsOfEachJob;

	public Ant(int id, Resource resource) {
		setId(id);
		setResource(resource);

		// �Ѿ��߹���start,����ʼ��
		currStep = TaskGraph.start;

		taboo = new HashSet<Step>();
		taboo.add(currStep);
		// �������ɴ��Step����TaskGraph��ڵ�ĸ�������
		List<Step> children = TaskGraph.start.getChildren();
		accessiblePool = new HashSet<Step>(children);
		// ��¼ÿ��Step�Ŀɿ�ʼִ��ʱ�䣬���ÿ��Step��Ϊ0
		startStepAfterTime = new HashMap<Step, Double>();
		for (Step child : children)
			startStepAfterTime.put(child, child.getJob().getBeginTime());

		path = new LinkedHashSet<Step>();
		path.add(TaskGraph.start);

		lastFinishedTimeForJobs = new double[TaskGraph.JOBS_COUNT];

		// Ϊÿ��Job��������Ѿ����Ⱥ��˵�Step��ʱ��Ƭ
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
	 * �ѿ�ѡ���е����з�General���͵�Stepת��ΪGeneral
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
				// �Ѹ������������ʱ�丳�����ӽ��
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
			// ���child��������JOIN����ô�ڰѸ�child��ӵ���ѡ��֮ǰ��������ǰ��Step���Ѿ���ɣ�����ӵ�taboo��
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

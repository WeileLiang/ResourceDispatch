package com.lwl.aco;

import java.util.ArrayList;
import java.util.List;

import com.lwl.antcolony.Ant;
import com.lwl.antcolony.AntColony;
import com.lwl.antcolony.Way;
import com.lwl.resource.Machine;
import com.lwl.resource.Resource;
import com.lwl.task.Step;
import com.lwl.task.TaskGraph;

public class Round {
	private ACO aco;

	// ��Ϣ����Ϣ������ѡ��Step��Ӱ��̶�
	private static final double ALPHA = 16;

	// ������Ϣ������ѡ��Step��Ӱ��̶�
	private static final double BETA = 5;

	// ÿֻ����ÿ�ֲ�������Ϣ������
	private static final double Q = 2;

	// ���ֽ�������Ϣ������
	private double[][] detaPhe;

	private Resource mResource;

	// �������ɵ���Ⱥ
	private AntColony antColony;

	private static final double C = 0;
	private static final double P = 1;

	// �ڶ����汾������ʽ��ʽ: eta=Q*(B/|S|+T/SUM(T)),����mQ��Q�� mB��B
	private static final double mQ = 1;
	private static final double mB = 1;

	// �ڶ����汾�ĸ�����Ϣ�صĹ�ʽ:detaPhe=E/(L+C)
	private static final double mE = 10;
	private static final double mC = 0;

	// ���ֵ����·��
	private Way bestWay;
	// �������·�����������ϵ�Id
	private int bestAntId = 0;

	public Round(ACO aco, Resource resource) {
		this.aco = aco;
		this.mResource = resource;
		antColony = new AntColony(resource);

		detaPhe = new double[TaskGraph.TOTAL_STEP_COUNT][TaskGraph.TOTAL_STEP_COUNT];

		while (!allAntsFinished()) {

			for (int i = 0; i < AntColony.ANT_COUNT; i++) {
				if (antColony.getKthAnt(i).isAccessiblePoolEmpty())
					continue;

				Ant ant = antColony.getKthAnt(i);
				StepMapMachine map = chooseNextStep(ant);
				// ��ѡ��Ϊ��
				if (map == null)
					continue;

				Step nextStep = TaskGraph.JOBS[map.getJobId()].getAllSteps().get(map.getStepId());
				Machine machine = ant.getResource().getMachines().get(map.getMachineId());
				double finishedTime = machine.getFinishedTimeAndInsertTimeChip3(map, ant);

				// ��һ��Ҫ�ߵľ���nextStep
				ant.setCurrStep(nextStep);
				// ��nextStep��ӵ�path��
				ant.addToPath(nextStep);
				// ���µ�ǰʱ��
				ant.setTime(finishedTime);
				// ��nextStep������ɱ���
				ant.addToTaboo(nextStep);
				// �ӱ�ѡ����ɾ����step
				ant.getAccessiblePool().remove(nextStep);
				// ��¼Step��Ӧ��Job���ϸ�Step���ʱ��
				ant.setLastFinishedTimeForOneJob(nextStep, finishedTime);
				// �����Step��orBrothers����˵��ѡ���˸�Step������orBrother��Ҫ�ӱ�ѡ����ɾ��
				if (nextStep.getOrBrothers() != null && nextStep.getOrBrothers().size() > 0)
					for (Step orBrother : nextStep.getOrBrothers()) {
						ant.getAccessiblePool().remove(orBrother);
						ant.addToTaboo(orBrother);
					}

				// ��nextStep�ĺ�����ӵ���ѡ����
				List<Step> children = nextStep.getChildren();
				if (children != null && children.size() > 0) {
					ant.getAccessiblePool().addAll(children);
					// ����nextStep�ĺ�������Ŀ�ʼִ��ʱ��
					for (Step child : children) {
						Double beginTimeOfChild = ant.getStartStepAfterTime().get(child);
						// ���child�����翪ʼʱ�䲻���ڣ����߱�parent�Ľ���ʱ���磬�͸��������翪ʼʱ��
						if (beginTimeOfChild == null || beginTimeOfChild < finishedTime)
							ant.getStartStepAfterTime().put(child, finishedTime);
					}
				}

			}
		}

		// ����һ�ֺ�

		// ��ѡ���������·���Լ�������·��������Id
		bestWay = new Way(null, Double.MAX_VALUE, null);
		bestAntId = 0;
		for (int i = 0; i < AntColony.ANT_COUNT; i++) {
			Ant ant = antColony.getKthAnt(i);
			Way way = ant.getWay();
			if (way.getTime() <= bestWay.getTime()) {
				bestWay = way;
				bestAntId = i;
			}
		}

		// ֮�������Ϣ��
		for (int i = 0; i < AntColony.ANT_COUNT; i++) {
			Ant ant = antColony.getKthAnt(i);
			double averagePhe = mE / (ant.getTime() + mC);

			// ����������ǲ������·�������ϣ��������ϵ�·���Ǳ������·����ͬʱ��֮ǰ�Ѿ�ɸѡ�������·��
			if (i == bestAntId && aco.getBestWay() != null) {
				// �������·���ķѵ�ʱ���֮ǰ���٣������������·���ǲ�ͬ�ģ���ô���ӵ���Ϣ�ؼӱ�
				if (bestWay.getTime() <= aco.getBestWay().getTime() && !bestWay.equals(aco.getBestWay()))
					averagePhe *= 2;
			}

			Step[] path = ant.getPahtArray();
			int k = 1;
			while (k < path.length) {// �������ϲ�������Ϣ��
				int id0 = path[k - 1].getGraphId();
				int id1 = path[k].getGraphId();

				detaPhe[id0][id1] += averagePhe;
				// detaPhe[id1][id0] = detaPhe[id0][id1];

				k++;
			}
		}

	}

	private StepMapMachine chooseNextStep(Ant ant) {

		ant.changeAllToGeneralType();

		// ���Ͽɵ����Step����
		List<Step> allowSteps = new ArrayList<Step>(ant.getAccessiblePool());

		// ��ѡ��Ϊ��
		if (allowSteps.size() == 0)
			return null;

		// �����ÿ��list�����ѡ����һ��Step�ĸ�����ɿ�����
		List<List<StepMapMachine>> lists = new ArrayList<List<StepMapMachine>>();

		// �����ÿһ��Step�����п��ܵķ�ʽ���洢��maps��
		for (int i = 0; i < allowSteps.size(); i++) {
			Step step = allowSteps.get(i);
			// System.out.println(step.toString());
			List<StepMapMachine> list = new ArrayList<StepMapMachine>();

			for (int j = 0; j < step.getSuitableMachines().size(); j++) {
				if (mResource.getAvailableMachineIds().contains(step.getSuitableMachines().get(j)))
					// ��ʾJob��Step�������ĸ��豸�����
					list.add(
							new StepMapMachine(step.getJob().getId(), step.getId(), step.getSuitableMachines().get(j)));
			}

			lists.add(list);
		}

		// ���п��ܵ���ɷ�ʽ
		List<StepMapMachine> maps = new ArrayList<StepMapMachine>();
		// ���������eta
		List<Double> etas = new ArrayList<Double>();

		// ����ÿһ�ֿ��ܵķ�ʽ��eta
		for (int i = 0; i < lists.size(); i++) {
			List<StepMapMachine> list = lists.get(i);
			maps.addAll(list);
			// eta�ķ�ĸ
			// List<Double> denumerators = new ArrayList<Double>();
			// double numerator = 0;// eta�ķ���

			// eta�ķ���
			List<Double> mNumerators = new ArrayList<Double>();
			// eta�ķ�ĸ
			double mDenumerator = 0;

			for (int j = 0; j < list.size(); j++) {
				StepMapMachine map = list.get(j);
				Step step = TaskGraph.JOBS[map.getJobId()].getAllSteps().get(map.getStepId());
				Machine machine = ant.getResource().getMachines().get(map.getMachineId());
				double T = machine.getFinishedTime3(map, ant.getStartStepAfterTime().get(step), ant);

				// denumerators.add(list.size() * T);
				// numerator += T;

				mNumerators.add(T);
				mDenumerator += T;
			}

			// ������������eta
			// for (int j = 0; j < denumerators.size(); j++) {
			// double eta = C + P * numerator / denumerators.get(j);
			// etas.add(eta);
			// }

			// ������������eta
			for (int j = 0; j < mNumerators.size(); j++) {
				double eta = mQ * (mB / list.size() + mNumerators.get(j) / mDenumerator);

				etas.add(eta);
			}

		}

		// ���Ը��ʵķ���
		double[] ups = new double[maps.size()];
		// ��ĸ
		double down = 0;

		for (int i = 0; i < ups.length; i++) {
			StepMapMachine map = maps.get(i);
			Step toStep = TaskGraph.JOBS[map.getJobId()].getAllSteps().get(map.getStepId());
			ups[i] = Math.pow(aco.getPheromone()[ant.getCurrStep().getGraphId()][toStep.getGraphId()], ALPHA)
					* Math.pow(etas.get(i), BETA);

			down += ups[i];
		}

		// ��¼�����ɴ���еĸ���
		double[] p = new double[maps.size()];
		// ��¼�����ɴ���еĸ��ʷ�Χ
		double[] scope = new double[maps.size()];

		scope[0] = p[0] = ups[0] / down;

		for (int i = 1; i < p.length; i++) {
			p[i] = ups[i] / down;
			scope[i] = scope[i - 1] + p[i];
		}

		scope[scope.length - 1] = 1;

		// �������һ����[0,1)�������
		double random = Math.random();

		int i = 0;
		while (random > scope[i])
			i++;

		return maps.get(i);
	}

	// // ����T(Oijk),����ȡĳ��step���ض�machine�ϵ����ʱ��
	// private double getSpecificFinishedTime(Ant ant, StepMapMachine map) {
	// Machine machine = ant.getResource().getMachines()
	// .get(map.getMachineId());
	// Step step = TaskGraph.JOBS[map.getJobId()].getAllSteps().get(
	// map.getStepId());
	//
	// return machine.getFinishedTime(step,0);
	// }

	/**
	 * ����ÿֻ���ϵĿ�ѡ���Ƿ�����ж��Ƿ��������϶�������
	 * 
	 * @return
	 */
	private boolean allAntsFinished() {

		for (int i = 0; i < AntColony.ANT_COUNT; i++)
			if (!antColony.getKthAnt(i).isAccessiblePoolEmpty())
				return false;

		return true;
	}

	public Way getBestWay() {
		return bestWay;
	}

	public double[][] getDetaPhe() {
		return detaPhe;
	}

}

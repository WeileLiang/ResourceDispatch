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

	// 信息素信息对蚂蚁选择Step的影响程度
	private static final double ALPHA = 16;

	// 环境信息对蚂蚁选择Step的影响程度
	private static final double BETA = 5;

	// 每只蚂蚁每轮播撒的信息素总量
	private static final double Q = 2;

	// 本轮结束后信息素增量
	private double[][] detaPhe;

	private Resource mResource;

	// 本轮生成的蚁群
	private AntColony antColony;

	private static final double C = 0;
	private static final double P = 1;

	// 第二个版本的启发式公式: eta=Q*(B/|S|+T/SUM(T)),其中mQ即Q， mB即B
	private static final double mQ = 1;
	private static final double mB = 1;

	// 第二个版本的更新信息素的公式:detaPhe=E/(L+C)
	private static final double mE = 10;
	private static final double mC = 0;

	// 本轮的最佳路径
	private Way bestWay;
	// 本轮最佳路径的隶属蚂蚁的Id
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
				// 可选池为空
				if (map == null)
					continue;

				Step nextStep = TaskGraph.JOBS[map.getJobId()].getAllSteps().get(map.getStepId());
				Machine machine = ant.getResource().getMachines().get(map.getMachineId());
				double finishedTime = machine.getFinishedTimeAndInsertTimeChip3(map, ant);

				// 下一步要走的就是nextStep
				ant.setCurrStep(nextStep);
				// 把nextStep添加到path中
				ant.addToPath(nextStep);
				// 更新当前时间
				ant.setTime(finishedTime);
				// 把nextStep放入禁忌表内
				ant.addToTaboo(nextStep);
				// 从备选池中删除该step
				ant.getAccessiblePool().remove(nextStep);
				// 记录Step对应的Job的上个Step完成时间
				ant.setLastFinishedTimeForOneJob(nextStep, finishedTime);
				// 如果该Step有orBrothers链表，说明选择了该Step后，其他orBrother就要从备选池中删除
				if (nextStep.getOrBrothers() != null && nextStep.getOrBrothers().size() > 0)
					for (Step orBrother : nextStep.getOrBrothers()) {
						ant.getAccessiblePool().remove(orBrother);
						ant.addToTaboo(orBrother);
					}

				// 把nextStep的孩子添加到备选池中
				List<Step> children = nextStep.getChildren();
				if (children != null && children.size() > 0) {
					ant.getAccessiblePool().addAll(children);
					// 更新nextStep的孩子最早的开始执行时间
					for (Step child : children) {
						Double beginTimeOfChild = ant.getStartStepAfterTime().get(child);
						// 如果child的最早开始时间不存在，或者比parent的结束时刻早，就更新其最早开始时间
						if (beginTimeOfChild == null || beginTimeOfChild < finishedTime)
							ant.getStartStepAfterTime().put(child, finishedTime);
					}
				}

			}
		}

		// 经历一轮后

		// 先选出本轮最佳路径以及产生该路径的蚂蚁Id
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

		// 之后更新信息素
		for (int i = 0; i < AntColony.ANT_COUNT; i++) {
			Ant ant = antColony.getKthAnt(i);
			double averagePhe = mE / (ant.getTime() + mC);

			// 如果该蚂蚁是产出最佳路径的蚂蚁，即该蚂蚁的路径是本轮最佳路径。同时，之前已经筛选出了最佳路径
			if (i == bestAntId && aco.getBestWay() != null) {
				// 本轮最佳路径耗费的时间比之前的少，并且两条最佳路径是不同的，那么增加的信息素加倍
				if (bestWay.getTime() <= aco.getBestWay().getTime() && !bestWay.equals(aco.getBestWay()))
					averagePhe *= 2;
			}

			Step[] path = ant.getPahtArray();
			int k = 1;
			while (k < path.length) {// 更新蚂蚁产生的信息素
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

		// 蚂蚁可到达的Step集合
		List<Step> allowSteps = new ArrayList<Step>(ant.getAccessiblePool());

		// 可选池为空
		if (allowSteps.size() == 0)
			return null;

		// 里面的每个list代表可选池中一个Step的各种完成可能性
		List<List<StepMapMachine>> lists = new ArrayList<List<StepMapMachine>>();

		// 把完成每一个Step的所有可能的方式都存储在maps中
		for (int i = 0; i < allowSteps.size(); i++) {
			Step step = allowSteps.get(i);
			// System.out.println(step.toString());
			List<StepMapMachine> list = new ArrayList<StepMapMachine>();

			for (int j = 0; j < step.getSuitableMachines().size(); j++) {
				if (mResource.getAvailableMachineIds().contains(step.getSuitableMachines().get(j)))
					// 表示Job的Step可以在哪个设备上完成
					list.add(
							new StepMapMachine(step.getJob().getId(), step.getId(), step.getSuitableMachines().get(j)));
			}

			lists.add(list);
		}

		// 所有可能的完成方式
		List<StepMapMachine> maps = new ArrayList<StepMapMachine>();
		// 所有情况的eta
		List<Double> etas = new ArrayList<Double>();

		// 计算每一种可能的方式的eta
		for (int i = 0; i < lists.size(); i++) {
			List<StepMapMachine> list = lists.get(i);
			maps.addAll(list);
			// eta的分母
			// List<Double> denumerators = new ArrayList<Double>();
			// double numerator = 0;// eta的分子

			// eta的分子
			List<Double> mNumerators = new ArrayList<Double>();
			// eta的分母
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

			// 计算各种情况的eta
			// for (int j = 0; j < denumerators.size(); j++) {
			// double eta = C + P * numerator / denumerators.get(j);
			// etas.add(eta);
			// }

			// 计算各种情况的eta
			for (int j = 0; j < mNumerators.size(); j++) {
				double eta = mQ * (mB / list.size() + mNumerators.get(j) / mDenumerator);

				etas.add(eta);
			}

		}

		// 各自概率的分子
		double[] ups = new double[maps.size()];
		// 分母
		double down = 0;

		for (int i = 0; i < ups.length; i++) {
			StepMapMachine map = maps.get(i);
			Step toStep = TaskGraph.JOBS[map.getJobId()].getAllSteps().get(map.getStepId());
			ups[i] = Math.pow(aco.getPheromone()[ant.getCurrStep().getGraphId()][toStep.getGraphId()], ALPHA)
					* Math.pow(etas.get(i), BETA);

			down += ups[i];
		}

		// 记录各个可达城市的概率
		double[] p = new double[maps.size()];
		// 记录各个可达城市的概率范围
		double[] scope = new double[maps.size()];

		scope[0] = p[0] = ups[0] / down;

		for (int i = 1; i < p.length; i++) {
			p[i] = ups[i] / down;
			scope[i] = scope[i - 1] + p[i];
		}

		scope[scope.length - 1] = 1;

		// 随机产生一个在[0,1)的随机数
		double random = Math.random();

		int i = 0;
		while (random > scope[i])
			i++;

		return maps.get(i);
	}

	// // 计算T(Oijk),即获取某个step在特定machine上的完成时刻
	// private double getSpecificFinishedTime(Ant ant, StepMapMachine map) {
	// Machine machine = ant.getResource().getMachines()
	// .get(map.getMachineId());
	// Step step = TaskGraph.JOBS[map.getJobId()].getAllSteps().get(
	// map.getStepId());
	//
	// return machine.getFinishedTime(step,0);
	// }

	/**
	 * 根据每只蚂蚁的可选池是否空来判断是否所有蚂蚁都走完了
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

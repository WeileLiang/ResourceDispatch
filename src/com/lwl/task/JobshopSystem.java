package com.lwl.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobshopSystem {
	public static final String JOBSHOP_PATH = ".\\jobshops.txt";
	public List<Jobshop> jobshops;

	public JobshopSystem() {
		initJobshops();
	}

	private void initJobshops() {
		jobshops = new ArrayList<>();
		File file = new File(JOBSHOP_PATH);
		if (!file.exists())
			return;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

			String line = null;
			while ((line = reader.readLine()) != null) {
				String name = line.trim();
				String[] ids = reader.readLine().trim().split(" ");
				Set<Integer> set = new HashSet<Integer>();
				for (int i = 0; i < ids.length; i++)
					set.add(Integer.parseInt(ids[i]));

				jobshops.add(new Jobshop(name, set));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

	public boolean handleJob(Jobshop jobshop, Job job) {
		Set<Step> taboo = new HashSet<>();
		Set<Step> accessiblePool = new HashSet<>();

		accessiblePool.add(job.getAllSteps().get(0));

		// 最后一个Step为END，当该结点被添加到taboo中，表示该Jobshop能够完成该Job
		Step end = job.getAllSteps().get(job.getAllSteps().size() - 1);

		while (!taboo.contains(end)) {
			changeAll2General(taboo, accessiblePool);

			// 在转换到General过程中，终结结点已经被添加到了禁忌池中
			if (taboo.contains(end) || accessiblePool.isEmpty())
				break;

			// 标记可选池中是否所有结点都不能由当前车间完成
			boolean allCannotBeFinished = true;
			Step pickStep = null;
			for (Step step : accessiblePool) {
				List<Integer> list = new ArrayList<>(step.getSuitableMachines());
				list.retainAll(jobshop.machineIds);
				if (!list.isEmpty()) {
					pickStep = step;
					allCannotBeFinished = false;
					break;
				}

			}

			if (allCannotBeFinished)
				return false;

			if (pickStep.getChildren() != null)
				accessiblePool.addAll(pickStep.getChildren());

			accessiblePool.remove(pickStep);
			taboo.add(pickStep);
		}

		return true;
	}

	public static void changeAll2General(Set<Step> taboo, Set<Step> accessiblePool) {
		Step[] steps = new Step[accessiblePool.size()];
		accessiblePool.toArray(steps);
		for (int i = 0; i < steps.length; i++)
			changeStepToGeneralType(taboo, accessiblePool, steps[i]);
		
	}

	public static void changeStepToGeneralType(Set<Step> taboo, Set<Step> accessiblePool, Step step) {
		if (step.getType() == Step.GENERAL)
			return;

		if (step.getType() == Step.OR || step.getType() == Step.AND) {
			taboo.add(step);
			accessiblePool.remove(step);
			List<Step> children = step.getChildren();
			if (children != null && children.size() > 0) {
				accessiblePool.addAll(children);
				for (Step child : children)
					changeStepToGeneralType(taboo, accessiblePool, child);
			}
		} else if (step.getType() == Step.JOIN) {

			if (step.isJoinOrStep) {
				// step是跟OR结点对应的JOIN结点，那么只需要任意一条分支即可
				List<Step> list = new ArrayList<>(taboo);
				list.retainAll(step.getFinishedBeforeDone());
				if (!list.isEmpty()) {// taboo中存在一条完整的分支，那么该JOIN结点就能完成了
					// 移除所有约束结点，减少计算量
					accessiblePool.removeAll(step.getFinishedBeforeDone());
					taboo.add(step);
					
					//将该Step的孩子添加到可选池
					List<Step> children = step.getChildren();
					if (children != null && !children.isEmpty()) {
						accessiblePool.addAll(children);
						for (Step child : children)
							changeStepToGeneralType(taboo, accessiblePool, child);
					}
				}
			} else {
				// 如果child的类型是与AND结点对应的JOIN结点，那么在把该child添加到备选池之前必须所有前提Step都已经完成，即添加到taboo中
				List<Step> finishedBeforeDone = step.getFinishedBeforeDone();
				boolean allFinished = true;
				for (Step s : finishedBeforeDone)
					if (!taboo.contains(s)) {
						allFinished = false;
						break;
					}

				if (allFinished) {
					taboo.add(step);
					List<Step> children = step.getChildren();
					if (children != null && children.size() > 0) {
						accessiblePool.addAll(children);
						for (Step child : children)
							changeStepToGeneralType(taboo, accessiblePool, child);

					}
				}
			}

			// JOIN结点无论能否完成都是要移除的
			accessiblePool.remove(step);
		}

	}

	public static class Jobshop {
		String name;
		Set<Integer> machineIds;

		public Jobshop(String name, Set<Integer> ids) {
			this.name = name;
			machineIds = ids;
		}
	}
}

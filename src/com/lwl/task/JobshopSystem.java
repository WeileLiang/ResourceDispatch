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

		// ���һ��StepΪEND�����ý�㱻��ӵ�taboo�У���ʾ��Jobshop�ܹ���ɸ�Job
		Step end = job.getAllSteps().get(job.getAllSteps().size() - 1);

		while (!taboo.contains(end)) {
			changeAll2General(taboo, accessiblePool);

			// ��ת����General�����У��ս����Ѿ�����ӵ��˽��ɳ���
			if (taboo.contains(end) || accessiblePool.isEmpty())
				break;

			// ��ǿ�ѡ�����Ƿ����н�㶼�����ɵ�ǰ�������
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
				// step�Ǹ�OR����Ӧ��JOIN��㣬��ôֻ��Ҫ����һ����֧����
				List<Step> list = new ArrayList<>(taboo);
				list.retainAll(step.getFinishedBeforeDone());
				if (!list.isEmpty()) {// taboo�д���һ�������ķ�֧����ô��JOIN�����������
					// �Ƴ�����Լ����㣬���ټ�����
					accessiblePool.removeAll(step.getFinishedBeforeDone());
					taboo.add(step);
					
					//����Step�ĺ�����ӵ���ѡ��
					List<Step> children = step.getChildren();
					if (children != null && !children.isEmpty()) {
						accessiblePool.addAll(children);
						for (Step child : children)
							changeStepToGeneralType(taboo, accessiblePool, child);
					}
				}
			} else {
				// ���child����������AND����Ӧ��JOIN��㣬��ô�ڰѸ�child��ӵ���ѡ��֮ǰ��������ǰ��Step���Ѿ���ɣ�����ӵ�taboo��
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

			// JOIN��������ܷ���ɶ���Ҫ�Ƴ���
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

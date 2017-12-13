package com.lwl.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lwl.aco.StepMapMachine;
import com.lwl.antcolony.Ant;
import com.lwl.task.Step;
import com.lwl.task.TaskGraph;

/**
 * �豸��
 * 
 * @author LWL
 * 
 */
public class Machine implements Cloneable {
	private int id;
	// �豸��ִ��/��ִ�е�Step
	private List<TimeChip> chips;

	// �ڵ��ȹ����У�ĳ��Step�����ʱ���Լ�Ӧ�ñ����뵽�ĸ�ʱ��Ƭ֮��
	private Map<Step, double[]> finishedTimeForStep;

	public Machine(int id, List<TimeChip> chips) {
		setId(id);
		setChips(chips);
		finishedTimeForStep = new HashMap<Step, double[]>();
	}

	public void setChips(List<TimeChip> chips) {
		this.chips = chips;
		if (this.chips == null)
			this.chips = new ArrayList<TimeChip>();
	}

	public List<TimeChip> getChips() {
		return chips;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public int getNumOfChips() {
		return chips.size();
	}

	/**
	 * �ڵ�ǰʱ�������ж�����ĳ��Step���Step�����ĸ�ʱ���ɱ����
	 * 
	 * beginTimeΪ��Step�����翪ʼʱ��
	 * 
	 * @param step
	 * @return
	 */
	public double getFinishedTime(Step step, double beginTime) {
		// step�ڸ�machine�������Ҫ��ʱ���
		int period = step.getFinishedTimeBaseMachineId(getId());

		int size = getNumOfChips();
		if (size == 0)
			return beginTime + period;

		if (size == 1) {

			if (getChips().get(0).getEnd() <= beginTime
					|| getChips().get(0).getStart() - beginTime >= period)
				return beginTime + period;

			return getChips().get(0).getEnd() + period;
		}

		for (int i = 1; i < size; i++) {
			TimeChip previous = getChips().get(i - 1);
			TimeChip current = getChips().get(i);
			if (previous.getEnd() <= beginTime
					&& current.getStart() - beginTime >= period)
				return beginTime + period;

			if (previous.getEnd() >= beginTime
					&& current.getStart() - previous.getEnd() >= period)
				return previous.getEnd() + period;

		}

		return getChips().get(size - 1).getEnd() + period;
	}

	/**
	 * �ڵ�ǰʱ�������ж�����ĳ��Step���Step�����ĸ�ʱ���ɱ���� ͬʱ�ڸ�machine��ʱ�����ϲ���TimeChip
	 * 
	 * @param step
	 * @return
	 */
	public double getFinishedTimeAndInsertTimeChip(Step step, double beginTime) {
		// step�ڸ�machine�������Ҫ��ʱ���
		int period = step.getFinishedTimeBaseMachineId(getId());

		int size = getNumOfChips();
		if (size == 0) {
			getChips().add(new TimeChip(beginTime, beginTime + period, step));
			return beginTime + period;
		}

		if (getChips().get(0).getStart() - beginTime >= period) {
			getChips()
					.add(0, new TimeChip(beginTime, beginTime + period, step));
			return beginTime + period;
		}

		for (int i = 1; i < size; i++) {
			TimeChip previous = getChips().get(i - 1);
			TimeChip current = getChips().get(i);
			if (previous.getEnd() <= beginTime
					&& current.getStart() - beginTime >= period) {
				getChips().add(i,
						new TimeChip(beginTime, beginTime + period, step));
				return beginTime + period;
			}

			if (previous.getEnd() >= beginTime
					&& current.getStart() - previous.getEnd() >= period) {
				getChips().add(
						i,
						new TimeChip(previous.getEnd(), previous.getEnd()
								+ period, step));
				return previous.getEnd() + period;
			}

		}

		TimeChip chip = getChips().get(size - 1);
		if (chip.getEnd() >= beginTime) {
			getChips().add(
					new TimeChip(chip.getEnd(), chip.getEnd() + period, step));
			return chip.getEnd() + period;

		} else {
			getChips().add(new TimeChip(beginTime, beginTime + period, step));
			return beginTime + period;
		}
	}

	// public double getFinishedTime(Step step, double beginTime, Ant ant) {
	//
	// // step�ڸ�machine�������Ҫ��ʱ���
	// int period = step.getFinishedTimeBaseMachineId(getId());
	// // step������Job��ʱ����
	// List<TimeChip> timeLineOfJob = ant.getTimeLineForKthJob(step.getJob()
	// .getId());
	//
	// int sizeOfTimeLine = timeLineOfJob.size();
	// int sizeOfChipsOnMachine = chips.size();
	//
	// TempTimeChip currentChipOfTimeLine = null;
	// TempTimeChip currentChipOfMachine = null;
	//
	// int currentIndexOfTimeLine = 0;
	// int currentIndexOfMachine = -1;
	// boolean changeChipOfJob = true;
	// boolean changeChipOfMachine = true;
	// while (true) {
	// if (changeChipOfJob) {
	// if (sizeOfTimeLine == 0)
	// currentChipOfTimeLine = new TempTimeChip(beginTime,
	// Double.MAX_VALUE, -1, TempTimeChip.JOB);
	//
	// while (currentIndexOfTimeLine < sizeOfTimeLine - 1) {
	// // System.out.println("��Jobѭ��");
	// TimeChip current = timeLineOfJob
	// .get(currentIndexOfTimeLine);
	// TimeChip next = timeLineOfJob
	// .get(currentIndexOfTimeLine + 1);
	// if (current.getEnd() <= beginTime
	// && next.getStart() - beginTime >= period) {
	// currentChipOfTimeLine = new TempTimeChip(beginTime,
	// next.getStart(), currentIndexOfTimeLine,
	// TempTimeChip.JOB);
	// break;
	// }
	// currentIndexOfTimeLine++;
	// }
	//
	// if (currentIndexOfTimeLine == sizeOfTimeLine - 1) {
	// if (timeLineOfJob.get(currentIndexOfTimeLine).getEnd() > beginTime)
	// currentChipOfTimeLine = new TempTimeChip(timeLineOfJob
	// .get(currentIndexOfTimeLine).getEnd(),
	// Double.MAX_VALUE, currentIndexOfTimeLine,
	// TempTimeChip.JOB);
	// else
	// currentChipOfTimeLine = new TempTimeChip(beginTime,
	// Double.MAX_VALUE, currentIndexOfTimeLine,
	// TempTimeChip.JOB);
	// }
	//
	// }
	//
	// if (changeChipOfMachine) {
	//
	// if (sizeOfChipsOnMachine == 0)
	// currentChipOfMachine = new TempTimeChip(beginTime,
	// Double.MAX_VALUE, -1, TempTimeChip.MACHINE);
	//
	// boolean findNewChip = true;
	// if (currentIndexOfMachine == -1 && sizeOfChipsOnMachine > 0
	// && chips.get(0).getStart() - beginTime >= period) {
	//
	// currentChipOfMachine = new TempTimeChip(beginTime, chips
	// .get(0).getStart(), -1, TempTimeChip.MACHINE);
	// findNewChip = false;
	//
	// } else {
	// if (currentIndexOfMachine == -1)
	// currentIndexOfMachine = 0;
	// }
	//
	// if (findNewChip) {
	// while (currentIndexOfMachine < sizeOfChipsOnMachine - 1) {
	//
	// // System.out.println("��Machineѭ��");
	//
	// TimeChip current = chips.get(currentIndexOfMachine);
	// TimeChip next = chips.get(currentIndexOfMachine + 1);
	// if (current.getEnd() <= beginTime
	// && next.getStart() - beginTime >= period) {
	// currentChipOfMachine = new TempTimeChip(beginTime,
	// next.getStart(), currentIndexOfMachine,
	// TempTimeChip.MACHINE);
	// break;
	// }
	// currentIndexOfMachine++;
	// }
	//
	// if (sizeOfChipsOnMachine != 0
	// && currentIndexOfMachine == sizeOfChipsOnMachine - 1) {
	// if (chips.get(currentIndexOfMachine).getEnd() > beginTime)
	// currentChipOfMachine = new TempTimeChip(chips.get(
	// currentIndexOfMachine).getEnd(),
	// Double.MAX_VALUE, currentIndexOfMachine,
	// TempTimeChip.MACHINE);
	// else
	// currentChipOfMachine = new TempTimeChip(beginTime,
	// Double.MAX_VALUE, currentIndexOfMachine,
	// TempTimeChip.MACHINE);
	//
	// }
	//
	// }
	// }
	//
	// TempTimeChip small = currentChipOfTimeLine.start <
	// currentChipOfMachine.start ? currentChipOfTimeLine
	// : currentChipOfMachine;
	// TempTimeChip large = currentChipOfTimeLine.start <
	// currentChipOfMachine.start ? currentChipOfMachine
	// : currentChipOfTimeLine;
	//
	// if (canContain(small, large)) {
	// finishedTimeForStep.put(step, new double[] {
	// large.start + period,
	// currentChipOfMachine.indexOfPreviousChip + 1 });
	// ant.getFinishedTimeForKthJob(step.getJob().getId())
	// .put(step,
	// new double[] {
	// large.start + period,
	// currentChipOfTimeLine.indexOfPreviousChip + 1 });
	// return large.start + period;
	// } else {
	// if (small.end > large.start) {
	// currentIndexOfTimeLine++;
	// currentIndexOfMachine++;
	// changeChipOfJob = true;
	// changeChipOfMachine = true;
	//
	// } else {
	// if (small.type == TempTimeChip.JOB) {
	// currentIndexOfTimeLine++;
	// changeChipOfJob = true;
	// changeChipOfMachine = false;
	// } else {
	// currentIndexOfMachine++;
	// changeChipOfJob = false;
	// changeChipOfMachine = true;
	// }
	// }
	// }
	// }
	//
	// }

	// public double getFinishedTimeAndInsertTimeChip(Step step, Ant ant) {
	// List<TimeChip> timeLine = ant.getTimeLineForKthJob(step.getJob()
	// .getId());
	//
	// double[] mapOfMachine = finishedTimeForStep.get(step);
	// double[] mapOfTimeLine = ant.getFinishedTimeForKthJob(
	// step.getJob().getId()).get(step);
	//
	// // step�ڸ�machine�������Ҫ��ʱ���
	// int period = step.getFinishedTimeBaseMachineId(getId());
	// chips.add((int) mapOfMachine[1], new TimeChip(mapOfMachine[0] - period,
	// mapOfMachine[0], step));
	//
	// timeLine.add((int) mapOfTimeLine[1], new TimeChip(mapOfTimeLine[0]
	// - period, mapOfTimeLine[0], step));
	//
	// finishedTimeForStep.clear();
	// ant.getFinishedTimeForKthJob(step.getJob().getId()).clear();
	//
	// return mapOfMachine[0];
	//
	// }

	public double getFinishedTime2(Step step, double beginTime, Ant ant) {

		// step�ڸ�machine�������Ҫ��ʱ���
		int period = step.getFinishedTimeBaseMachineId(getId());
		// step������Job��ʱ����
		List<TimeChip> timeLineOfJob = ant.getTimeLineForKthJob(step.getJob()
				.getId());

		int sizeOfTimeLine = timeLineOfJob.size();

		// ����Job��ʱ���������пհ׵�ʱ����
		List<TempTimeChip> freeChipsOfTimeLine = new ArrayList<Machine.TempTimeChip>();

		if (sizeOfTimeLine == 0)
			freeChipsOfTimeLine.add(new TempTimeChip(beginTime,
					Double.MAX_VALUE, -1, TempTimeChip.JOB));

		// ������ʱ��Ƭ֮��Ŀհ�ʱ������ӵ�������
		for (int i = 0; i < sizeOfTimeLine - 1; i++) {
			TimeChip current = timeLineOfJob.get(i);
			TimeChip next = timeLineOfJob.get(i + 1);

			// ֻ�е��հ׵�ʱ��������beginTime֮���Ҽ������>=periodʱ����ӵ�������
			if (current.getEnd() <= beginTime
					&& next.getStart() - beginTime >= period)
				freeChipsOfTimeLine.add(new TempTimeChip(beginTime, next
						.getStart(), i, TempTimeChip.JOB));

		}

		// �����һ��ʱ��Ƭ֮���һ�οհ�ʱ������ӵ�����
		if (sizeOfTimeLine > 0) {
			if (timeLineOfJob.get(sizeOfTimeLine - 1).getEnd() >= beginTime)
				freeChipsOfTimeLine.add(new TempTimeChip(timeLineOfJob.get(
						sizeOfTimeLine - 1).getEnd(), Double.MAX_VALUE,
						sizeOfTimeLine - 1, TempTimeChip.JOB));
			else
				freeChipsOfTimeLine
						.add(new TempTimeChip(beginTime, Double.MAX_VALUE,
								sizeOfTimeLine - 1, TempTimeChip.JOB));

		}

		int sizeOfMachine = chips.size();
		// ��Ÿ��豸�ϵĿհ�ʱ����
		List<TempTimeChip> freeChipsOfMacine = new ArrayList<Machine.TempTimeChip>();

		if (sizeOfMachine == 0)
			freeChipsOfMacine.add(new TempTimeChip(beginTime, Double.MAX_VALUE,
					-1, TempTimeChip.MACHINE));
		else {// �����һ��ʱ��Ƭǰ�Ŀհ�ʱ�����������Step������ӵ�������
			if (chips.get(0).getStart() - beginTime >= period)
				freeChipsOfMacine.add(new TempTimeChip(beginTime, chips.get(0)
						.getStart(), -1, TempTimeChip.MACHINE));
		}

		for (int i = 0; i < sizeOfMachine - 1; i++) {
			TimeChip current = chips.get(i);
			TimeChip next = chips.get(i + 1);
			if (current.getEnd() <= beginTime
					&& next.getStart() - beginTime >= period)
				freeChipsOfMacine.add(new TempTimeChip(beginTime, next
						.getStart(), i, TempTimeChip.MACHINE));
		}

		if (sizeOfMachine > 0) {
			if (chips.get(sizeOfMachine - 1).getEnd() > beginTime)
				freeChipsOfMacine.add(new TempTimeChip(chips.get(
						sizeOfMachine - 1).getEnd(), Double.MAX_VALUE,
						sizeOfMachine - 1, TempTimeChip.MACHINE));
			else
				freeChipsOfMacine.add(new TempTimeChip(beginTime,
						Double.MAX_VALUE, sizeOfMachine - 1,
						TempTimeChip.MACHINE));
		}

		int pointerOfTimeLine = 0;
		int pointerOfMachine = 0;

		while (pointerOfMachine < freeChipsOfMacine.size()
				&& pointerOfTimeLine < freeChipsOfTimeLine.size()) {
			TempTimeChip currentChipOfTimeLine = freeChipsOfTimeLine
					.get(pointerOfTimeLine);
			TempTimeChip currentChipOfMachine = freeChipsOfMacine
					.get(pointerOfMachine);

			// ���ݿհ�ʱ��Ƭ��start��С����������
			TempTimeChip small = currentChipOfTimeLine.start < currentChipOfMachine.start ? currentChipOfTimeLine
					: currentChipOfMachine;
			TempTimeChip large = currentChipOfTimeLine.start < currentChipOfMachine.start ? currentChipOfMachine
					: currentChipOfTimeLine;

			if (small.end - large.start >= period) {
				// finishedTimeForStep.put(step, new double[] {
				// large.start + period,
				// currentChipOfMachine.indexOfPreviousChip + 1 });
				// ant.getFinishedTimeForKthJob(step.getJob().getId())
				// .put(step,
				// new double[] {
				// large.start + period,
				// currentChipOfTimeLine.indexOfPreviousChip + 1 });

				return large.start + period;
			} else {
				if (small.type == TempTimeChip.JOB)
					pointerOfTimeLine++;
				else
					pointerOfMachine++;
			}
		}

		// �豸�Ŀհ�ʱ��Ƭ��δ������
		if (pointerOfMachine < freeChipsOfMacine.size()) {
			// Job��ʱ�����Ͽհ�ʱ��Ƭ�Ѿ������꣬��ȡ�����һ���հ�ʱ��Ƭ
			TempTimeChip chipOfTimeLine = freeChipsOfTimeLine
					.get(freeChipsOfTimeLine.size() - 1);
			while (pointerOfMachine < freeChipsOfMacine.size()) {
				TempTimeChip currentChipOfMachine = freeChipsOfMacine
						.get(pointerOfMachine);

				TempTimeChip small = chipOfTimeLine.start < currentChipOfMachine.start ? chipOfTimeLine
						: currentChipOfMachine;
				TempTimeChip large = chipOfTimeLine.start < currentChipOfMachine.start ? currentChipOfMachine
						: chipOfTimeLine;

				if (small.end - large.start >= period) {
					// finishedTimeForStep.put(step, new double[] {
					// large.start + period,
					// currentChipOfMachine.indexOfPreviousChip + 1 });
					// ant.getFinishedTimeForKthJob(step.getJob().getId()).put(
					// step,
					// new double[] { large.start + period,
					// chipOfTimeLine.indexOfPreviousChip + 1 });
					return large.start + period;
				} else {
					pointerOfMachine++;
				}

			}
		}

		// ��Job�Ŀհ�ʱ��Ƭû�б�����
		if (pointerOfTimeLine < freeChipsOfTimeLine.size()) {
			TempTimeChip chipOfMachine = freeChipsOfMacine
					.get(freeChipsOfMacine.size() - 1);
			while (pointerOfTimeLine < freeChipsOfTimeLine.size()) {
				TempTimeChip currentChipOfTimeLine = freeChipsOfTimeLine
						.get(pointerOfTimeLine);

				TempTimeChip small = currentChipOfTimeLine.start < chipOfMachine.start ? currentChipOfTimeLine
						: chipOfMachine;
				TempTimeChip large = currentChipOfTimeLine.start < chipOfMachine.start ? chipOfMachine
						: currentChipOfTimeLine;

				if (small.end - large.start >= period) {
					// finishedTimeForStep.put(step, new double[] {
					// large.start + period,
					// chipOfMachine.indexOfPreviousChip + 1 });
					// ant.getFinishedTimeForKthJob(step.getJob().getId())
					// .put(step,
					// new double[] {
					// large.start + period,
					// currentChipOfTimeLine.indexOfPreviousChip + 1 });
					return large.start + period;
				} else {
					pointerOfTimeLine++;
				}
			}
		}

		return 0;
	}

	public double getFinishedTimeAndInsertTimeChip2(Step step,
			double beginTime, Ant ant) {

		// step�ڸ�machine�������Ҫ��ʱ���
		int period = step.getFinishedTimeBaseMachineId(getId());
		// step������Job��ʱ����
		List<TimeChip> timeLineOfJob = ant.getTimeLineForKthJob(step.getJob()
				.getId());

		int sizeOfTimeLine = timeLineOfJob.size();

		// ����Job��ʱ���������пհ׵�ʱ����
		List<TempTimeChip> freeChipsOfTimeLine = new ArrayList<Machine.TempTimeChip>();

		if (sizeOfTimeLine == 0)
			freeChipsOfTimeLine.add(new TempTimeChip(beginTime,
					Double.MAX_VALUE, -1, TempTimeChip.JOB));

		// ������ʱ��Ƭ֮��Ŀհ�ʱ������ӵ�������
		for (int i = 0; i < sizeOfTimeLine - 1; i++) {
			TimeChip current = timeLineOfJob.get(i);
			TimeChip next = timeLineOfJob.get(i + 1);

			// ֻ�е��հ׵�ʱ��������beginTime֮���Ҽ������>=periodʱ����ӵ�������
			if (current.getEnd() <= beginTime
					&& next.getStart() - beginTime >= period)
				freeChipsOfTimeLine.add(new TempTimeChip(beginTime, next
						.getStart(), i, TempTimeChip.JOB));

		}

		// �����һ��ʱ��Ƭ֮���һ�οհ�ʱ������ӵ�����
		if (sizeOfTimeLine > 0) {
			if (timeLineOfJob.get(sizeOfTimeLine - 1).getEnd() >= beginTime)
				freeChipsOfTimeLine.add(new TempTimeChip(timeLineOfJob.get(
						sizeOfTimeLine - 1).getEnd(), Double.MAX_VALUE,
						sizeOfTimeLine - 1, TempTimeChip.JOB));
			else
				freeChipsOfTimeLine
						.add(new TempTimeChip(beginTime, Double.MAX_VALUE,
								sizeOfTimeLine - 1, TempTimeChip.JOB));

		}

		int sizeOfMachine = chips.size();
		// ��Ÿ��豸�ϵĿհ�ʱ����
		List<TempTimeChip> freeChipsOfMacine = new ArrayList<Machine.TempTimeChip>();

		if (sizeOfMachine == 0)
			freeChipsOfMacine.add(new TempTimeChip(beginTime, Double.MAX_VALUE,
					-1, TempTimeChip.MACHINE));
		else {// �����һ��ʱ��Ƭǰ�Ŀհ�ʱ�����������Step������ӵ�������
			if (chips.get(0).getStart() - beginTime >= period)
				freeChipsOfMacine.add(new TempTimeChip(beginTime, chips.get(0)
						.getStart(), -1, TempTimeChip.MACHINE));
		}

		for (int i = 0; i < sizeOfMachine - 1; i++) {
			TimeChip current = chips.get(i);
			TimeChip next = chips.get(i + 1);
			if (current.getEnd() <= beginTime
					&& next.getStart() - beginTime >= period)
				freeChipsOfMacine.add(new TempTimeChip(beginTime, next
						.getStart(), i, TempTimeChip.MACHINE));
		}

		if (sizeOfMachine > 0) {
			if (chips.get(sizeOfMachine - 1).getEnd() > beginTime)
				freeChipsOfMacine.add(new TempTimeChip(chips.get(
						sizeOfMachine - 1).getEnd(), Double.MAX_VALUE,
						sizeOfMachine - 1, TempTimeChip.MACHINE));
			else
				freeChipsOfMacine.add(new TempTimeChip(beginTime,
						Double.MAX_VALUE, sizeOfMachine - 1,
						TempTimeChip.MACHINE));
		}

		int pointerOfTimeLine = 0;
		int pointerOfMachine = 0;

		while (pointerOfMachine < freeChipsOfMacine.size()
				&& pointerOfTimeLine < freeChipsOfTimeLine.size()) {
			TempTimeChip currentChipOfTimeLine = freeChipsOfTimeLine
					.get(pointerOfTimeLine);
			TempTimeChip currentChipOfMachine = freeChipsOfMacine
					.get(pointerOfMachine);

			// ���ݿհ�ʱ��Ƭ��start��С����������
			TempTimeChip small = currentChipOfTimeLine.start < currentChipOfMachine.start ? currentChipOfTimeLine
					: currentChipOfMachine;
			TempTimeChip large = currentChipOfTimeLine.start < currentChipOfMachine.start ? currentChipOfMachine
					: currentChipOfTimeLine;

			if (small.end - large.start >= period) {
				// finishedTimeForStep.put(step, new double[] {
				// large.start + period,
				// currentChipOfMachine.indexOfPreviousChip + 1 });
				// ant.getFinishedTimeForKthJob(step.getJob().getId())
				// .put(step,
				// new double[] {
				// large.start + period,
				// currentChipOfTimeLine.indexOfPreviousChip + 1 });
				chips.add(currentChipOfMachine.indexOfPreviousChip + 1,
						new TimeChip(large.start, large.start + period, step));
				ant.getTimeLineForKthJob(step.getJob().getId()).add(
						currentChipOfTimeLine.indexOfPreviousChip + 1,
						new TimeChip(large.start, large.start + period, step));
				return large.start + period;
			} else {
				if (small.type == TempTimeChip.JOB)
					pointerOfTimeLine++;
				else
					pointerOfMachine++;
			}
		}

		// �豸�Ŀհ�ʱ��Ƭ��δ������
		if (pointerOfMachine < freeChipsOfMacine.size()) {
			// Job��ʱ�����Ͽհ�ʱ��Ƭ�Ѿ������꣬��ȡ�����һ���հ�ʱ��Ƭ
			TempTimeChip chipOfTimeLine = freeChipsOfTimeLine
					.get(freeChipsOfTimeLine.size() - 1);
			while (pointerOfMachine < freeChipsOfMacine.size()) {
				TempTimeChip currentChipOfMachine = freeChipsOfMacine
						.get(pointerOfMachine);

				TempTimeChip small = chipOfTimeLine.start < currentChipOfMachine.start ? chipOfTimeLine
						: currentChipOfMachine;
				TempTimeChip large = chipOfTimeLine.start < currentChipOfMachine.start ? currentChipOfMachine
						: chipOfTimeLine;

				if (small.end - large.start >= period) {
					// finishedTimeForStep.put(step, new double[] {
					// large.start + period,
					// currentChipOfMachine.indexOfPreviousChip + 1 });
					// ant.getFinishedTimeForKthJob(step.getJob().getId()).put(
					// step,
					// new double[] { large.start + period,
					// chipOfTimeLine.indexOfPreviousChip + 1 });

					chips.add(currentChipOfMachine.indexOfPreviousChip + 1,
							new TimeChip(large.start, large.start + period,
									step));
					ant.getTimeLineForKthJob(step.getJob().getId()).add(
							chipOfTimeLine.indexOfPreviousChip + 1,
							new TimeChip(large.start, large.start + period,
									step));

					return large.start + period;
				} else {
					pointerOfMachine++;
				}

			}
		}

		// ��Job�Ŀհ�ʱ��Ƭû�б�����
		if (pointerOfTimeLine < freeChipsOfTimeLine.size()) {
			TempTimeChip chipOfMachine = freeChipsOfMacine
					.get(freeChipsOfMacine.size() - 1);
			while (pointerOfTimeLine < freeChipsOfTimeLine.size()) {
				TempTimeChip currentChipOfTimeLine = freeChipsOfTimeLine
						.get(pointerOfTimeLine);

				TempTimeChip small = currentChipOfTimeLine.start < chipOfMachine.start ? currentChipOfTimeLine
						: chipOfMachine;
				TempTimeChip large = currentChipOfTimeLine.start < chipOfMachine.start ? chipOfMachine
						: currentChipOfTimeLine;

				if (small.end - large.start >= period) {
					// finishedTimeForStep.put(step, new double[] {
					// large.start + period,
					// chipOfMachine.indexOfPreviousChip + 1 });
					// ant.getFinishedTimeForKthJob(step.getJob().getId())
					// .put(step,
					// new double[] {
					// large.start + period,
					// currentChipOfTimeLine.indexOfPreviousChip + 1 });
					chips.add(chipOfMachine.indexOfPreviousChip + 1,
							new TimeChip(large.start, large.start + period,
									step));
					ant.getTimeLineForKthJob(step.getJob().getId()).add(
							currentChipOfTimeLine.indexOfPreviousChip + 1,
							new TimeChip(large.start, large.start + period,
									step));
					return large.start + period;
				} else {
					pointerOfTimeLine++;
				}
			}
		}

		return 0;
	}

	public double getFinishedTime3(StepMapMachine map, double beginTime, Ant ant) {

		Step step = TaskGraph.JOBS[map.getJobId()].getAllSteps().get(
				map.getStepId());

		// step�ڸ�machine�������Ҫ��ʱ���
		int period = step.getFinishedTimeBaseMachineId(getId());
		// step������Job��ʱ����
		List<TimeChip> timeLineOfJob = ant.getTimeLineForKthJob(step.getJob()
				.getId());

		int sizeOfTimeLine = timeLineOfJob.size();

		// ����Job��ʱ���������пհ׵�ʱ����
		List<TempTimeChip> freeChipsOfTimeLine = new ArrayList<Machine.TempTimeChip>();

		if (sizeOfTimeLine == 0)
			freeChipsOfTimeLine.add(new TempTimeChip(beginTime,
					Double.MAX_VALUE, -1, TempTimeChip.JOB));

		// ������ʱ��Ƭ֮��Ŀհ�ʱ������ӵ�������
		for (int i = 0; i < sizeOfTimeLine - 1; i++) {
			TimeChip current = timeLineOfJob.get(i);
			TimeChip next = timeLineOfJob.get(i + 1);

			// ֻ�е��հ׵�ʱ��������beginTime֮���Ҽ������>=periodʱ����ӵ�������
			if (current.getEnd() <= beginTime
					&& next.getStart() - beginTime >= period)
				freeChipsOfTimeLine.add(new TempTimeChip(beginTime, next
						.getStart(), i, TempTimeChip.JOB));

		}

		// �����һ��ʱ��Ƭ֮���һ�οհ�ʱ������ӵ�����
		if (sizeOfTimeLine > 0) {
			if (timeLineOfJob.get(sizeOfTimeLine - 1).getEnd() >= beginTime)
				freeChipsOfTimeLine.add(new TempTimeChip(timeLineOfJob.get(
						sizeOfTimeLine - 1).getEnd(), Double.MAX_VALUE,
						sizeOfTimeLine - 1, TempTimeChip.JOB));
			else
				freeChipsOfTimeLine
						.add(new TempTimeChip(beginTime, Double.MAX_VALUE,
								sizeOfTimeLine - 1, TempTimeChip.JOB));

		}

		int sizeOfMachine = chips.size();
		// ��Ÿ��豸�ϵĿհ�ʱ����
		List<TempTimeChip> freeChipsOfMacine = new ArrayList<Machine.TempTimeChip>();

		if (sizeOfMachine == 0)
			freeChipsOfMacine.add(new TempTimeChip(beginTime, Double.MAX_VALUE,
					-1, TempTimeChip.MACHINE));
		else {// �����һ��ʱ��Ƭǰ�Ŀհ�ʱ�����������Step������ӵ�������
			if (chips.get(0).getStart() - beginTime >= period)
				freeChipsOfMacine.add(new TempTimeChip(beginTime, chips.get(0)
						.getStart(), -1, TempTimeChip.MACHINE));
		}

		for (int i = 0; i < sizeOfMachine - 1; i++) {
			TimeChip current = chips.get(i);
			TimeChip next = chips.get(i + 1);
			if (current.getEnd() <= beginTime
					&& next.getStart() - beginTime >= period)
				freeChipsOfMacine.add(new TempTimeChip(beginTime, next
						.getStart(), i, TempTimeChip.MACHINE));
		}

		if (sizeOfMachine > 0) {
			if (chips.get(sizeOfMachine - 1).getEnd() > beginTime)
				freeChipsOfMacine.add(new TempTimeChip(chips.get(
						sizeOfMachine - 1).getEnd(), Double.MAX_VALUE,
						sizeOfMachine - 1, TempTimeChip.MACHINE));
			else
				freeChipsOfMacine.add(new TempTimeChip(beginTime,
						Double.MAX_VALUE, sizeOfMachine - 1,
						TempTimeChip.MACHINE));
		}

		int pointerOfTimeLine = 0;
		int pointerOfMachine = 0;

		while (pointerOfMachine < freeChipsOfMacine.size()
				&& pointerOfTimeLine < freeChipsOfTimeLine.size()) {
			TempTimeChip currentChipOfTimeLine = freeChipsOfTimeLine
					.get(pointerOfTimeLine);
			TempTimeChip currentChipOfMachine = freeChipsOfMacine
					.get(pointerOfMachine);

			// ���ݿհ�ʱ��Ƭ��start��С����������
			TempTimeChip small = currentChipOfTimeLine.start < currentChipOfMachine.start ? currentChipOfTimeLine
					: currentChipOfMachine;
			TempTimeChip large = currentChipOfTimeLine.start < currentChipOfMachine.start ? currentChipOfMachine
					: currentChipOfTimeLine;

			if (small.end - large.start >= period) {
				finishedTimeForStep.put(step, new double[] {
						large.start + period,
						currentChipOfMachine.indexOfPreviousChip + 1 });
				ant.getArrangedFinishedTimeForKthJob(step.getJob().getId())
						.put(map,
								new double[] {
										large.start + period,
										currentChipOfTimeLine.indexOfPreviousChip + 1 });

				return large.start + period;
			} else {
				if (small.type == TempTimeChip.JOB)
					pointerOfTimeLine++;
				else
					pointerOfMachine++;
			}
		}

		// �豸�Ŀհ�ʱ��Ƭ��δ������
		if (pointerOfMachine < freeChipsOfMacine.size()) {
			// Job��ʱ�����Ͽհ�ʱ��Ƭ�Ѿ������꣬��ȡ�����һ���հ�ʱ��Ƭ
			TempTimeChip chipOfTimeLine = freeChipsOfTimeLine
					.get(freeChipsOfTimeLine.size() - 1);
			while (pointerOfMachine < freeChipsOfMacine.size()) {
				TempTimeChip currentChipOfMachine = freeChipsOfMacine
						.get(pointerOfMachine);

				TempTimeChip small = chipOfTimeLine.start < currentChipOfMachine.start ? chipOfTimeLine
						: currentChipOfMachine;
				TempTimeChip large = chipOfTimeLine.start < currentChipOfMachine.start ? currentChipOfMachine
						: chipOfTimeLine;

				if (small.end - large.start >= period) {
					// finishedTimeForStep.put(step, new double[] {
					// large.start + period,
					// currentChipOfMachine.indexOfPreviousChip + 1 });
					// ant.getFinishedTimeForKthJob(step.getJob().getId()).put(
					// step,
					// new double[] { large.start + period,
					// chipOfTimeLine.indexOfPreviousChip + 1 });

					finishedTimeForStep.put(step, new double[] {
							large.start + period,
							currentChipOfMachine.indexOfPreviousChip + 1 });
					ant.getArrangedFinishedTimeForKthJob(step.getJob().getId())
							.put(map,
									new double[] {
											large.start + period,
											chipOfTimeLine.indexOfPreviousChip + 1 });

					return large.start + period;
				} else {
					pointerOfMachine++;
				}

			}
		}

		// ��Job�Ŀհ�ʱ��Ƭû�б�����
		if (pointerOfTimeLine < freeChipsOfTimeLine.size()) {
			TempTimeChip chipOfMachine = freeChipsOfMacine
					.get(freeChipsOfMacine.size() - 1);
			while (pointerOfTimeLine < freeChipsOfTimeLine.size()) {
				TempTimeChip currentChipOfTimeLine = freeChipsOfTimeLine
						.get(pointerOfTimeLine);

				TempTimeChip small = currentChipOfTimeLine.start < chipOfMachine.start ? currentChipOfTimeLine
						: chipOfMachine;
				TempTimeChip large = currentChipOfTimeLine.start < chipOfMachine.start ? chipOfMachine
						: currentChipOfTimeLine;

				if (small.end - large.start >= period) {
					// finishedTimeForStep.put(step, new double[] {
					// large.start + period,
					// chipOfMachine.indexOfPreviousChip + 1 });
					// ant.getFinishedTimeForKthJob(step.getJob().getId())
					// .put(step,
					// new double[] {
					// large.start + period,
					// currentChipOfTimeLine.indexOfPreviousChip + 1 });

					finishedTimeForStep.put(step, new double[] {
							large.start + period,
							chipOfMachine.indexOfPreviousChip + 1 });
					ant.getArrangedFinishedTimeForKthJob(step.getJob().getId())
							.put(map,
									new double[] {
											large.start + period,
											currentChipOfTimeLine.indexOfPreviousChip + 1 });

					return large.start + period;
				} else {
					pointerOfTimeLine++;
				}
			}
		}

		return 0;
	}

	public double getFinishedTimeAndInsertTimeChip3(StepMapMachine map, Ant ant) {

		Step step = TaskGraph.JOBS[map.getJobId()].getAllSteps().get(
				map.getStepId());
		int period = step.getFinishedTimeBaseMachineId(getId());
		
		double finishedTime= finishedTimeForStep.get(step)[0];
		chips.add((int) finishedTimeForStep.get(step)[1],
				new TimeChip(finishedTime - period,
						finishedTime, step));

		int indexForTimeLine = (int) ant.getArrangedFinishedTimeForKthJob(
				step.getJob().getId()).get(map)[1];
		ant.getTimeLineForKthJob(step.getJob().getId()).add(
				indexForTimeLine,
				new TimeChip(finishedTime - period,
						finishedTime, step));

		finishedTimeForStep.clear();
		ant.getArrangedFinishedTimeForKthJob(step.getJob().getId()).clear();
		return finishedTime;
	}

	// �����豸��ʱ��Ƭ��Ϣ
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder("Machine" + getId() + ": ");

		if (chips.size() == 0)
			sb.append("No TimeChips");
		else
			for (int i = 0; i < getChips().size(); i++)
				sb.append(chips.get(i).toString() + " ");

		return sb.toString();
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		Machine o = null;
		try {
			o = (Machine) super.clone();
			o.chips = new ArrayList<TimeChip>();
			for (int i = 0; i < chips.size(); i++) {
				TimeChip chip = (TimeChip) chips.get(i).clone();
				o.chips.add(chip);
			}
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;
	}

	private boolean canContain(TempTimeChip small, TempTimeChip large) {
		return small.start <= large.start && small.end >= large.end;
	}

	class TempTimeChip {
		double start;
		double end;

		int indexOfPreviousChip;

		int type;

		public static final int JOB = 0;
		public static final int MACHINE = 1;

		public TempTimeChip(double start, double end, int indexOfPreviousChip,
				int type) {
			this.start = start;
			this.end = end;
			this.indexOfPreviousChip = indexOfPreviousChip;
			this.type = type;
		}

	}
}

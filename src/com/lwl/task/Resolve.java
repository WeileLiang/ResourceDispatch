package com.lwl.task;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Resolve {

	public static String INIT_FILE_PATH = "graph.txt";
	public static String JOB_PREFIX = "JOB";
	private BufferedReader reader;

	private Job[] jobs;

	// �и���ʼ�ڵ�start,��˳�ʼֵΪ1
	int totalStepCount = 1;

	public Resolve() {
		try {
			initJobs();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Product product = new Product();
		TaskGraph.compute(product.getJobs(), product.getTotalStepCount());
	}

	private void initJobs() throws NumberFormatException, IOException {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(INIT_FILE_PATH)));
		int jobsCount = Integer.parseInt(reader.readLine());

		int[] jobIds = new int[jobsCount];
		String[] jobsPath = new String[jobsCount];
		// ��ȡ����Job���ļ�·��
		for (int i = 0; i < jobsCount; i++) {
			jobIds[i] = Integer.parseInt(reader.readLine().trim());
			jobsPath[i] = JOB_PREFIX + jobIds[i] + ".txt";
		}

		jobs = new Job[jobsCount];
		// ��ȡ����Job.txt����Ϣ��ת��Ϊ��Ӧ��ͼ
		for (int i = 0; i < jobsCount; i++) {
			jobs[i] = new Job(i, jobIds[i]);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(jobsPath[i])));
			// ��i��Job��Step�����Լ���ʼʱ��
			String[] countAndBeginTime = reader.readLine().split(" ");
			int stepCount = Integer.parseInt(countAndBeginTime[0]);
			if (countAndBeginTime.length > 1)
				jobs[i].setBeginTime(Double.parseDouble(countAndBeginTime[1]));

			while (stepCount-- > 0) {
				String idBuffer = reader.readLine();
				addStepToJob(jobs[i], idBuffer);
			}

			reader.readLine();// ����
			addEdgeToJob(jobs[i]);
		}
	}

	// ��ȡ���е�Step���
	private void addStepToJob(Job job, String idBuffer) throws IOException {
		String[] idCoordinate = idBuffer.trim().split(" ");
		int id = Integer.parseInt(idCoordinate[0]);
		int type = 0;
		Step step = null;
		switch (idCoordinate.length) {
		case 1:// �����Step
			type = Step.GENERAL;
			step = new Step(job, id, type, totalStepCount++);
			// ����ɸ�Step���豸�б�
			String machineBuffer = reader.readLine();
			// ��Ӧ�����ʱ��
			String timeBuffer = reader.readLine();
			String[] machineCoordinate = machineBuffer.trim().split(" ");
			String[] timeCoordinate = timeBuffer.trim().split(" ");
			List<Integer> suitableMachines = new ArrayList<Integer>();
			List<Integer> finishedTimes = new ArrayList<Integer>();
			for (int i = 0; i < timeCoordinate.length; i++) {
				suitableMachines.add(Integer.parseInt(machineCoordinate[i]));
				finishedTimes.add(Integer.parseInt(timeCoordinate[i]));
			}

			step.setFinishedTimes(finishedTimes);
			step.setSuitableMachines(suitableMachines);
			break;
		case 2:
			if (idCoordinate[1].equals("AND"))
				type = Step.AND;
			else
				type = Step.OR;

			step = new Step(job, id, type, totalStepCount++);
			break;
		default:
			type = Step.JOIN;
			step = new Step(job, id, type, totalStepCount++);
			break;
		}

		if (type == Step.JOIN) {// �����������JOINʱ����¼�ڰ��Ÿ�Stepǰ������ɵ�Step
			List<Step> finishedBeforeDone = new ArrayList<Step>();
			for (int i = 2; i < idCoordinate.length; i++) {
				int mId = Integer.parseInt(idCoordinate[i]);
				finishedBeforeDone.add(job.getAllSteps().get(mId));
			}

			step.setFinishedBeforeDone(finishedBeforeDone);
		}

		job.addNewStep(step);
		// System.out.println(totalStepCount);

	}

	// �������Ľ����ӵ����Ե�children������
	private void addEdgeToJob(Job job) throws IOException {
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] coordinate = line.trim().split(" ");
			Step step = job.getAllSteps().get(Integer.parseInt(coordinate[0]));
			List<Step> children = new ArrayList<Step>();
			for (int i = 1; i < coordinate.length; i++)
				children.add(job.getAllSteps().get(Integer.parseInt(coordinate[i])));

			step.setChildren(children);
			// �����Step��OR,��ô����ӽ��ʱ����ģ�����ڸ������ӽ�㱣�������Ķ��ӽ�㡣
			if (step.getType() == Step.OR)
				for (Step step2 : children)
					step2.setOrBrothers(children);

		}
	}

	public Job[] getJobs() {
		return jobs;
	}

	public int getTotalStepCount() {
		return totalStepCount;
	}
}

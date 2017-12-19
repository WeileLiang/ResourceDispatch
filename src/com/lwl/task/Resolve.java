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

	// 有个初始节点start,因此初始值为1
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
		JobshopSystem jobshopSystem = new JobshopSystem();
		
//		System.out.println(jobshopSystem.handleJob(jobshopSystem.jobshops.get(0), product.getJobs()[0]));
		TaskGraph.compute(product.getJobs(), product.getTotalStepCount());
	}

	public static void resolve(JobshopSystem jobshopSystem, Product product) {

	}

	private void initJobs() throws NumberFormatException, IOException {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(INIT_FILE_PATH)));
		int jobsCount = Integer.parseInt(reader.readLine());

		int[] jobIds = new int[jobsCount];
		String[] jobsPath = new String[jobsCount];
		// 获取各个Job的文件路径
		for (int i = 0; i < jobsCount; i++) {
			jobIds[i] = Integer.parseInt(reader.readLine().trim());
			jobsPath[i] = JOB_PREFIX + jobIds[i] + ".txt";
		}

		jobs = new Job[jobsCount];
		// 读取各个Job.txt的信息，转换为对应的图
		for (int i = 0; i < jobsCount; i++) {
			jobs[i] = new Job(i, jobIds[i]);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(jobsPath[i])));
			// 第i个Job的Step总数以及开始时间
			String[] countAndBeginTime = reader.readLine().split(" ");
			int stepCount = Integer.parseInt(countAndBeginTime[0]);
			if (countAndBeginTime.length > 1)
				jobs[i].setBeginTime(Double.parseDouble(countAndBeginTime[1]));

			while (stepCount-- > 0) {
				String idBuffer = reader.readLine();
				addStepToJob(jobs[i], idBuffer);
			}

			reader.readLine();// 空行
			addEdgeToJob(jobs[i]);
		}
	}

	// 获取所有的Step结点
	private void addStepToJob(Job job, String idBuffer) throws IOException {
		String[] idCoordinate = idBuffer.trim().split(" ");
		int id = Integer.parseInt(idCoordinate[0]);
		int type = 0;
		Step step = null;
		switch (idCoordinate.length) {
		case 1:// 常规的Step
			type = Step.GENERAL;
			step = new Step(job, id, type, totalStepCount++);
			// 能完成该Step的设备列表
			String machineBuffer = reader.readLine();
			// 对应的完成时间
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
			else if (idCoordinate[1].equals("OR"))
				type = Step.OR;

			step = new Step(job, id, type, totalStepCount++);
			break;
		default:
			if (idCoordinate[1].equals("JOIN")) {
				type = Step.JOIN;
				step = new Step(job, id, type, totalStepCount++);
			}
			break;
		}

		if (type == Step.JOIN) {// 当结点类型是JOIN时，记录在安排该Step前必须完成的Step
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

	// 把相连的结点添加到各自的children链表中
	private void addEdgeToJob(Job job) throws IOException {
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] coordinate = line.trim().split(" ");
			Step step = job.getAllSteps().get(Integer.parseInt(coordinate[0]));
			List<Step> children = new ArrayList<Step>();
			for (int i = 1; i < coordinate.length; i++)
				children.add(job.getAllSteps().get(Integer.parseInt(coordinate[i])));

			step.setChildren(children);
			// 如果该Step是OR,那么其儿子结点时互斥的，因此在各个儿子结点保存其他的儿子结点。
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

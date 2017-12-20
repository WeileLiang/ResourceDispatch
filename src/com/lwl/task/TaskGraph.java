package com.lwl.task;

import java.beans.beancontext.BeanContext;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.print.attribute.standard.JobSheets;

import com.lwl.aco.ACO;
import com.lwl.antcolony.Way;
import com.lwl.resource.Resource;

/**
 * ����Job���ɵ�ͼ
 * 
 * @author LWL
 * 
 */
public class TaskGraph {
	// ����Jobͼ�����
	public static Step start;

	// start�ĺ�������
	public static List<Step> children;

	// ��������JOb����Step����Ŀ
	public static int TOTAL_STEP_COUNT = 1;

	// ��Ÿ���Jobͼ�ṹ���ļ�·������·��
	private static final String INIT_FILE_PATH = "graph.txt";
	// ����Job�ṹ�ļ���ǰ׺
	private static final String PREFIX_FILE_NAME = "JOB";
	// ����Job�����֣���ţ�
	private static int[] jobsName;
	// Job����Ŀ
	public static int JOBS_COUNT;
	// ��Ÿ���Jobͼ�ṹ���ļ�·��
	private static String[] JOBS_FILE_PATH;

	public static Job[] JOBS;
	private BufferedReader reader;

	public TaskGraph() throws IOException {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(INIT_FILE_PATH)));
		JOBS_COUNT = Integer.parseInt(reader.readLine());
		// System.out.println(""+JOBS_COUNT);
		JOBS_FILE_PATH = new String[JOBS_COUNT];
		jobsName = new int[JOBS_COUNT];

		// ��ȡ����Job���ļ�·��
		for (int i = 0; i < JOBS_COUNT; i++) {
			jobsName[i] = Integer.parseInt(reader.readLine());
			JOBS_FILE_PATH[i] = PREFIX_FILE_NAME + jobsName[i] + ".txt";
		}

		JOBS = new Job[JOBS_COUNT];
		// ��ȡ����Job.txt����Ϣ��ת��Ϊ��Ӧ��ͼ
		for (int i = 0; i < JOBS_COUNT; i++) {
			JOBS[i] = new Job(i, jobsName[i]);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(JOBS_FILE_PATH[i])));
			// ��i��Job��Step�����Լ���ʼʱ��
			String[] countAndBeginTime = reader.readLine().split(" ");
			int stepCount = Integer.parseInt(countAndBeginTime[0]);
			if (countAndBeginTime.length > 1)
				JOBS[i].setBeginTime(Double.parseDouble(countAndBeginTime[1]));

			while (stepCount-- > 0) {
				String idBuffer = reader.readLine();
				addStepToJob(JOBS[i], idBuffer);
			}

			reader.readLine();// ����
			addEdgeToJob(JOBS[i]);
		}

		children = new ArrayList<Step>();
		for (int i = 0; i < JOBS_COUNT; i++)
			children.add(JOBS[i].getAllSteps().get(0));

		start = new Step(new Job(-1, -1), 0, Step.AND, 0);
		start.setChildren(children);

	}

	public static int tempTotalStepCount;

	public TaskGraph(Job[] jobs, int totalStepCount) {
		JOBS = jobs;
		JOBS_COUNT = jobs.length;
		TOTAL_STEP_COUNT = totalStepCount;

		children = new ArrayList<Step>();
		for (int i = 0; i < JOBS_COUNT; i++)
			children.add(JOBS[i].getAllSteps().get(0));

		start = new Step(new Job(-1, -1), 0, Step.AND, 0);
		start.setChildren(children);
	}

	// public static void main(String[] args) throws IOException {
	//// TaskGraph graph = new TaskGraph();
	// Resolve resolve=new Resolve();
	// TaskGraph graph = new
	// TaskGraph(resolve.getJobs(),resolve.getTotalStepCount());
	// Resource initResource = new Resource(null);
	// ACO aco = new ACO(initResource);
	// for (int i = 0; i < ACO.MAX_ROUND; i++) {
	// aco.startNewRound(i);
	// }
	//
	// Way bestWay = aco.getBestWay();
	// System.out.println("Time: " + bestWay.getTime());
	// System.out.println(bestWay.getResource());
	// }
	
	public static Resource curResource=new Resource();
	
	public static Way compute(Job[] jobs, int totalStepCount, Set<Integer> availableMachineIds) {
		TaskGraph graph = new TaskGraph(jobs, totalStepCount);
		curResource.setAvailableMachineIds(availableMachineIds);
		ACO aco = new ACO(curResource);
		for (int i = 0; i < ACO.MAX_ROUND; i++) {
			aco.startNewRound(i);
		}

		return aco.getBestWay();
		// System.out.println("Time: " + bestWay.getTime());
		// System.out.println(bestWay.getResource());
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

	// ��ȡ���е�Step���
	private void addStepToJob(Job job, String idBuffer) throws IOException {
		String[] idCoordinate = idBuffer.trim().split(" ");
		int id = Integer.parseInt(idCoordinate[0]);
		int type = 0;
		Step step = null;
		switch (idCoordinate.length) {
		case 1:// �����Step
			type = Step.GENERAL;
			step = new Step(job, id, type, TOTAL_STEP_COUNT++);
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

			step = new Step(job, id, type, TOTAL_STEP_COUNT++);
			break;
		default:
			type = Step.JOIN;
			step = new Step(job, id, type, TOTAL_STEP_COUNT++);
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

}

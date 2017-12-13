package com.lwl.aco;

import java.util.Arrays;

import com.lwl.antcolony.Way;
import com.lwl.resource.Resource;
import com.lwl.task.TaskGraph;

public class ACO {

	// ѭ����������������������ѭ��������Ž���Ϊ���ս��
	public static final int MAX_ROUND = 100;

	// ��·����ʼ��Ϣ��Ũ��
	private static final double INIT_PHE = 1;

	// ÿ�ֽ�������Ϣ��Ũ�ȱ����İٷֱ�
	private static final double RHO = 0.7;

	// ��ǰ����Ϣ��Ũ��
	private double[][] pheromone;
	
	//��ǰ��õ���Դ���÷�ʽ
	private Way bestWay;
	//��ǰ���ʱ��
	private double bestTime=Double.MAX_VALUE;
	
	private Round[] rounds;
	
	private Resource initResource;
	
	public ACO(Resource initResource){
		this.initResource=initResource;
		
		pheromone=new double[TaskGraph.TOTAL_STEP_COUNT][TaskGraph.TOTAL_STEP_COUNT];
		
		for (int i = 0; i < TaskGraph.TOTAL_STEP_COUNT; i++) 
			Arrays.fill(pheromone[i], INIT_PHE);
		
		rounds=new Round[MAX_ROUND];
	}
	
	public void startNewRound(int count) {
		
		rounds[count] = new Round(this,initResource);
		
		if (bestWay == null
				|| bestWay.getTime() >=rounds[count].getBestWay().getTime())
			bestWay = rounds[count].getBestWay();

		for (int i = 0; i < TaskGraph.TOTAL_STEP_COUNT; i++)
			for (int j = 0; j <TaskGraph.TOTAL_STEP_COUNT; j++)
				pheromone[i][j] = pheromone[i][j] * RHO
						+ rounds[count].getDetaPhe()[i][j];

	}
	
	public double[][] getPheromone() {
		return pheromone;
	}
	

	public Way getBestWay() {
		return bestWay;
	}
	
}

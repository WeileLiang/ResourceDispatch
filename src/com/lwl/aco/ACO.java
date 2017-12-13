package com.lwl.aco;

import java.util.Arrays;

import com.lwl.antcolony.Way;
import com.lwl.resource.Resource;
import com.lwl.task.TaskGraph;

public class ACO {

	// 循环的轮数，经过该数量的循环后的最优解作为最终结果
	public static final int MAX_ROUND = 100;

	// 各路径初始信息素浓度
	private static final double INIT_PHE = 1;

	// 每轮结束后信息素浓度保留的百分比
	private static final double RHO = 0.7;

	// 当前的信息素浓度
	private double[][] pheromone;
	
	//当前最好的资源配置方式
	private Way bestWay;
	//当前最佳时间
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

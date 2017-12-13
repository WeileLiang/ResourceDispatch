package com.lwl.antcolony;

import com.lwl.resource.Resource;
import com.lwl.task.TaskGraph;

public class AntColony {

	public static final int ANT_COUNT = TaskGraph.JOBS_COUNT*30;
	private Ant[] ants;

	public AntColony(Resource resource) {
		ants = new Ant[ANT_COUNT];

		for (int i = 0; i < ANT_COUNT; i++){
			Resource mResource=(Resource) resource.clone();
			
			ants[i] = new Ant(i, mResource);
		}
	}

	public Ant getKthAnt(int k) {
		if (k >= 0 && k < ANT_COUNT)
			return ants[k];

		return null;

	}

}

package com.lwl.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

	private boolean handleJob(Jobshop jobshop, Job job) {
		Set<Step> taboo = new HashSet<>();
		Set<Step> accessiblePool = new HashSet<>();

		accessiblePool.add(job.getAllSteps().get(0));

		// 最后一个Step为END，当该结点被添加到taboo中，表示该Jobshop能够完成该Job
		Step end = job.getAllSteps().get(job.getAllSteps().size() - 1);

		while (!taboo.contains(end)) {
				
		}
		
		return true;
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

package com.lwl.resource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.lwl.task.Job;
import com.lwl.task.Step;

/**
 * 资源类，统筹设备的集合
 * 
 * @author LWL
 * 
 */
public class Resource implements Cloneable {
	// 计时器
	private Timer timer;
	private TimerTask task;
	
	public static final String INIT_MACHINE_PATH="initMachine.txt";

	// 资源集合
	private List<Machine> machines;
	// 计时器计时的时间间隔
	public static final int PERIOD = 1000;

	public static final int MACHINE_COUNT = 16;

	public Resource(List<Machine> machines) {
		// setMachines(machines);
		initMachines(); 
		// startTimer();
	}

	private void initMachines() {
		
		BufferedReader reader=null;
		try {
			reader=new BufferedReader(new InputStreamReader(new FileInputStream(
					INIT_MACHINE_PATH)));
			//需要初始化的设备数量
			int initMachineCount=Integer.parseInt(reader.readLine());
			//保存需要初始化的设备的工序
			Map<Integer, List<TimeChip>> chipsOfInitMachines=new HashMap<Integer, List<TimeChip>>();
			
			int idOfInitStep=0;
			for (int i = 0; i < initMachineCount; i++) {
				//空行
				reader.readLine();
				
				String[] idAndNumber=reader.readLine().split(" ");
				//需要初始化的设备编号(1-15)
				int machineId=Integer.parseInt(idAndNumber[0]);
				//这个需要设备初始有多少个工序
				int numberOfChips=Integer.parseInt(idAndNumber[1]);
				
				//保存该设备上初始的工序
				List<TimeChip> chipsOfMachine=new ArrayList<TimeChip>();
				for (int j = 0; j < numberOfChips; j++) {
					//每行代表一个时间片
					String[] chip=reader.readLine().split(" ");
					int start=Integer.parseInt(chip[0]);
					int end=Integer.parseInt(chip[1]);
					chipsOfMachine.add(new TimeChip(start, end, new Step(new Job(-1, -1), idOfInitStep++, Step.GENERAL, 0)));
				}
				//id为machineId的设备对应的初始时间片
				chipsOfInitMachines.put(machineId, chipsOfMachine);
			}
			
			machines = new ArrayList<Machine>();
			for (int i = 0; i < MACHINE_COUNT; i++) {
				machines.add(new Machine(i, chipsOfInitMachines.get(i)));
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

//		machines.get(1).getChips().add(new TimeChip(0, 8, null));
	}

	private void startTimer() {
		timer = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				timeElapse();
			}
		};

		timer.schedule(task, PERIOD, PERIOD);
	}

	// 随着计时器的进行，各个设备Machine的时间轴向前移动
	// 时间片的开始时间和结束时间也随之更改
	private void timeElapse() {
		for (int i = 0; i < machines.size(); i++) {
			Machine machine = machines.get(i);
			for (int j = 0; j < machine.getNumOfChips(); j++) {
				TimeChip chip = machine.getChips().get(j);
				double start = chip.getStart();
				double end = chip.getEnd();

				// 时间片向前移动一个时间单位
				start = start - 1 < 0 ? 0 : start - 1;
				end = end - 1 < 0 ? 0 : end - 1;

				// 如果end为0,即任务执行完毕，移除该时间片
				if (end <= 0)
					machine.getChips().remove(chip);
				else {
					chip.setStart(start);
					chip.setEnd(end);
				}
			}
		}

	}

	public void setMachines(List<Machine> machines) {
		this.machines = machines;
		if (this.machines == null)
			this.machines = new ArrayList<Machine>();
	}

	public List<Machine> getMachines() {
		return machines;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getMachines().size(); i++)
			sb.append(getMachines().get(i).toString()).append('\n');

		sb.append('\n');

		return sb.toString().trim();
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		Resource o = null;

		try {
			o = (Resource) super.clone();
			o.machines = new ArrayList<Machine>();
			for (int i = 0; i < machines.size(); i++) {
				Machine machine = (Machine) machines.get(i).clone();
				o.machines.add(machine);
			}
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return o;
	}

}

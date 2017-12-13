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
 * ��Դ�࣬ͳ���豸�ļ���
 * 
 * @author LWL
 * 
 */
public class Resource implements Cloneable {
	// ��ʱ��
	private Timer timer;
	private TimerTask task;
	
	public static final String INIT_MACHINE_PATH="initMachine.txt";

	// ��Դ����
	private List<Machine> machines;
	// ��ʱ����ʱ��ʱ����
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
			//��Ҫ��ʼ�����豸����
			int initMachineCount=Integer.parseInt(reader.readLine());
			//������Ҫ��ʼ�����豸�Ĺ���
			Map<Integer, List<TimeChip>> chipsOfInitMachines=new HashMap<Integer, List<TimeChip>>();
			
			int idOfInitStep=0;
			for (int i = 0; i < initMachineCount; i++) {
				//����
				reader.readLine();
				
				String[] idAndNumber=reader.readLine().split(" ");
				//��Ҫ��ʼ�����豸���(1-15)
				int machineId=Integer.parseInt(idAndNumber[0]);
				//�����Ҫ�豸��ʼ�ж��ٸ�����
				int numberOfChips=Integer.parseInt(idAndNumber[1]);
				
				//������豸�ϳ�ʼ�Ĺ���
				List<TimeChip> chipsOfMachine=new ArrayList<TimeChip>();
				for (int j = 0; j < numberOfChips; j++) {
					//ÿ�д���һ��ʱ��Ƭ
					String[] chip=reader.readLine().split(" ");
					int start=Integer.parseInt(chip[0]);
					int end=Integer.parseInt(chip[1]);
					chipsOfMachine.add(new TimeChip(start, end, new Step(new Job(-1, -1), idOfInitStep++, Step.GENERAL, 0)));
				}
				//idΪmachineId���豸��Ӧ�ĳ�ʼʱ��Ƭ
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

	// ���ż�ʱ���Ľ��У������豸Machine��ʱ������ǰ�ƶ�
	// ʱ��Ƭ�Ŀ�ʼʱ��ͽ���ʱ��Ҳ��֮����
	private void timeElapse() {
		for (int i = 0; i < machines.size(); i++) {
			Machine machine = machines.get(i);
			for (int j = 0; j < machine.getNumOfChips(); j++) {
				TimeChip chip = machine.getChips().get(j);
				double start = chip.getStart();
				double end = chip.getEnd();

				// ʱ��Ƭ��ǰ�ƶ�һ��ʱ�䵥λ
				start = start - 1 < 0 ? 0 : start - 1;
				end = end - 1 < 0 ? 0 : end - 1;

				// ���endΪ0,������ִ����ϣ��Ƴ���ʱ��Ƭ
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

		return sb.toString();
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

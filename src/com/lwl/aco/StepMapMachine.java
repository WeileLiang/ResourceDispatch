package com.lwl.aco;

/**
 * ´ú±í
 * 
 * @author ROBOT
 *
 */
public class StepMapMachine {

	private int jobId;
	private int stepId;
	private int machineId;
	
	public StepMapMachine(int jobId,int stepId,int machineId){
		setJobId(jobId);
		setMachineId(machineId);
		setStepId(stepId);
	}
	
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public void setMachineId(int machineId) {
		this.machineId = machineId;
	}
	
	public void setStepId(int stepId) {
		this.stepId = stepId;
	}
	
	public int getJobId() {
		return jobId;
	}
	
	public int getMachineId() {
		return machineId;
	}
	
	public int getStepId() {
		return stepId;
	}
	
	@Override
	public boolean equals(Object arg0) {
		// TODO Auto-generated method stub
		
		if (arg0 instanceof StepMapMachine) {
			StepMapMachine map=(StepMapMachine) arg0;
			return jobId==map.getJobId()&&stepId==map.getStepId()&&machineId==map.getMachineId();
		}
		
		return false;
	}
	
}

package com.lwl.antcolony;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lwl.resource.Resource;
import com.lwl.task.Step;

/**
 * ·���࣬��¼�����Ⱥ��߹���ЩStep
 * 
 * @author LWL
 *
 */
public class Way {
	private Step[] way;
	//�������Step�����ʱ��
	private double time;
	//��¼��ȡ��·����Ӧ����Դ�������
	private Resource resource;
	
	public Way(Step[] way, double time , Resource resource){
		setResource(resource);
		setTime(time);
		setWay(way);
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public void setTime(double time) {
		this.time = time;
	}
	
	public void setWay(Step[] way) {
		this.way = way;
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public double getTime() {
		return time;
	}
	
	public Step[] getWay() {
		return way;
	}
	
	//�ж�����·���Ƿ���ͬ
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof Way) {
			List<Step> mList=Arrays.asList(getWay());
			List<Step> oList=Arrays.asList(((Way) obj).getWay());
			
			return mList.equals(oList);
		}
		
		return false;
	}
	
}

package com.lwl.antcolony;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lwl.resource.Resource;
import com.lwl.task.Step;

/**
 * 路径类，记录蚂蚁先后走过哪些Step
 * 
 * @author LWL
 *
 */
public class Way {
	private Step[] way;
	//完成所有Step的最迟时间
	private double time;
	//记录获取该路径对应的资源调度情况
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
	
	//判断两条路径是否相同
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
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder sb=new StringBuilder();
		sb.append("Time: ").append(time).append('\n');
		sb.append(resource);
		
		return sb.toString();
	}
}

package org.fog.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.power.PowerVm;
import org.fog.application.selectivity.SelectivityModel;
import org.fog.scheduler.TupleScheduler;
import org.fog.utils.FogUtils;

/**
 * Class representing an application module, the processing elements of the application model of iFogSim.
 * @author Harshit Gupta
 *
 */
public class AppModule extends PowerVm{

	private String name;
	private String appId;
	private Map<Pair<String, String>, SelectivityModel> selectivityMap;
	private int version = 0;
	
	/**
	 * Mapping from tupleType emitted by this AppModule to Actuators subscribing to that tupleType
	 */
	private Map<String, List<Integer>> actuatorSubscriptions;
	
	public AppModule(
			int id,
			String name,
			String appId,
			int userId,
			double mips,
			int ram,
			long bw,
			long size,
			String vmm,
			CloudletScheduler cloudletScheduler,
			Map<Pair<String, String>, SelectivityModel> selectivityMap) {
		super(id, userId, mips, 1, ram, bw, size, 1, vmm, cloudletScheduler, 300);
		setName(name);
		setId(id);
		setAppId(appId);
		setUserId(userId);
		setUid(getUid(userId, id));
		setMips(mips);
		setNumberOfPes(1);
		setRam(ram);
		setBw(bw);
		setSize(size);
		setVmm(vmm);
		setCloudletScheduler(cloudletScheduler);
		setInMigration(false);
		setBeingInstantiated(true);
		setCurrentAllocatedBw(0);
		setCurrentAllocatedMips(null);
		setCurrentAllocatedRam(0);
		setCurrentAllocatedSize(0);
		setSelectivityMap(selectivityMap);
		setActuatorSubscriptions(new HashMap<String, List<Integer>>());
	}
	public AppModule(AppModule operator) {
		super(FogUtils.generateEntityId(), operator.getUserId(), operator.getMips(), 1, operator.getRam(), operator.getBw(), operator.getSize(), 1, operator.getVmm(), new TupleScheduler(operator.getMips(), 1), operator.getSchedulingInterval());
		setName(operator.getName());
		setAppId(operator.getAppId());
		setInMigration(false);
		setBeingInstantiated(true);
		setCurrentAllocatedBw(0);
		setCurrentAllocatedMips(null);
		setCurrentAllocatedRam(0);
		setCurrentAllocatedSize(0);
		setSelectivityMap(operator.getSelectivityMap());
	}
	
	public void subscribeActuator(int id, String tuplyType){
		if(!getActuatorSubscriptions().containsKey(tuplyType))
			getActuatorSubscriptions().put(tuplyType, new ArrayList<Integer>());
		getActuatorSubscriptions().get(tuplyType).add(id);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Map<Pair<String, String>, SelectivityModel> getSelectivityMap() {
		return selectivityMap;
	}
	
	public void setSelectivityMap(Map<Pair<String, String>, SelectivityModel> selectivityMap) {
		this.selectivityMap = selectivityMap;
	}
	
	public String getAppId() {
		return appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public Map<String, List<Integer>> getActuatorSubscriptions() {
		return actuatorSubscriptions;
	}
	
	public void setActuatorSubscriptions(Map<String, List<Integer>> actuatorSubscriptions) {
		this.actuatorSubscriptions = actuatorSubscriptions;
	}
	
	public String getSelectitvityAsString(){
		String buf="";
		for(Pair<String,String> pair : getSelectivityMap().keySet()){
			//System.out.println(pair.toString());
			buf=buf+"Module Name:"+getName()+"    First:"+pair.getFirst()+"    Second:"+pair.getSecond()+"     Fraction"+getSelectivityMap().get(pair).getSelectivity()+"	";
		}
		return buf;
	}
	
	public String getSelectitvityToString(){
		String buf="";
		for(Pair<String,String> pair : getSelectivityMap().keySet()){
			//System.out.println(pair.toString());
			buf=buf+getName()+"\t"+pair.getFirst()+"\t"+pair.getSecond()+"\t"+getSelectivityMap().get(pair).getSelectivity()+",";
		}
		return buf;
	}
	
	public void setVersion(int version){
		this.version = version;
	}
	
	public int getVersion(){
		return this.version;
	}
	
	public String toString(){
		return "AppModuleName:"+getName()+"   Mips:"+getMips()+"    ram:"+getRam()+"   bw:"+getBw()+" currentAllocatedMips:"+getCurrentAllocatedMips();
	}
	public void resetVersion() {
		// TODO Auto-generated method stub
		this.version = 0;
	}
}
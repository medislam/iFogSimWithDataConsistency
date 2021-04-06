package org.fog.placement;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.examples.DataPlacement;
import org.fog.stats.LatencyStats;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.ModuleLaunchConfig;
import org.fog.utils.NetworkUsageMonitor;
import org.fog.utils.TimeKeeper;
import org.fog2.entities.Actuator;
import org.fog2.entities.FogBroker;
import org.fog2.entities.FogDevice;
import org.fog2.entities.Sensor;

public class Controller extends SimEntity{
	
	public static boolean ONLY_CLOUD = false;
		
	private List<FogDevice> fogDevices;
	private List<Sensor> sensors;
	private List<Actuator> actuators;
	
	private Map<String, Application> applications;
	private Map<String, Integer> appLaunchDelays;
	private ModuleMapping moduleMapping;

	public Controller(String name, List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators, ModuleMapping moduleMapping) {
		super(name);
		this.applications = new HashMap<String, Application>();
		setAppLaunchDelays(new HashMap<String, Integer>());
		setModuleMapping(moduleMapping);
		for(FogDevice fogDevice : fogDevices){
			fogDevice.setControllerId(getId());
		}
		setFogDevices(fogDevices);
		setActuators(actuators);
		setSensors(sensors);
		connectWithLatencies();
	}

	public Controller(String name, List<FogDevice> fogDevices, ModuleMapping moduleMapping) {
		super(name);
		this.applications = new HashMap<String, Application>();
		setAppLaunchDelays(new HashMap<String, Integer>());
		setModuleMapping(moduleMapping);
		for(FogDevice fogDevice : fogDevices){
			fogDevice.setControllerId(getId());
		}
		setFogDevices(fogDevices);
		//connectWithLatencies();
	}

	private FogDevice getFogDeviceById(int id){
		for(FogDevice fogDevice : getFogDevices()){
			if(id==fogDevice.getId())
				return fogDevice;
		}
		return null;
	}
	
	private void connectWithLatencies(){
		System.out.println("Controller, compute connect with latencies!");
		
		for(FogDevice fogDevice : getFogDevices()){
			FogDevice parent = getFogDeviceById(fogDevice.getParentId());
			if(parent == null)
				continue;
			float latency = fogDevice.getUplinkLatency();
			parent.getChildToLatencyMap().put(fogDevice.getId(), latency);
			parent.getChildrenIds().add(fogDevice.getId());			
		}
	}
	
	@Override
	public void startEntity() {
		System.out.println("Start Controller...");
//		System.out.println("Number of applications ="+applications.size());
		
//		for(String appId : applications.keySet()){
//			if(DataPlacement.submitApplication){
//				processAppSubmit(applications.get(appId));
//				DataPlacement.submitApplication=false;
//			}
			
		processAppSubmit(FogBroker.application);
		
//			if(getAppLaunchDelays().get(appId)==0){
//				processAppSubmit(applications.get(appId));
//			
//			}else{
//				send(getId(), getAppLaunchDelays().get(appId), FogEvents.APP_SUBMIT, applications.get(appId));
//			}
//		}

		//send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
		send(getId(), Config.MAX_SIMULATION_TIME, FogEvents.STOP_SIMULATION);

	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.APP_SUBMIT:
			processAppSubmit(ev);
			break;
		case FogEvents.TUPLE_FINISHED:
			processTupleFinished(ev);
			break;
		case FogEvents.CONTROLLER_RESOURCE_MANAGE:
			manageResources();
			break;
		case FogEvents.STOP_SIMULATION:
			CloudSim.stopSimulation();
			//printTimeDetails();
			//printPowerDetails();
			//printCostDetails();
			//printNetworkUsageDetails();
			System.out.println("Simulations riched its max time!");
			System.out.println("Overal read latency:"+LatencyStats.getOverall_read_Latency());
			System.out.println("Overal write latency:"+LatencyStats.getOverall_write_Latency());
			System.out.println("Overal latency:"+LatencyStats.getOverall_Latency());
			break;
			
		}
	}
	
	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
//		System.out.println("Dealocate modules");
//		for(FogDevice dev: fogDevices){
//			System.out.println("deallocate module from device:"+dev.getName());
//			for(AppModule module : ModulePlacement.deviceToModuleMap.get(dev.getName())){
//				Vm vm = (Vm) module;
//				dev.getVmAllocationPolicy().deallocateHostForVm(vm);
//			}
//			
//		}
	}
	
	private void printNetworkUsageDetails() {
		System.out.println("Total network usage = "+NetworkUsageMonitor.getNetworkUsage()/Config.MAX_SIMULATION_TIME);
		
	}

	private FogDevice getCloud(){
		for(FogDevice dev : getFogDevices())
			if(dev.getName().startsWith("DC"))
				return dev;
		return null;
	}
	
	private void printCostDetails(){
		System.out.println("Cost of execution in cloud = "+getCloud().getTotalCost());
	}
	private void printPowerDetails() {
		// TODO Auto-generated method stub
		for(FogDevice fogDevice : getFogDevices()){
			System.out.println(fogDevice.getName() + " : Energy Consumed = "+fogDevice.getEnergyConsumption());
		}
	}

	private String getStringForLoopId(int loopId){
		for(String appId : getApplications().keySet()){
			Application app = getApplications().get(appId);
			for(AppLoop loop : app.getLoops()){
				if(loop.getLoopId() == loopId)
					return loop.getModules().toString();
			}
		}
		return null;
	}
	private void printTimeDetails() {
		System.out.println("=========================================");
		System.out.println("============== RESULTS ==================");
		System.out.println("=========================================");
		System.out.println("EXECUTION TIME : "+ (Calendar.getInstance().getTimeInMillis() - TimeKeeper.getInstance().getSimulationStartTime()));
		System.out.println("=========================================");
		System.out.println("APPLICATION LOOP DELAYS");
		System.out.println("=========================================");
		for(Integer loopId : TimeKeeper.getInstance().getLoopIdToTupleIds().keySet()){
			/*double average = 0, count = 0;
			for(int tupleId : TimeKeeper.getInstance().getLoopIdToTupleIds().get(loopId)){
				Double startTime = 	TimeKeeper.getInstance().getEmitTimes().get(tupleId);
				Double endTime = 	TimeKeeper.getInstance().getEndTimes().get(tupleId);
				if(startTime == null || endTime == null)
					break;
				average += endTime-startTime;
				count += 1;
			}
			System.out.println(getStringForLoopId(loopId) + " ---> "+(average/count));*/
			System.out.println(getStringForLoopId(loopId) + " ---> "+TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loopId));
		}
		System.out.println("=========================================");
		System.out.println("TUPLE CPU EXECUTION DELAY");
		System.out.println("=========================================");
		
		for(String tupleType : TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().keySet()){
			System.out.println(tupleType + " ---> "+TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().get(tupleType));
		}
		
		System.out.println("=========================================");
	}

	protected void manageResources(){
		System.out.println("Controller Resource Manager! ");
		//To Do here what resources management!
		//Send ev to the next period
		send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
	}
	
	private void processTupleFinished(SimEvent ev) {
	}
	
	
	public void submitApplication(Application application, int delay){
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		getAppLaunchDelays().put(application.getAppId(), delay);
		for(Sensor sensor : sensors){
			sensor.setApp(application);
		}
		for(Actuator ac : actuators){
			ac.setApp(application);
		}
		
		for(AppEdge edge : application.getEdges()){
			if(edge.getEdgeType() == AppEdge.ACTUATOR){
				String moduleName = edge.getSource();
				for(Actuator actuator : getActuators()){
					if(actuator.getActuatorType().equalsIgnoreCase(edge.getDestination().get(0)))
						application.getModuleByName(moduleName).subscribeActuator(actuator.getId(), edge.getTupleType());
				}
			}
		}
		
	}
	
	public void submitApplication2(Application application, int delay){
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		getAppLaunchDelays().put(application.getAppId(), delay);

	}
	
	
	
	private void processAppSubmit(SimEvent ev){
		Application app = (Application) ev.getData();
		processAppSubmit(app);
	}
	
	private void processAppSubmit(Application application){
		System.out.println(CloudSim.clock()+" Submitted application "+ application.getAppId());
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		
		//ModulePlacement modulePlacement = new ModulePlacementEdgewards(getFogDevices(), getSensors(), getActuators(), application, getModuleMapping());
		
		//creating modules on devices
		ModulePlacement modulePlacement = new ModulePlacementMapping(getFogDevices(), application, getModuleMapping());
		for(FogDevice fogDevice : fogDevices){
			sendNow(fogDevice.getId(), FogEvents.ACTIVE_APP_UPDATE, application);
		}
		
		Map<Integer, List<AppModule>> deviceToModuleMap = modulePlacement.getDeviceToModuleMap();
		Map<Integer, Map<String, Integer>> instanceCountMap = modulePlacement.getModuleInstanceCountMap();
		for(Integer deviceId : deviceToModuleMap.keySet()){
			for(AppModule module : deviceToModuleMap.get(deviceId)){
				sendNow(deviceId, FogEvents.APP_SUBMIT, application);
				sendNow(deviceId, FogEvents.LAUNCH_MODULE, module);
				sendNow(deviceId, FogEvents.LAUNCH_MODULE_INSTANCE, new ModuleLaunchConfig(module, instanceCountMap.get(deviceId).get(module.getName())));
			}
		}
	}

	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}

	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public Map<String, Integer> getAppLaunchDelays() {
		return appLaunchDelays;
	}

	public void setAppLaunchDelays(Map<String, Integer> appLaunchDelays) {
		this.appLaunchDelays = appLaunchDelays;
	}

	public Map<String, Application> getApplications() {
		return applications;
	}

	public void setApplications(Map<String, Application> applications) {
		this.applications = applications;
	}
	public ModuleMapping getModuleMapping() {
		return moduleMapping;
	}
	public void setModuleMapping(ModuleMapping moduleMapping) {
		this.moduleMapping = moduleMapping;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		for(Sensor sensor : sensors)
			sensor.setControllerId(getId());
		this.sensors = sensors;
	}

	public List<Actuator> getActuators() {
		return actuators;
	}

	public void setActuators(List<Actuator> actuators) {
		this.actuators = actuators;
	}
}
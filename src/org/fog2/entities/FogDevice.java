package org.fog2.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.cplex.DataAllocation;
import org.fog.dataConsistency.LastVersionInformation;
import org.fog.examples.DataPlacement;
import org.fog.lpFileConstuction.BasisDelayMatrix;
import org.fog.stats.LatencyStats;
import org.fog.stats.Stats;
import org.fog.placement.ModuleMapping;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;
import org.fog.utils.Logger;
import org.fog.utils.ModuleLaunchConfig;
import org.fog.utils.NetworkUsageMonitor;
import org.fog.utils.TimeKeeper;

public class FogDevice extends PowerDatacenter {
	protected Queue<Tuple> northTupleQueue;
	protected Queue<Pair<Tuple, Integer>> southTupleQueue;
	
	protected List<String> activeApplications;
	
	protected Map<String, Application> applicationMap;
	protected Map<String, List<String>> appToModulesMap;
	protected Map<Integer, Float> childToLatencyMap;
 
	protected Map<Integer, Integer> cloudTrafficMap;
	
	protected double lockTime;
	
	private int level;
	/**	
	 * ID of the parent Fog Device
	 */
	protected int parentId;
	
	/**
	 * IDs of left right
	 */
	protected int leftId;
	protected int rightId;
	
	/**
	 * IDs of the children Fog devices
	 */
	protected List<Integer> childrenIds;
		
	/**
	 * Latencies of left and right FogDivces
	 */
	protected float leftLatency;
	protected float rightLatency;
	protected float uplinkLatency;
	
	
	protected float uplinkBandwidth;
	protected float downlinkBandwidth;
	protected float leftlinkBandwidth;
	protected float rightlinkBandwidth;
	/**
	 * ID of the Controller
	 */
	protected int controllerId;
	

	protected Map<Integer, List<String>> childToOperatorsMap;
	
	/**
	 * Flag denoting whether the link southwards from this FogDevice is busy
	 */
	protected boolean isSouthLinkBusy;
	
	/**
	 * Flag denoting whether the link northwards from this FogDevice is busy
	 */
	protected boolean isNorthLinkBusy;
	
	
	/**
	 * Flag denoting whether the link leftwards from this FogDevice is busy
	 */
	protected boolean isLeftLinkBusy;
	
	/**
	 * Flag denoting whether the link rightwards from this FogDevice is busy
	 */
	protected boolean isRightLinkBusy;
	
	
	protected List<Pair<Integer, Double>> associatedActuatorIds;
	
	protected double energyConsumption;
	protected double lastUtilizationUpdateTime;
	protected double lastUtilization;
	
	
	protected double ratePerMips;
	
	protected double totalCost;
	
	protected Map<String, Map<String, Integer>> moduleInstanceCount;
	
	public Map<String, StoredDataQueue> storedData = null;
	
	public Map<Long,List<LastVersionInformation>> lastVersionMap = new HashMap<Long,List<LastVersionInformation>>();
	
	public long requestId = 0;
	
	public FogDevice(
			String name, 
			FogDeviceCharacteristics characteristics, 
			VmAllocationPolicy vmAllocationPolicy, 
			List<Storage> storageList,  
			int rightId, 
			int leftId, 
			float rightLatency, 
			float leftLatency, 
			double schedulingInterval,
            float uplinkBandwidth, 
            float downlinkBandwidth, 
            float uplinkLatency, 
            double ratePerMips
            ) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		setCharacteristics(characteristics);
		setVmAllocationPolicy(vmAllocationPolicy);
		setLastProcessTime(0.0);
		setStorageList(storageList);
		setVmList(new ArrayList<Vm>());
		setSchedulingInterval(schedulingInterval);
		setUplinkBandwidth(uplinkBandwidth);
		setDownlinkBandwidth(downlinkBandwidth);
		setUplinkLatency(uplinkLatency);
		setRatePerMips(ratePerMips);
		setAssociatedActuatorIds(new ArrayList<Pair<Integer, Double>>());
		
		setLeftId(leftId);
		setRightId(rightId);
		setRightLatency(rightLatency);
		setLeftLatency(leftLatency);
		
		for (Host host : getCharacteristics().getHostList()) {
			host.setDatacenter(this);
		}
		setActiveApplications(new ArrayList<String>());
		// If this resource doesn't have any PEs then no useful at all
		if (getCharacteristics().getNumberOfPes() == 0) {
			throw new Exception(super.getName()
					+ " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
		}
		// stores id of this class
		getCharacteristics().setId(super.getId());
		
		applicationMap = new HashMap<String, Application>();
		appToModulesMap = new HashMap<String, List<String>>();
		northTupleQueue = new LinkedList<Tuple>();
		southTupleQueue = new LinkedList<Pair<Tuple, Integer>>();
		setNorthLinkBusy(false);
		setSouthLinkBusy(false);
		
		
		setChildrenIds(new ArrayList<Integer>());
		setChildToOperatorsMap(new HashMap<Integer, List<String>>());
		
		this.cloudTrafficMap = new HashMap<Integer, Integer>();
		
		this.lockTime = 0;
		
		this.energyConsumption = 0;
		this.lastUtilization = 0;
		setTotalCost(0);
		setModuleInstanceCount(new HashMap<String, Map<String, Integer>>());
		setChildToLatencyMap(new HashMap<Integer, Float>());
		this.storedData = new HashMap<String,StoredDataQueue>();
	}
	
	/**
	 * Overrides this method when making a new and different type of resource. <br>
	 * <b>NOTE:</b> You do not need to override {@link #body()} method, if you use this method.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void registerOtherEntity() {
		
	}
	
	@Override
	protected void processOtherEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.TUPLE_ARRIVAL:
			processTupleArrival(ev);
			break;
		case FogEvents.LAUNCH_MODULE:
			processModuleArrival(ev);
			break;
		case FogEvents.RELEASE_OPERATOR:
			processOperatorRelease(ev);
			break;
		case FogEvents.SENSOR_JOINED:
			processSensorJoining(ev);
			break;
		case FogEvents.SEND_PERIODIC_TUPLE:
			sendPeriodicTuple(ev);
			break;
		case FogEvents.APP_SUBMIT:
			processAppSubmit(ev);
			break;
		case FogEvents.UPDATE_NORTH_TUPLE_QUEUE:
			updateNorthTupleQueue();
			break;
		case FogEvents.UPDATE_SOUTH_TUPLE_QUEUE:
			updateSouthTupleQueue();
			break;
		case FogEvents.ACTIVE_APP_UPDATE:
			updateActiveApplications(ev);
			break;
		case FogEvents.ACTUATOR_JOINED:
			processActuatorJoined(ev);
			break;
		case FogEvents.LAUNCH_MODULE_INSTANCE:
			updateModuleInstanceCount(ev);
			break;
		case FogEvents.RESOURCE_MGMT:
			manageResources(ev);
			break;
		case FogEvents.TUPLE_STORAGE:
			processTupleStorage(ev);
			break;
		case FogEvents.TUPLE_PROCESS:
			processTupleProcess(ev);
			break;
		case CloudSimTags.VM_DATACENTER_EVENT:
			Object [] tab = (Object []) ev.getData();
			checkCloudletCompletion((Tuple) tab[0]);
			break;
		
		case FogEvents.INITIALIZE_CONSUMPTION:
			if(DataPlacement.load_consumption_times)
				processInitializeConsumption_estimation(ev);
			else
				processInitializeConsumption_intial(ev);
			break;
			
					
		case FogEvents.SEND_DATA_TO_CONSUMER:
			processSendDataToConsumer(ev);
			break;
			
		case FogEvents.DELAY_TUPLE_STORAGE:
			processDelayTupleStorage(ev);
			break;
			
		case FogEvents.VERSION_EXCHANGE:
			processVersionExchange(ev);
			break;
			
		case FogEvents.GET_LAST_VERSION:
			processGetLastVersion(ev);
			break;
			
		case FogEvents.RETURN_LAST_VERSION:
			processReturnLastVersion(ev);
			break;
			
		case FogEvents.INITIALIZE_PERIODIC_TUPLE_PRODUCTION:
			initializePeriodicTupleProduction(ev);
			break;
			
		default:
			break;
		}
	}
	
	private void initializePeriodicTupleProduction(SimEvent ev){
		// TODO Auto-generated method stub
		Object [] tab = (Object[]) ev.getData();
		AppEdge edge = (AppEdge) tab[0];
		List<Double> times = (List<Double>) tab[1];
		
//		float latency = 0;
//		
//		if(this.getName().startsWith("HGW")){
//			latency = 50;
//		}

		for(Double time: times){
			Tuple tuple = FogBroker.application.createTuple(edge, this.getId());
			///*Log.writeIn///*LogFile(this.getName(), "Send tuple arrival time:"+time+"\ttuple:"+tuple.toString());
			//System.out.println(this.getName()+ "Send tuple arrival time:"+time+"\ttuple:"+tuple.toString());
			send(this.getId(), time, FogEvents.TUPLE_ARRIVAL, tuple);
			
			
			//System.out.println("ADD latency to join the broker!");
			//LatencyStats.add_Overall_write_Latency(latency);
			//LatencyStats.addWriteLatency(edge.getTupleType() , latency);
		}
	}

	private void processReturnLastVersion(SimEvent ev){
		// TODO Auto-generated method stub
		Object [] tab = (Object []) ev.getData();
		/*
		 * tab [0]: DataCons Id
		 * tab [1]: destinater service name
		 * tab [2]: edge
		 * tab [3]: list of response replicas
		 * tab [4]: reserved to last version
		 * tab [5]: requestId
		 */
		
		AppEdge edge = (AppEdge) tab[2];
		
		int lastVersion = (int) tab[4];
		int rep = ev.getSource();
		String tupleType = edge.getTupleType();
		long reqId = (long) tab[5];
		
		if(!lastVersionMap.containsKey(reqId)){
			System.out.println(this.getName()+" Error!!! Last Version map should be contain request Id :"+reqId);
			///*Log.writeIn///*LogFile(this.getName()," Error!!! Last Version map should be contain request Id :"+reqId);
			System.exit(0);
		}
		List<LastVersionInformation> lastVersionList = lastVersionMap.get(reqId);
		
		if(lastVersionList==null){
			System.out.println(this.getName()+" Error!!! Last Version list is null for request Id :"+reqId);
			///*Log.writeIn///*LogFile(this.getName()," Error!!! Last Version list is null for request Id :"+reqId);
			System.exit(0);
		}
		
		/*
		 * set last version info
		 */
		for(LastVersionInformation info : lastVersionList){
			if(info.getTupleType().equals(tupleType) && info.getReplicaNodeId()==ev.getSource()){
				info.setVersion(lastVersion);
				break;
			}
		}
		
		/*
		 * check is all replicas have sent their last version
		 */
		int nbLastVersion = 0;
		
		for(LastVersionInformation info : lastVersionList){
			if(info.getVersion()==-1){
				/*
				 * there is a replica that not already send its last version
				 */
				break;
			}
			nbLastVersion++;
		}
		
		if(nbLastVersion==lastVersionList.size()){
			/*
			 * Search the latestVersion
			 */
			int recentVersion = -1;
			for(LastVersionInformation info : lastVersionList){
				if (info.getVersion() > recentVersion) {
					recentVersion = info.getVersion();
				}
			}
			
			/*
			 * check if there are some stored replicas ==> recentVersion > -1 
			 */
			if(recentVersion > -1){
				/*
				 * delete the non recent version from the list
				 */
				Iterator<?> itr = lastVersionList.iterator();
				while(itr.hasNext()){
					LastVersionInformation info = (LastVersionInformation) itr.next();
					if (info.getVersion() < recentVersion) {
						itr.remove();
					}
				}
				
				/*
				 * Chose the consumer nearest replica
				 */
				if (!lastVersionList.isEmpty()){
													
					int nearest = -1;
					float latency = Long.MAX_VALUE;
				
					int consumerId = (int) tab[0];
		
					for(LastVersionInformation info : lastVersionList){
						if (latency > BasisDelayMatrix.getFatestLink(consumerId, info.getReplicaNodeId()) ) {
							nearest = info.getReplicaNodeId();
							latency = BasisDelayMatrix.getFatestLink(consumerId, info.getReplicaNodeId());
						}
					}
					
					float delay = BasisDelayMatrix.getFatestLink(this.getId() , nearest);
					
					//**System.out.println(this.getName()+" send an event SEND_DATA_TO_CONSUMER to replics"+nearest+" tuple"+tupleType+" for consumer:"+ (String) tab[1]);
					///*Log.writeIn///*LogFile(this.getName()," send an event SEND_DATA_TO_CONSUMER to replics"+nearest+" tuple"+tupleType+" for consumer:"+ (String) tab[1]);
					send(nearest, delay, FogEvents.SEND_DATA_TO_CONSUMER, tab);
					LatencyStats.add_Overall_read_Latency(delay);
					LatencyStats.addReadLatency(tupleType , delay);
					
				}else{
					System.out.println(this.getName()+" Error!!! recent Last Version list is null for request Id :"+reqId);
					///*Log.writeIn///*LogFile(this.getName()," Error!!! recent Last Version list is null for request Id :"+reqId);
					System.exit(0);
					
				}
			
			}else{
				//**System.out.println(this.getName()+" median doesn't retreive any host that stores the replica of "+tupleType);
				//**System.out.println(this.getName()+" Consumer module:"+(String)tab[1]+" will be not served for:"+tupleType);
				//**System.out.println(this.getName()+" Send to broker  Non retrieve Tuple for edge:"+((AppEdge) tab[2]).toString());
				
				///*Log.writeIn///*LogFile(this.getName(), " median doesn't retreive any host that stores the replica of "+tupleType);
				///*Log.writeIn///*LogFile(this.getName(), "Consumer module:"+(String)tab[1]+" will be not served for:"+tupleType);
				///*Log.writeIn///*LogFile(this.getName()," Send to broker  Non retrieve Tuple for edge:"+((AppEdge) tab[2]).toString());
				
				sendNow(2, FogEvents.NON_TUPLE_RETRIEVE, tab);
				
				Stats.incrementServedRead();
				Stats.incrementReadServedWithRecentVersion(6);

				
				float latency = BasisDelayMatrix.getFatestLink(this.getId(), (int) tab[0]);
				LatencyStats.add_Overall_read_Latency(latency);
				LatencyStats.addReadLatency(tupleType , latency);
				
			}
		}
		
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
		
	}

	private void processGetLastVersion(SimEvent ev) {
		// TODO Auto-generated method stub
		Object [] tab = (Object []) ev.getData();
		/*
		 * tab [0]: DataCons Id
		 * tab [1]: destinater service name
		 * tab [2]: edge
		 * tab [3]: list of response replicas
		 * tab [4]: reserved to last version
		 * tab [5]: requestId
		 */
		AppEdge edge = (AppEdge) tab[2];
				
		//**System.out.println(this.getName()+" Send to median:"+ev.getSource()+" return last version for tupleType:"+edge.getTupleType());
		///*Log.writeIn///*LogFile(this.getName(), " Send to median:"+ev.getSource()+" return last version for tupleType:"+edge.getTupleType());
		
		String tupleType = edge.getTupleType();
		if (!this.storedData.containsKey(tupleType)) {
			System.out.println(this.getName()+" Error!, node"+this.getName()+" doesn't host any replica of "+tupleType);
			///*Log.writeIn///*LogFile(this.getName(), "Error!, node"+this.getName()+" doesn't host any replica of "+tupleType);
			System.exit(0);
			
		} 

		/*
		 * tab [0]: DataCons Id
		 * tab [1]: destinater service name
		 * tab [2]: edge
		 * tab [3]: list of response replicas
		 * tab [4]: last version
		 * tab [5]: requestId
		 */
		
		if(this.storedData.get(tupleType) == null){
			//**System.out.println(this.getName()+" there is no stored replica for "+tupleType+" in this node"+this.getName());
			///*Log.writeIn///*LogFile(this.getName()," there is no stored replica for "+tupleType+" in this node"+this.getName());
			tab[4] = Integer.MIN_VALUE;
			
		}else {
			
			Iterator<Tuple> itr = this.storedData.get(tupleType).iterator();
			Tuple tuple = itr.next();
			tab[4] = tuple.getTupleVersion();
			
			//**System.out.println(this.getName()+" last version:"+tuple.getTupleVersion());
			///*Log.writeIn///*LogFile(this.getName(), " last version:"+tuple.getTupleVersion());
		}
		
		float latency = BasisDelayMatrix.getFatestLink(getId(), ev.getSource());
		send(ev.getSource(), latency, FogEvents.RETURN_LAST_VERSION, tab);
		
		/*
		 * here there is no version exchange latency, because it has been added already in version exchange function
		 */
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
	}

	private void processVersionExchange(SimEvent ev) {
		// TODO Auto-generated method stub
		Object [] tab = (Object []) ev.getData();
		/*
		 * tab [0]: DataCons Id
		 * tab [1]: destinater service name
		 * tab [2]: edge
		 * tab [3]: list of response replicas
		 * tab [4]: reserved to last version
		 * tab [5]: requestId
		 */
		
		
		/*
		 * Send to all response replicas, get last version
		 */
		List<Integer> listOfResponseReplicas = (List<Integer>) tab[3];
		AppEdge edge = (AppEdge) tab[2];
		
		List<LastVersionInformation> lastVersionList = new ArrayList<LastVersionInformation>();
		tab[5] = requestId;
		
		//*System.out.println();
		float maxLatency =0;
		for(int rep : listOfResponseReplicas){
			
			LastVersionInformation info = new LastVersionInformation(edge.getTupleType(),rep);
			lastVersionList.add(info);
			
			Object [] tab2 = new Object [6];
			tab2 = tab.clone();
			
			float latency = BasisDelayMatrix.getFatestLink(getId(), rep);
			//**System.out.println(this.getName()+" Send to replica in:"+rep+" event for get last version for tupleType:"+edge.getTupleType());
			///*Log.writeIn///*LogFile(this.getName(), " Send to replica in:"+rep+" event for get last version for tupleType:"+edge.getTupleType());
			
			
			send(rep, latency, FogEvents.GET_LAST_VERSION, tab2);
			if(maxLatency<latency)
				maxLatency = latency;
		}
		
		Stats.incrementVersionExchangeLatency(maxLatency*2);
		//LatencyStats.add_Overall_read_Latency(maxLatency*2);
		
		lastVersionMap.put(requestId, lastVersionList);
		requestId++;
		
	}

	private void processSendDataToConsumer(SimEvent ev) {
		// TODO Auto-generated method stub
	
		Object [] tab = (Object []) ev.getData();
		
		/*
		 * tab [0]: DataCons Id
		 * tab [1]: destinater service name
		 * tab [2]: edge
		 * tab [3]: list of response replicas
		 * tab [4]: median replicas Node
		 * tab [5]: requestId
		 */
		
		String tupleType = ((AppEdge)tab[2]).getTupleType();
		if (!this.storedData.containsKey(tupleType)) {
			//**System.out.println("Error!, node"+this.getName()+" doesn't host any replica of "+tupleType);
			///*Log.writeIn///*LogFile(this.getName(), "Error!, node"+this.getName()+" doesn't host any replica of "+tupleType);
			System.exit(0);
		}

		if (this.storedData.get(tupleType) == null) {
			//**System.out.println("Node"+this.getName()+" doesn't host any replica of "+tupleType);
			//**System.out.println("Consumer module:"+(String)tab[1]+" will be not served for:"+tupleType+" by node:"+this.getName());
			///*Log.writeIn///*LogFile(this.getName(), "Node"+this.getName()+" doesn't host any replica of "+tupleType);
			///*Log.writeIn///*LogFile(this.getName(), "Consumer module:"+(String)tab[1]+" will be not served for:"+tupleType+" by node:"+this.getName());

			///*Log.writeIn///*LogFile(this.getName()," Send to broker  Non stored Tuple for edge:"+((AppEdge) tab[2]).toString());
			sendNow(2, FogEvents.NON_TUPLE_RETRIEVE, tab);
			
			Stats.incrementServedRead();
			Stats.incrementReadServedWithRecentVersion(6);

			
			float latency = BasisDelayMatrix.getFatestLink(this.getId(), (int) tab[0]);
			LatencyStats.add_Overall_read_Latency(latency);
			LatencyStats.addReadLatency(tupleType , latency);
			
		}else {
			
			/*
			 * tab [0]: DataCons Id <----- tuple
			 * tab [1]: destinater service name
			 * tab [2]: edge
			 * tab [3]: list of response replicas
			 * tab [4]: last version
			 * tab [5]: requestId
			 */
			
			Iterator<Tuple> tuple = this.storedData.get(tupleType).iterator();
			Tuple data = tuple.next();
			
			Tuple tupleSend = (Tuple) data.clone();
			
			List<String> dest = new ArrayList<String>();
			dest.add((String) tab[1]);
			tupleSend.setDestModuleName(dest);
			
			int destDevId = (int) tab[0];
			float latency = BasisDelayMatrix.getFatestLink(getId(), destDevId);
			
			int ex = DataPlacement.Basis_Exchange_Unit;
			long tupleDataSize = data.getCloudletFileSize();
			int nb_Unit = (int) (tupleDataSize / ex);
			if(tupleDataSize % ex != 0) nb_Unit++;
			
			////*System.out.println("tupleSend:"+tupleSend.toString());
			///*Log.writeIn///*LogFile(this.getName(), "Send replica for process:"+tupleSend.toString()+"\n to consumer node :"+destDevId+" consumer module:"+dest.get(0));
			//*System.out.println(this.getName()+ " Send replica for process:"+tupleSend.toString()+"\n to consumer node :"+destDevId+" consumer module:"+dest.get(0));
			
			/*
			 * set tuple in tab[0]
			 */
			tab[0] = tupleSend;
				
			send(destDevId, latency*nb_Unit, FogEvents.TUPLE_PROCESS, tab);	
			
			LatencyStats.add_Overall_read_Latency(latency*nb_Unit);
			LatencyStats.addReadLatency(tupleSend.getTupleType(), latency*nb_Unit);
			
			//Application app = this.applicationMap.get(tupleSend.getAppId());
			Application app = CloudSim.broker.getAppliaction();
			
			AppModule mod = app.getModuleByName(tupleSend.getSrcModuleName());
			
			Stats.incrementServedRead();			
			Stats.incrementReadServedWithRecentVersion(mod.getVersion() - tupleSend.getTupleVersion());
							
//			Scanner sc = new Scanner(System.in);
//			String str = sc.nextLine();
		}
	}
	
	private void processInitializeConsumption_intial(SimEvent ev) {
		// TODO Auto-generated method stub
		Object [] tab = (Object []) ev.getData();
		
		/*
		 * tab [0]: DataProd Id
		 * tab [1]: destinator service name
		 * tab [2]: edge
		 * tab [3]: list of response replicas
		 * tab [4]: median replicas Node
		 */
		
		float delay = (float) (Math.random() * DataPlacement.DataConsRequestInterval + 500);
		
		//float delay = DataPlacement.DataConsRequestInterval;
		
		//*System.out.println(this.getName()+" Send to broker Tuple retrieve for edge:"+((AppEdge) tab[2]).toString()+" ev time:"+(ev.eventTime()+delay));
		///*Log.writeIn///*LogFile(this.getName()," Send to broker Tuple retrieve for edge:"+((AppEdge) tab[2]).toString()+" ev time:"+(ev.eventTime()+delay));
		
		if(ev.eventTime()+delay < Config.MAX_SIMULATION_TIME){
			send(2, delay, FogEvents.TUPLE_RETRIEVE, tab);
			
			if(this.getName().startsWith("HGW")){
				LatencyStats.add_Overall_read_Latency(50);
				AppEdge edge = (AppEdge) tab[2];
				LatencyStats.addReadLatency(edge.getTupleType() , 50);
			}
			
			delay += (float) (Math.random()* DataPlacement.DataConsRequestInterval);
			Object [] tab2 = new Object[6];
			tab2 = tab.clone();
			
			send(this.getId(), delay, FogEvents.INITIALIZE_CONSUMPTION, tab2);	
		}
		

	}
	
	
	private void processInitializeConsumption_estimation(SimEvent ev) {
		// TODO Auto-generated method stub
		Object [] tab = (Object []) ev.getData();
		
		/*
		 * tab [0]: DataProd Id
		 * tab [1]: destinator service name
		 * tab [2]: edge
		 * tab [3]: list of response replicas
		 * tab [4]: median replicas Node
		 */
		

		//*System.out.println(this.getName()+" Send to broker Tuple retrieve for edge:"+((AppEdge) tab[2]).toString()+" ev time:"+(ev.eventTime()+delay));
		///*Log.writeIn///*LogFile(this.getName()," Send to broker Tuple retrieve for edge:"+((AppEdge) tab[2]).toString()+" ev time:"+(ev.eventTime()));
		sendNow(2, FogEvents.TUPLE_RETRIEVE, tab);
			
		if(this.getName().startsWith("HGW")){
			LatencyStats.add_Overall_read_Latency(50);
			AppEdge edge = (AppEdge) tab[2];
			LatencyStats.addReadLatency(edge.getTupleType() , 50);
		}		

	}
	
		
	protected void processDelayTupleStorage(SimEvent ev){
			// TODO Auto-generated method stub
		
			Tuple tuple = (Tuple) ev.getData();
						
			//if(this.applicationMap.get(tuple.getAppId()).getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(), CloudSim.broker.getDataAllocation()).contains(this.getId())){
			if(CloudSim.broker.getAppliaction().getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(), CloudSim.broker.getDataAllocation()).contains(this.getId())){
				/*
				 * this replicas must be is unlocked to apply the delayed write
				 */
							
				///*Log.writeIn///*LogFile(this.getName(), "delayed Storage tuple:"+tuple.toString());
				//System.out.println(this.getName()+ " delayed Storage tuple:"+tuple.toString());
							
				StoredDataQueue storedqueue = this.storedData.get(tuple.getTupleType());
				
				if(storedqueue != null){		
					if(storedqueue.iterator().next().getTupleVersion()> tuple.getTupleVersion()){			
						//System.out.println(this.getName()+ ":Version rollback! this write will be not considred!");
						///*Log.writeIn///*LogFile(this.getName(), " Version rollback! this write will be not considred!");
						this.printAllStoredData();
						
						Stats.incrementVersionRollBack(tuple.getTupleType(), this.getId());
						sendNow(2,  FogEvents.DELAY_TUPLE_STORAGE_ACK, tuple);
						
					}else{
						storedqueue.addData(tuple);
						this.storedData.put(tuple.getTupleType(), storedqueue);
						
						/* Send an ACK to Broker */
						if(this.storedData.get(tuple.getTupleType()).contains(tuple)){
							//System.out.println(this.getName()+ " delayed Tuple has been well stored:"+tuple.toString()+"\n");
							///*Log.writeIn///*LogFile(this.getName(), " delayed Tuple has been well stored:"+tuple.toString()+"\n");
							sendNow(2,  FogEvents.DELAY_TUPLE_STORAGE_ACK, tuple);
									
						}else {
							System.out.println(this.getName()+ " delayed Tuple does not been well stored:"+tuple.toString());
							///*Log.writeIn///*LogFile(this.getName(), "delayed Tuple does not been well stored:"+tuple.toString());
							System.exit(0);
						}
					}				
				}
				
			}else{
				/*
				 * this replicas is locked, so send another delay write tuple
				 */
				//float latency = (float) (Math.random()*DataPlacement.DelayedWriteInLockReplicaTime);
				
				float latency = (float) (DataPlacement.DelayedWriteInLockReplicaTime);
						
				//System.out.println(this.getName()+ " Send old tuple write:"+tuple.toString()+"\nto the non selected storage node:"+this.getId()+" in time event:"+String.valueOf(latency+ev.eventTime()));
				///*Log.writeIn///*LogFile(this.getName(), " Send old tuple write:"+tuple.toString()+"\nto the non selected storage node:"+this.getId()+" in time event:"+String.valueOf(latency+ev.eventTime()));
				
				send(this.getId(), latency, FogEvents.DELAY_TUPLE_STORAGE, tuple);
				
				Stats.incrementDelayedWrite();
				LatencyStats.add_Overall_delayed_write_Latency(latency);
				
				
			}
					
//			Scanner sc = new Scanner(System.in);
//			String str = sc.nextLine();
	}
	
	private void processTupleStorage(SimEvent ev) {
		// TODO Auto-generated method stub
		Tuple tuple = (Tuple) ev.getData();
		////*System.out.println(tuple.toString());
		
		//*System.out.println();
		///*Log.writeIn///*LogFile(this.getName(), "Storage tuple:"+tuple.toString());
		//*System.out.println(this.getName()+ " Storage tuple:"+tuple.toString());
		
		//System.out.println("tuple.getAppId():"+tuple.getAppId());
		
			
		//if(this.applicationMap.get(tuple.getAppId()).getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(), CloudSim.broker.getDataAllocation()).contains(this.getId()) ){
		if(CloudSim.broker.getAppliaction().getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(), CloudSim.broker.getDataAllocation()).contains(this.getId()) ){
			//*System.out.println(this.getName()+ " Error! Storage tuple in unlocked replicas:"+tuple.toString());
			///*Log.writeIn///*LogFile(this.getName(), "  Error! Storage tuple in unlocked replicas:"+tuple.toString());
			System.exit(0);
		}
		
		/* 
		 * if this fog node does not host tuple then create new queue, also add into the end of the queue 
		 */
		if (this.storedData.get(tuple.getTupleType()) == null) {
			//*System.out.println(this.getName()+ " this is the first stored tuple:"+tuple.toString());
			///*Log.writeIn///*LogFile(this.getName(), " this is the first stored tuple:"+tuple.toString());
			
			StoredDataQueue storedqueue = new StoredDataQueue();
			storedqueue.addData(tuple);
			this.storedData.put(tuple.getTupleType(), storedqueue);
			
			/* Send an ACK to Broker*/
			if(this.storedData.get(tuple.getTupleType()).contains(tuple)){
				//*System.out.println(this.getName()+ " Tuple has been well stored:"+tuple.toString()+"\n");
				///*Log.writeIn///*LogFile(this.getName(), "Tuple has been well stored:"+tuple.toString()+"\n");
				sendNow(2,  FogEvents.TUPLE_STORAGE_ACK, tuple);
				
			}else {
				//*System.out.println(tuple.toString());
				this.printAllStoredData();
				//*System.out.println(this.getName()+ " Tuple does not been well stored:"+tuple.toString());
				///*Log.writeIn///*LogFile(this.getName(), "Tuple does not been well stored:"+tuple.toString());
				Application app = CloudSim.broker.getAppliaction();
				CloudSim.broker.getDataAllocation().printDataAllocationMap(app);
				System.exit(0);
			}			
			
		}else{
			//*System.out.println(this.getName()+ " this is not the first stored tuple:"+tuple.toString());
			///*Log.writeIn///*LogFile(this.getName(), " this is not the first stored tuple:"+tuple.toString());
			
			StoredDataQueue storedqueue = this.storedData.get(tuple.getTupleType());
			
			if(storedqueue.iterator().next().getTupleVersion()> tuple.getTupleVersion()){
				
				//*System.out.println(this.getName()+ ":Version rollback! this write will be not considred!");
				///*Log.writeIn///*LogFile(this.getName(), " Version rollback! this write will be not considred!");
				//this.printAllStoredData();
				
				Stats.incrementVersionRollBack(tuple.getTupleType(), this.getId());
				sendNow(2,  FogEvents.TUPLE_STORAGE_ACK, tuple);
				
			}else{
				storedqueue.addData(tuple);
				this.storedData.put(tuple.getTupleType(), storedqueue);
				
				/* Send an ACK to Broker*/
				if(this.storedData.get(tuple.getTupleType()).contains(tuple)){
					//*System.out.println(this.getName()+ " Tuple has been well stored:"+tuple.toString());
					///*Log.writeIn///*LogFile(this.getName(), "Tuple has been well stored:"+tuple.toString());
					sendNow(2,  FogEvents.TUPLE_STORAGE_ACK, tuple);
					
				}else {
					//*System.out.println(tuple.toString());
					this.printAllStoredData();
					//*System.out.println(this.getName()+ " Tuple does not been well stored:"+tuple.toString());
					///*Log.writeIn///*LogFile(this.getName(), "Tuple does not been well stored:"+tuple.toString());
					
					//CloudSim.broker.getDataAllocation().printDataAllocationMap(this.applicationMap.get(tuple.getAppId()));
					Application app = CloudSim.broker.getAppliaction();
					CloudSim.broker.getDataAllocation().printDataAllocationMap(app);
					System.exit(0);
				}
			}
		}
		
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
	}

	/**
	 * Perform miscellaneous resource management tasks
	 * @param ev
	 */
	private void manageResources(SimEvent ev) {
		updateEnergyConsumption();
		send(getId(), Config.RESOURCE_MGMT_INTERVAL, FogEvents.RESOURCE_MGMT);
	}

	/**
	 * Updating the number of modules of an application module on this device
	 * @param ev instance of SimEvent containing the module and no of instances 
	 */
	private void updateModuleInstanceCount(SimEvent ev) {
		ModuleLaunchConfig config = (ModuleLaunchConfig)ev.getData();
		String appId = config.getModule().getAppId();
		
		if(!moduleInstanceCount.containsKey(appId))
			moduleInstanceCount.put(appId, new HashMap<String, Integer>());
		
		moduleInstanceCount.get(appId).put(config.getModule().getName(), config.getInstanceCount());
		////*System.out.println(getName()+ " Creating "+config.getInstanceCount()+" instances of module "+config.getModule().getName());
	}

	/**
	 * Sending periodic tuple for an application edge. Note that for multiple instances of a single source module, only one tuple is sent DOWN while instanceCount number of tuples are sent UP.
	 * @param ev SimEvent instance containing the edge to send tuple on
	 */
	private void sendPeriodicTuple(SimEvent ev) {
		
		AppEdge edge = (AppEdge)ev.getData();
		String srcModule = edge.getSource();
		
		///*Log.writeIn///*LogFile(this.getName(), "send periodic tuple:"+edge.getTupleType()+"\tmodule:"+srcModule);
		
		AppModule module = null;
		for(Vm vm : getHost().getVmList()){
			if(((AppModule)vm).getName().equals(srcModule)){
				module=(AppModule)vm;
				break;
			}
		}
		if(module == null){
			System.out.println("Error ! "+ "module:"+srcModule+"\tis not allocated in "+this.getName());
			///*Log.writeIn///*LogFile(this.getName(), "module:"+srcModule+"\tis not allocated in this node!");
			return;
		}
			
		
		int instanceCount = getModuleInstanceCount().get(module.getAppId()).get(srcModule);
		
		/*
		 * Since tuples sent through a DOWN application edge are anyways broadcasted, only UP tuples are replicated
		 */
		for(int i = 0;i<((edge.getDirection()==Tuple.UP)?instanceCount:1);i++){
			Application app = CloudSim.broker.getAppliaction();
			//Tuple tuple = applicationMap.get(module.getAppId()).createTuple(edge, getId());
			Tuple tuple = app.createTuple(edge, getId());
			////*System.out.println("Sending tuple :"+tuple.toString());
		
			updateTimingsOnSending(tuple);
			sendToSelf(tuple);			
		}
		send(getId(), edge.getPeriodicity(), FogEvents.SEND_PERIODIC_TUPLE, edge);
	}

	protected void processActuatorJoined(SimEvent ev) {
		int actuatorId = ev.getSource();
//		double delay = (double)ev.getData();
//		getAssociatedActuatorIds().add(new Pair<Integer, Double>(actuatorId, delay));
		
		getAssociatedActuatorIds().add(new Pair<Integer, Double>(actuatorId, (double) 10));
	}

	
	protected void updateActiveApplications(SimEvent ev) {
		Application app = (Application)ev.getData();
		getActiveApplications().add(app.getAppId());
	}

	
	public String getOperatorName(int vmId){
		for(Vm vm : this.getHost().getVmList()){
			if(vm.getId() == vmId)
				return ((AppModule)vm).getName();
		}
		return null;
	}
	
	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return the double
	 */
	
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		////*System.out.println("updateCloudetProcessingWithoutSchedulingFutureEventsForce -> fogDevice.java");
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;

		for (PowerHost host : this.<PowerHost> getHostList()) {
			///*Log.printLine();

			double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
			if (time < minTime) {
				minTime = time;
			}

			///*Log.formatLine("%.2f: [Host #%d] utilization is %.2f%%", currentTime, host.getId(), host.getUtilizationOfCpu() * 100);
		}

		if (timeDiff > 0) {
			///*Log.formatLine("\nEnergy consumption for the last time frame from %.2f to %.2f:", getLastProcessTime(), currentTime);

			for (PowerHost host : this.<PowerHost> getHostList()) {
				double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
				double utilizationOfCpu = host.getUtilizationOfCpu();
				double timeFrameHostEnergy = host.getEnergyLinearInterpolation(previousUtilizationOfCpu, utilizationOfCpu, timeDiff);
				
				timeFrameDatacenterEnergy += timeFrameHostEnergy;

				///*Log.printLine();
				///*Log.formatLine("%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%", currentTime, host.getId(), getLastProcessTime(), previousUtilizationOfCpu * 100, utilizationOfCpu * 100);
				///*Log.formatLine("%.2f: [Host #%d] energy is %.2f W*sec", currentTime, host.getId(), timeFrameHostEnergy);
			}

			///*Log.formatLine("\n%.2f: Data center's energy is %.2f W*sec\n", currentTime, timeFrameDatacenterEnergy);
		}

		setPower(getPower() + timeFrameDatacenterEnergy);

		checkCloudletCompletion();

		/** Remove completed VMs **/
		/**
		 * Change made by HARSHIT GUPTA
		 */
		/*for (PowerHost host : this.<PowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				///*Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
		}*/
		
		///*Log.printLine();

		setLastProcessTime(currentTime);
		return minTime;
	}

	protected void checkCloudletCompletion(){
//		////*System.out.println("checkCloudletCompletion -> FogDevice.java");
//		///*Log.writeIn///*LogFile(this.getName(), "checkCloudletCompletion -> FogDevice.java");
//		boolean cloudletCompleted = false;
//		List<? extends Host> list = getVmAllocationPolicy().getHostList();
//		for (int i = 0; i < list.size(); i++) {
//			Host host = list.get(i);
//			////*System.out.println("Host:"+i+"   hostId="+host.getId());
//			///*Log.writeIn///*LogFile(this.getName(), "Host:"+i+"   hostId="+host.getId());
//			for (Vm vm : host.getVmList()) {
//				////*System.out.println("VmId:"+vm.getId()+"    in HostId:"+host.getId());
//				////*System.out.println("vm.getCloudletScheduler().isFinishedCloudlets()= "+vm.getCloudletScheduler().isFinishedCloudlets());
//				///*Log.writeIn///*LogFile(this.getName(), "VmId:"+vm.getId()+"    in HostId:"+host.getId());
//				///*Log.writeIn///*LogFile(this.getName(), "vm.getCloudletScheduler().isFinishedCloudlets()= "+vm.getCloudletScheduler().isFinishedCloudlets());
//				
//				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
//					//*System.out.println("vm.getCloudletScheduler().getNextFinishedCloudlet()");
//					///*Log.writeIn///*LogFile(this.getName(), "vm.getCloudletScheduler().getNextFinishedCloudlet()");
//
//					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
//					if (cl != null) {
//						//*System.out.println("Cloudlet is not null");
//						///*Log.writeIn///*LogFile(this.getName(), "Cloudlet is not null");
//						cloudletCompleted = true;
//						Tuple tuple = (Tuple)cl;
//						
//						TimeKeeper.getInstance().tupleEndedExecution(tuple);
//						Application application = getApplicationMap().get(tuple.getAppId());
//						///*Logger.debug(getName(), "Completed execution of tuple "+tuple.getCloudletId()+"on "+tuple.getDestModuleName());
//						
//						List<Tuple> resultantTuples = application.getResultantTuples(tuple.getDestModuleName().get(0), tuple, getId());
//						////*System.out.println("resultantTuples="+resultantTuples.size());
//						///*Log.writeIn///*LogFile(this.getName(), "resultantTuples="+resultantTuples.size());
//						for(Tuple resTuple : resultantTuples){
//							////*System.out.println("Tuple:"+resTuple.toString());
//							///*Log.writeIn///*LogFile(this.getName(), "Tuple:"+resTuple.toString());
//							resTuple.setModuleCopyMap(new HashMap<String, Integer>(tuple.getModuleCopyMap()));
//							resTuple.getModuleCopyMap().put(((AppModule)vm).getName(), vm.getId());
//							updateTimingsOnSending(resTuple);
//							sendToSelf(resTuple);
//						}
//						//sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
//					}else{
//						//*System.out.println("Cloudlet is null");
//						///*Log.writeIn///*LogFile(this.getName(), "Cloudlet is null");
//					}
//				}
//			}
//		}
//		if(cloudletCompleted)
//			updateAllocatedMips(null);
	}
	
	protected void checkCloudletCompletion(Tuple tuple){
		////*System.out.println("checkCloudletCompletion -> FogDevice.java");
		///*Log.writeIn///*LogFile(this.getName(), "Tuple is:"+tuple.toString());
		///*Log.writeIn///*LogFile(this.getName(), "checkCloudletCompletion -> FogDevice.java");
				
		Vm vm = getVmAllocationPolicy().getHostList().get(0).getVmList().get(0);
		
	
		//Application application = getApplicationMap().get(tuple.getAppId());
		Application application = FogBroker.application;
		
		List<Tuple> resultantTuples = application.getResultantTuples(tuple.getDestModuleName().get(0), tuple, getId());
		////*System.out.println("resultantTuples="+resultantTuples.size());
		///*Log.writeIn///*LogFile(this.getName(), "resultantTuples="+resultantTuples.size());
		for(Tuple resTuple : resultantTuples){
			////*System.out.println("Tuple:"+resTuple.toString());
			///*Log.writeIn///*LogFile(this.getName(), "Tuple:"+resTuple.toString());
			resTuple.setModuleCopyMap(new HashMap<String, Integer>(tuple.getModuleCopyMap()));
			resTuple.getModuleCopyMap().put(((AppModule)vm).getName(), vm.getId());
			updateTimingsOnSending(resTuple);
			sendToSelf(resTuple);
		}
		
	}
	
	protected void updateTimingsOnSending(Tuple resTuple) {
		// TODO ADD CODE FOR UPDATING TIMINGS WHEN A TUPLE IS GENERATED FROM A PREVIOUSLY RECIEVED TUPLE. 
		// WILL NEED TO CHECK IF A NEW LOOP STARTS AND INSERT A UNIQUE TUPLE ID TO IT.
		String srcModule = resTuple.getSrcModuleName();
		String destModule = resTuple.getDestModuleName().get(0);
		////*System.out.println("udpateTimingsOnSending Tuple");
		
		//for(AppLoop loop : getApplicationMap().get(resTuple.getAppId()).getLoops()){
		
		for(AppLoop loop : FogBroker.application.getLoops()){
			if(loop.hasEdge(srcModule, destModule) && loop.isStartModule(srcModule)){
				int tupleId = TimeKeeper.getInstance().getUniqueId();
				resTuple.setActualTupleId(tupleId);
				if(!TimeKeeper.getInstance().getLoopIdToTupleIds().containsKey(loop.getLoopId()))
					TimeKeeper.getInstance().getLoopIdToTupleIds().put(loop.getLoopId(), new ArrayList<Integer>());
				TimeKeeper.getInstance().getLoopIdToTupleIds().get(loop.getLoopId()).add(tupleId);
				TimeKeeper.getInstance().getEmitTimes().put(tupleId, CloudSim.clock());
				
				/////*Logger.debug(getName(), "\tSENDING\t"+tuple.getActualTupleId()+"\tSrc:"+srcModule+"\tDest:"+destModule);
				
			}
		}
	}

	protected int getChildIdWithRouteTo(int targetDeviceId){
		for(Integer childId : getChildrenIds()){
			if(targetDeviceId == childId)
				return childId;
			if(((FogDevice)CloudSim.getEntity(childId)).getChildIdWithRouteTo(targetDeviceId) != -1)
				return childId;
		}
		return -1;
	}
	
	protected int getChildIdForTuple(Tuple tuple){
		if(tuple.getDirection() == Tuple.ACTUATOR){
			int gatewayId = ((Actuator)CloudSim.getEntity(tuple.getActuatorId())).getGatewayDeviceId();
			return getChildIdWithRouteTo(gatewayId);
		}
		return -1;
	}
	
	protected void updateAllocatedMips(String incomingOperator){
		getHost().getVmScheduler().deallocatePesForAllVms();
		for(final Vm vm : getHost().getVmList()){
			if(vm.getCloudletScheduler().runningCloudlets() > 0 || ((AppModule)vm).getName().equals(incomingOperator)){
				getHost().getVmScheduler().allocatePesForVm(vm, new ArrayList<Double>(){
					protected static final long serialVersionUID = 1L;
				{add((double) getHost().getTotalMips());}});
			}else{
				getHost().getVmScheduler().allocatePesForVm(vm, new ArrayList<Double>(){
					protected static final long serialVersionUID = 1L;
				{add(0.0);}});
			}
		}
		
		updateEnergyConsumption();
		
	}
	
	private void updateEnergyConsumption() {
		////*System.out.println("update energy consumption and cost on device :"+getName());
		double totalMipsAllocated = 0;
		for(final Vm vm : getHost().getVmList()){
			AppModule operator = (AppModule)vm;
			operator.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(operator).getVmScheduler().getAllocatedMipsForVm(operator));
			totalMipsAllocated += getHost().getTotalAllocatedMipsForVm(vm);
		}
		
		double timeNow = CloudSim.clock();
		double currentEnergyConsumption = getEnergyConsumption();
		double newEnergyConsumption = currentEnergyConsumption + (timeNow-lastUtilizationUpdateTime)*getHost().getPowerModel().getPower(lastUtilization);
		setEnergyConsumption(newEnergyConsumption);
	
		/*if(getName().equals("d-0")){
			////*System.out.println("------------------------");
			////*System.out.println("Utilization = "+lastUtilization);
			////*System.out.println("Power = "+getHost().getPowerModel().getPower(lastUtilization));
			////*System.out.println(timeNow-lastUtilizationUpdateTime);
		}*/
		
		double currentCost = getTotalCost();
		double newcost = currentCost + (timeNow-lastUtilizationUpdateTime)*getRatePerMips()*lastUtilization*getHost().getTotalMips();
		setTotalCost(newcost);
		
		lastUtilization = Math.min(1, totalMipsAllocated/getHost().getTotalMips());
		lastUtilizationUpdateTime = timeNow;
	}

	protected void processAppSubmit(SimEvent ev) {
		//Application app = (Application)ev.getData();
		Application app = CloudSim.broker.getAppliaction();
		applicationMap.put(app.getAppId(), app);
	}

	protected void addChild(int childId){
		if(CloudSim.getEntityName(childId).toLowerCase().contains("sensor"))
			return;
		if(!getChildrenIds().contains(childId) && childId != getId())
			getChildrenIds().add(childId);
		if(!getChildToOperatorsMap().containsKey(childId))
			getChildToOperatorsMap().put(childId, new ArrayList<String>());
	}
	
	protected void updateCloudTraffic(){
		int time = (int)CloudSim.clock()/1000;
		if(!cloudTrafficMap.containsKey(time))
			cloudTrafficMap.put(time, 0);
		cloudTrafficMap.put(time, cloudTrafficMap.get(time)+1);
	}
	
	protected void sendTupleToActuator(Tuple tuple){
		/*for(Pair<Integer, Double> actuatorAssociation : getAssociatedActuatorIds()){
			int actuatorId = actuatorAssociation.getFirst();
			double delay = actuatorAssociation.getSecond();
			if(actuatorId == tuple.getActuatorId()){
				send(actuatorId, delay, FogEvents.TUPLE_ARRIVAL, tuple);
				return;
			}
		}
		int childId = getChildIdForTuple(tuple);
		if(childId != -1)
			sendDown(tuple, childId);*/
		
		for(Pair<Integer, Double> actuatorAssociation : getAssociatedActuatorIds()){
			int actuatorId = actuatorAssociation.getFirst();
			double delay = actuatorAssociation.getSecond();
			String actuatorType = ((Actuator)CloudSim.getEntity(actuatorId)).getActuatorType();
			if(tuple.getDestModuleName().get(0).equals(actuatorType)){
				int ex = DataPlacement.Basis_Exchange_Unit;
				long tupleDataSize = tuple.getCloudletFileSize();
				int nb_Unit = (int) (tupleDataSize / ex);
				if(tupleDataSize % ex != 0) nb_Unit++;
			
//				LatencyStats.add_Overall_read_Letency(LatencyStats.getOverall_read_Latency()+delay*nb_Unit);
//				LatencyStats.add_Overall_Letency(LatencyStats.getOverall_Latency()+delay*nb_Unit);
				
				////*System.out.println("Node name:"+getName());
				////*System.out.println("Overal read latency:"+LatencyStats.getOverall_read_Latency());
				////*System.out.println("Overal write latency:"+LatencyStats.getOverall_write_Latency());
				////*System.out.println("Overal latency:"+LatencyStats.getOverall_Latency());
				
				send(actuatorId, delay * nb_Unit, FogEvents.TUPLE_PROCESS, tuple);
				return;
			}
		}
//		for(int childId : getChildrenIds()){
//			sendDown(tuple, childId);
//		}
	}
	
	protected void processTupleProcess(SimEvent ev){

		/*
		 * tab [0]: DataCons Id <--- tuple
		 * tab [1]: destinater service name or null
		 * tab [2]: edge or null 
		 * tab [3]: list of response replicas or null
		 * null is in case of sensor tuple 
		 * tab [4]: reserved to last version
		 * tab [5]: requestId
		 */
		
		Object [] tab = (Object []) ev.getData();
		
		Tuple tuple = (Tuple) tab[0];
		
		////*System.out.println();
	////*System.out.println(this.getName()+" processTupleProcess: process tuple and send vm-data-center to self for :"+tuple.getTupleType());
		///*Log.writeIn///*LogFile(this.getName(), "processTupleProcess: process tuple and send vm-data-center to self for :"+tuple.getTupleType());
		
		////*System.out.println("Tuple:"+tuple.toString());
		///*Log.writeIn///*LogFile(this.getName(), "Tuple:"+tuple.toString());
		
		if(ev.getDestination()!=getId()){
			System.out.println("Error!!! ev.Destination Id:"+ev.getDestination()+" is different to the Entity Id"+getId());
			///*Log.writeIn///*LogFile(this.getName(), "Error!!! ev.Destination Id:"+ev.getDestination()+" is different to the Entity Id"+getId());
			System.exit(0);
		}


		if(!tuple.getTupleType().startsWith("TempSNR")){
		////*System.out.println(this.getName()+" Send a TUPLE_RETRIEVE_ACK to the broker for tuple:"+tuple.toString());
			///*Log.writeIn///*LogFile(this.getName(), "Send a TUPLE_RETRIEVE_ACK to the broker for tuple:"+tuple.toString());
			sendNow(2, FogEvents.TUPLE_RETRIEVE_ACK, tab);
		}
		
		//if(appToModulesMap.get(tuple.getAppId()).contains(tuple.getDestModuleName().get(0))){
			/* Search the destination module (vm) in this host */	
			int vmId = -1;
			for(Vm vm : getHost().getVmList()){
				if(((AppModule)vm).getName().equals(tuple.getDestModuleName().get(0))){
					vmId = vm.getId();
					break;
				}
			}	
			
			Application application = CloudSim.broker.getAppliaction();
			//Application application = applicationMap.get(tuple.getAppId());
			String deviceName = ModuleMapping.getDeviceHostModule(tuple.getDestModuleName().get(0));
			//////*System.out.println(deviceName);
			
			int destDevId = application.getFogDeviceByName(deviceName).getId();
			
			if(destDevId != getId()){
				//*System.out.println("Error! Tuple destination module is not deployed in this entity! ->"+destDevId+"   !=   "+getId());
				System.exit(0);
			}
			
			
			if(vmId < 0 || (tuple.getModuleCopyMap().containsKey(tuple.getDestModuleName().get(0)) &&  tuple.getModuleCopyMap().get(tuple.getDestModuleName().get(0))!=vmId )){
				System.out.println("Error! vm Id < 0 or ...");
				///*Log.writeIn///*LogFile(this.getName(), "Error! vm Id < 0 or ...");
				System.exit(0);
			}
			
			

			tuple.setVmId(vmId);
			/////*Logger.error(getName(), "Executing tuple for operator " + moduleName);
			
			//updateTimingsOnReceipt(tuple);
			
			executeTuple(ev, tuple.getDestModuleName().get(0));
			
		//}
		
	}

	protected void processTupleArrival(SimEvent ev){
		////*System.out.println("processTupleArrival: for send tuple to other entites...");
		///*Log.writeIn///*LogFile(this.getName(), "processTupleArrival: for send tuple to other entites...");
		Tuple tuple = (Tuple)ev.getData();
		
		////*System.out.println("Tuple:"+tuple.toString());
		///*Log.writeIn///*LogFile(this.getName(), "Tuple:"+tuple.toString());
		///*Logger.debug(getName(), "Received tuple "+tuple.getCloudletId()+"with tupleType = "+tuple.getTupleType()+"\t| Source : "+CloudSim.getEntityName(ev.getSource())+"|Dest : "+CloudSim.getEntityName(ev.getDestination()));
		
		
		
		if(tuple.getDirection() == Tuple.ACTUATOR){
			sendTupleToActuator(tuple);
			return;
		}
		
		/* get the current version of this tuple & set it also in the appModule */
		//Application app = getApplicationMap().get(tuple.getAppId());
		Application app = FogBroker.application;
		AppModule appModule = app.getModuleByName(tuple.getSrcModuleName());
		int version = appModule.getVersion();
		version++;
		appModule.setVersion(version);
		tuple.setTupleVersion(version);
		
		if(tuple.getDestModuleName()!=null){
			if(tuple.getDirection() == Tuple.UP)
				sendUp(tuple);
			
		}else{
					////*System.out.println(getName()+"\tError! There is no destination module! for tupel:"+ev.toString());
					///*Log.writeIn///*LogFile(this.getName(), "Error! There is no destination module! for tupel:"+ev.toString());
					System.exit(0);
		}
	}

	protected void updateTimingsOnReceipt(Tuple tuple) {
		////*System.out.println("updateTimingsOnReceipt -> FogDevice.java");
		///*Log.writeIn///*LogFile(this.getName(), "updateTimingsOnReceipt -> FogDevice.java");
		Application app = getApplicationMap().get(tuple.getAppId());
		String srcModule = tuple.getSrcModuleName();
		String destModule = tuple.getDestModuleName().get(0);
		List<AppLoop> loops = app.getLoops();
		for(AppLoop loop : loops){
			if(loop.hasEdge(srcModule, destModule) && loop.isEndModule(destModule)){				
				Double startTime = TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
				if(startTime==null)
					break;
				if(!TimeKeeper.getInstance().getLoopIdToCurrentAverage().containsKey(loop.getLoopId())){
					TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), 0.0);
					TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), 0);
				}
				double currentAverage = TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loop.getLoopId());
				int currentCount = TimeKeeper.getInstance().getLoopIdToCurrentNum().get(loop.getLoopId());
				double delay = CloudSim.clock()- TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
				TimeKeeper.getInstance().getEmitTimes().remove(tuple.getActualTupleId());
				double newAverage = (currentAverage*currentCount + delay)/(currentCount+1);
				TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), newAverage);
				TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), currentCount+1);
				break;
			}
		}

	}

	protected void processSensorJoining(SimEvent ev){
		send(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), FogEvents.SENSOR_JOINED);
	}
	
	protected void executeTuple(SimEvent ev, String operatorId){
		////*System.out.println("executeTuple -> FogDevice.java");
		///*Log.writeIn///*LogFile(this.getName(), "executeTuple -> FogDevice.java");

		//TODO Power funda
		///*Logger.debug(getName(), "Executing tuple on module "+operatorId);
		////*System.out.println("Execute tuple on module "+operatorId);
		Object [] tab = (Object []) ev.getData();
		
		Tuple tuple = (Tuple) tab[0];

		////*System.out.println("Tupel:"+tuple.toString());
		TimeKeeper.getInstance().tupleStartedExecution(tuple);
		//updateAllocatedMips(operatorId);
		processCloudletSubmit(ev, false);
		//updateAllocatedMips(operatorId);
		/*for(Vm vm : getHost().getVmList()){
			///*Logger.error(getName(), "MIPS allocated to "+((AppModule)vm).getName()+" = "+getHost().getTotalAllocatedMipsForVm(vm));
		}*/

	}
	
	protected void processModuleArrival(SimEvent ev){
		AppModule module = (AppModule)ev.getData();
		
		String appId = CloudSim.broker.getAppliaction().getAppId();
		//String appId = module.getAppId();
		
		//System.out.println("Creating module "+module.getName()+" on device "+getName());
		if(!appToModulesMap.containsKey(appId)){
			appToModulesMap.put(appId, new ArrayList<String>());
		}
		
		appToModulesMap.get(appId).add(module.getName());
		
		//processVmCreate(ev, false);
		
		if (module.isBeingInstantiated()) {
			module.setBeingInstantiated(false);
		}
		
		
		//initializePeriodicTuples(module);
		
		
		module.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(module).getVmScheduler().getAllocatedMipsForVm(module));
	}
	
	private void initializePeriodicTuples(AppModule module) {
		////*System.out.println("Sending of perioding tuples from "+getName()+"?");
		///*Log.writeIn///*LogFile(this.getName(), "Sending of perioding tuples from "+getName()+"?");
		
		//String appId = module.getAppId();
		
		//Application app = getApplicationMap().get(appId);
		Application app = CloudSim.broker.getAppliaction();
	
		//if there are a list of periodic tuples
		List<AppEdge> periodicEdges = app.getPeriodicEdges(module.getName());
		for(AppEdge edge : periodicEdges){
			////*System.out.println("Sending of perdiong tuple :"+edge.toString());
			///*Log.writeIn///*LogFile(this.getName(), "Sending of perdiong tuple :"+edge.toString());
			send(getId(), edge.getPeriodicity(), FogEvents.SEND_PERIODIC_TUPLE, edge);
		}
	}

	protected void processOperatorRelease(SimEvent ev){
		this.processVmMigrate(ev, false);
	}
	
	protected void updateNorthTupleQueue(){
//		if(!getNorthTupleQueue().isEmpty()){
//			Tuple tuple = getNorthTupleQueue().poll();
//			sendUpFreeLink(tuple);
//		}else{
//			setNorthLinkBusy(false);
//		}
	}
	
	protected void sendUpFreeLink(Tuple tuple){
		
		// send this tuple to the broker
		//*System.out.println(this.getName()+ ": Send to broker for Storage, tuple:"+tuple.toString());
		///*Log.writeIn///*LogFile(this.getName(), ": Send to broker for Storage, tuple:"+tuple.toString());
		float latency = 0;
		
		if(this.getName().startsWith("HGW")){
			latency = 50;
		}
		send(2, latency, FogEvents.TUPLE_STORAGE, tuple);
		LatencyStats.add_Overall_write_Latency(latency);
		LatencyStats.addWriteLatency(tuple.getTupleType() , latency);
	}

	protected void sendUp(Tuple tuple){
		sendUpFreeLink(tuple);

	}
	
	protected void updateSouthTupleQueue(){
		if(!getSouthTupleQueue().isEmpty()){
			Pair<Tuple, Integer> pair = getSouthTupleQueue().poll(); 
			sendDownFreeLink(pair.getFirst(), pair.getSecond());
		}else{
			setSouthLinkBusy(false);
		}
	}
	
	protected void sendDownFreeLink(Tuple tuple, int childId){
		double networkDelay = tuple.getCloudletFileSize()/getDownlinkBandwidth();
		/////*Logger.debug(getName(), "Sending tuple with tupleType = "+tuple.getTupleType()+" DOWN");
		setSouthLinkBusy(true);
		float latency = getChildToLatencyMap().get(childId);
		send(getId(), networkDelay, FogEvents.UPDATE_SOUTH_TUPLE_QUEUE);
		send(childId, networkDelay+latency, FogEvents.TUPLE_ARRIVAL, tuple);
		NetworkUsageMonitor.sendingTuple(latency, tuple.getCloudletFileSize());
	}
	
	protected void sendDown(Tuple tuple, int childId){
		if(getChildrenIds().contains(childId)){
			if(!isSouthLinkBusy()){
				sendDownFreeLink(tuple, childId);
			}else{
				southTupleQueue.add(new Pair<Tuple, Integer>(tuple, childId));
			}
		}
	}
	
	protected void sendToSelf(Tuple tuple){
		////*System.out.println("Sending the tuple to self for processing tuple:"+tuple.toString());
		///*Log.writeIn///*LogFile(this.getName(), "Sending the tuple to self for processing tuple:"+tuple.toString());
		send(getId(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ARRIVAL, tuple);
		//sendNow(getId(),  FogEvents.TUPLE_ARRIVAL, tuple);
	}
	
	
	
	public void printAllStoredData(){
		//*System.out.println("Print All data stored at "+ this.getName());
		///*Log.writeIn///*LogFile(this.getName(),"Print All data stored at "+ this.getName() );
		if(storedData.keySet() != null){
			//*System.out.println(this.getName()+" Stores:");
			///*Log.writeIn///*LogFile(this.getName()," Stores:");
			for(String tupleTupe : storedData.keySet()){
				if(storedData.get(tupleTupe)==null)
					continue;
				
				Iterator<Tuple> itr = storedData.get(tupleTupe).iterator();
				while(itr.hasNext()){
					Tuple data = (Tuple) itr.next();
					//*System.out.println(data.toString());
					///*Log.writeIn///*LogFile(this.getName(),data.toString());
				}
			}	
		}
	}
	
	public PowerHost getHost(){
		return (PowerHost) getHostList().get(0);
	}
	
	public int getParentId() {
		return parentId;
	}
	
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	
	public List<Integer> getChildrenIds() {
		return childrenIds;
	}
	
	public void setChildrenIds(List<Integer> childrenIds) {
		this.childrenIds = childrenIds;
	}
	
	public float getUplinkBandwidth() {
		return uplinkBandwidth;
	}
	
	public void setUplinkBandwidth(float uplinkBandwidth) {
		this.uplinkBandwidth = uplinkBandwidth;
	}
	
	public float getUplinkLatency() {
		return uplinkLatency;
	}
	
	public void setUplinkLatency(float uplinkLatency) {
		this.uplinkLatency = uplinkLatency;
	}
	
	public boolean isSouthLinkBusy() {
		return isSouthLinkBusy;
	}
	
	public boolean isNorthLinkBusy() {
		return isNorthLinkBusy;
	}
	
	public void setSouthLinkBusy(boolean isSouthLinkBusy) {
		this.isSouthLinkBusy = isSouthLinkBusy;
	}
	
	public void setNorthLinkBusy(boolean isNorthLinkBusy) {
		this.isNorthLinkBusy = isNorthLinkBusy;
	}
	
	public int getControllerId() {
		return controllerId;
	}
	
	public void setControllerId(int controllerId) {
		this.controllerId = controllerId;
	}
	
	public List<String> getActiveApplications() {
		return activeApplications;
	}
	
	public void setActiveApplications(List<String> activeApplications) {
		this.activeApplications = activeApplications;
	}
	
	public Map<Integer, List<String>> getChildToOperatorsMap() {
		return childToOperatorsMap;
	}
	
	public void setChildToOperatorsMap(Map<Integer, List<String>> childToOperatorsMap) {
		this.childToOperatorsMap = childToOperatorsMap;
	}

	public Map<String, Application> getApplicationMap() {
		return applicationMap;
	}

	public void setApplicationMap(Map<String, Application> applicationMap) {
		this.applicationMap = applicationMap;
	}

	public Queue<Tuple> getNorthTupleQueue() {
		return northTupleQueue;
	}

	public void setNorthTupleQueue(Queue<Tuple> northTupleQueue) {
		this.northTupleQueue = northTupleQueue;
	}

	public Queue<Pair<Tuple, Integer>> getSouthTupleQueue() {
		return southTupleQueue;
	}

	public void setSouthTupleQueue(Queue<Pair<Tuple, Integer>> southTupleQueue) {
		this.southTupleQueue = southTupleQueue;
	}

	public double getDownlinkBandwidth() {
		return downlinkBandwidth;
	}

	public void setDownlinkBandwidth(float downlinkBandwidth) {
		this.downlinkBandwidth = downlinkBandwidth;
	}

	public List<Pair<Integer, Double>> getAssociatedActuatorIds() {
		return associatedActuatorIds;
	}

	public void setAssociatedActuatorIds(List<Pair<Integer, Double>> associatedActuatorIds) {
		this.associatedActuatorIds = associatedActuatorIds;
	}
	
	public double getEnergyConsumption() {
		return energyConsumption;
	}

	public void setEnergyConsumption(double energyConsumption) {
		this.energyConsumption = energyConsumption;
	}
	
	public Map<Integer, Float> getChildToLatencyMap() {
		return childToLatencyMap;
	}

	public void setChildToLatencyMap(Map<Integer, Float> childToLatencyMap) {
		this.childToLatencyMap = childToLatencyMap;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double getRatePerMips() {
		return ratePerMips;
	}

	public void setRatePerMips(double ratePerMips) {
		this.ratePerMips = ratePerMips;
	}
	
	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}

	public Map<String, Map<String, Integer>> getModuleInstanceCount() {
		return moduleInstanceCount;
	}

	public void setModuleInstanceCount(
			Map<String, Map<String, Integer>> moduleInstanceCount) {
		this.moduleInstanceCount = moduleInstanceCount;
	}

	public int getLeftId(){
		return this.leftId;
	}
	
	public void setLeftId(int leftId){
		this.leftId=leftId;
	}
	
	public int getRightId(){
		return this.rightId;
	}
	
	public void setRightId(int rightId){
		this.rightId=rightId;
	}
	
	public float getRightLatency(){
		return this.rightLatency;
	}
	
	public void setRightLatency(float rightLatency){
		this.rightLatency=rightLatency;
	}
	
	public float getLeftLatency(){
		return this.leftLatency;
	}
	
	public void setLeftLatency(float leftLatency){
		this.leftLatency=leftLatency;
	}
	
	public void addStoredData(String tupleTupe, StoredDataQueue storedDataQue){
		storedData.put(tupleTupe, storedDataQue);
	}
	
	public Map<String, StoredDataQueue> getStoredData(){
		return storedData;
	}
	
}
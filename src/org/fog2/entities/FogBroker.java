package org.fog2.entities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.StorageMode.FogStorage;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.cplex.DataAllocation;
import org.fog.examples.DataPlacement;
import org.fog.lpFileConstuction.BasisDelayMatrix;
import org.fog.stats.LatencyStats;
import org.fog.stats.Stats;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacement;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;

public class FogBroker extends PowerDatacenterBroker{
	
	public static Application application;
	private DataAllocation dataAllocation;
	
	/*
	 * Map<Pair<Tuple,rep>,ackBoolean>
	 */
	private Map<Pair<Tuple,Integer>,Boolean> storageACKTestingMap = new HashMap<Pair<Tuple,Integer>,Boolean>();

	public FogBroker(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		//*//*System.out.println(getName()+" is starting...");
		
		if(DataPlacement.load_consumption_times){
			initializeDataRetrieveRequests_estimation(application);
		}else{
			initializeDataRetrieveRequests_global(application);
		}
		
				
		if(DataPlacement.sendPerdiodicTuples)
				intializeproduction();
				//intializePeriodicTupleProduction();
				
	}
	
	

	@Override
	public void processEvent(SimEvent ev) {
		// TODO Auto-generated method stub
		switch(ev.getTag()){
		
		case FogEvents.TUPLE_STORAGE:
			processTupleStorage(ev);
			break;
			
		case FogEvents.TUPLE_STORAGE_ACK:
			processTupleStorageACK(ev);
			break;
			
		case FogEvents.DELAY_TUPLE_STORAGE_ACK:
			processDelayTupleStorageACK(ev);
			break;
			
		case FogEvents.BLOCKED_WRITE_STORAGE:
			processBlockedWriteStorage(ev);
			break;
			
		case FogEvents.TUPLE_RETRIEVE:
			processTupleRetrieve(ev);
			break;
			
		case FogEvents.TUPLE_RETRIEVE_ACK:
			processTupleRetrieveACK(ev);
			break;
			
		case FogEvents.NON_TUPLE_RETRIEVE:
			processNONTupleRetrieveACK(ev);
			break;
			
//		case FogEvents.INITIALIZE_PERIODIC_TUPLE_PRODUCTION:
//			intializePeriodicTupleProduction();
//			break;	
			
			
		default:
			System.out.println("Error!!! There is no other event to broker:"+ev.toString());
			System.exit(0);
			break;
		}
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		for(FogDevice fogdev:DataPlacement.fogDevices){
			fogdev.printAllStoredData();
		}
	}
	
	private void initializePeriodicTuples() {
		//*System.out.println("Sending of perioding tuples ");
		///*Log.writeIn///*LogFile(this.getName(), "\tSending of perioding tuples ");
		
		
		//if there are a list of periodic tuples
		List<AppEdge> periodicEdges = application.getPeriodicEdges(application.getEdges().get(0).getSource());
		for(AppEdge edge : periodicEdges){
			////*//*System.out.println("Sending of perdiong tuple :"+edge.toString());
			///*Log.writeIn///*LogFile(this.getName(), "\tSending of perdiong tuple :"+edge.toString());
			int id = application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(edge.getSource())).getId();
			send(id, edge.getPeriodicity(), FogEvents.SEND_PERIODIC_TUPLE, edge);
		}
	}
	
	public void setAppliaction(Application application){
		this.application=application;
	}
	
	public Application getAppliaction(){
		return application;
	}
	
	public void setDataAllocation(DataAllocation dataAllocation){
		this.dataAllocation=dataAllocation;
	}
	
	public DataAllocation getDataAllocation(){
		return this.dataAllocation;
	}
	
	protected void processBlockedWriteStorage(SimEvent ev){
		Tuple tuple = (Tuple) ev.getData();
				
		if(ev.getSource()!=2){
			System.out.println(this.getName()+": Error, this event must be comming from the broker!");
			System.out.println(ev.toString());
			System.exit(0);
		}
		
		//*//*System.out.println();
		//*//*System.out.println("Clock:"+ev.eventTime()+"\t"+this.getName()+ " receive blocked write event from:"+ev.getSource()+" for tuple:"+tuple.toString());
		///*Log.writeIn///*LogFile(this.getName()," receive blocked write event from:"+ev.getSource()+" for tuple:"+tuple.toString());
				
		List<Integer> allReplicas = new ArrayList<Integer>();
		
		//*//*System.out.println("DataAllocation.getEmplacementNodeId("+tuple.getTupleType()+"):"+DataAllocation.getEmplacementNodeId(tuple.getTupleType()).toString());
		
		allReplicas.addAll(dataAllocation.getEmplacementNodeId(tuple.getTupleType()));
		///*Log.writeIn///*LogFile(this.getName(),"All replicas:"+allReplicas.toString());
		//*//*System.out.println(this.getName()+" All replicas:"+allReplicas.toString());
		
		int tupleSourceDevId =  application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(tuple.getSrcModuleName())).getId();
		
		List<Integer> unLockedReplicas = application.getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(), dataAllocation);
		///*Log.writeIn///*LogFile(this.getName(),"UnlockedReplicas:"+unLockedReplicas.toString());
		//*//*System.out.println(this.getName()+" UnlockedReplicas:"+unLockedReplicas.toString());
		
		List<Integer> listOfResponseReplicas = application.getDataConsistencyProtocol().getReplicasListRequestForWrite(tupleSourceDevId, unLockedReplicas);
		///*Log.writeIn///*LogFile(this.getName(),"responseReplicas:"+listOfResponseReplicas.toString());
		//*//*System.out.println(this.getName()+" ResponseReplicas:"+listOfResponseReplicas.toString());

				
		int nb_total_replica = allReplicas.size();
		
		//*//*System.out.println(this.getName()+" total number of replicas for tuple type is:"+nb_total_replica);
		///*Log.writeIn///*LogFile(this.getName(), " total number of replicas for tuple type is:"+nb_total_replica);
		
		
		allReplicas.removeAll(unLockedReplicas);
		List<Integer> lockedReplicas = allReplicas;
		///*Log.writeIn///*LogFile(this.getName(),"lockedReplicas for write:"+lockedReplicas.toString());
		//*//*System.out.println(this.getName()+" lockedReplicas for write:"+lockedReplicas.toString());			
						
		if(listOfResponseReplicas.size()<application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica)){
			/*
			 * Do no thing and add this write in the blocked ones
			 */

			application.getDataConsistencyProtocol().addCurrentBlockedWrite(tuple,ev.eventTime());
			Stats.incrementBlockedWriteForBlockedWrite();
			

		}else{
			
			/*
			 * Lock All response replicas
			 */
			for(int rep : listOfResponseReplicas){
				Pair<String,Integer> pair = new Pair<String,Integer> (tuple.getTupleType(),rep);
				this.application.getDataConsistencyProtocol().setReplicaStateForWrite(pair, true);
			}
			
			List<Integer> unLockedReplicasNew = this.application.getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(), dataAllocation);
			//*//*System.out.println(this.getName()+" unLocked Replicas for write New:"+unLockedReplicasNew.toString());
			///*Log.writeIn///*LogFile(this.getName()," unLocked Replicas for write New:"+unLockedReplicasNew.toString());
			
			/*
			 * Send Storage event to All response replicas
			 */
			float maxLatency = 0;
			for(int rep: listOfResponseReplicas){
				//*//*System.out.println(this.getName()+ " Send tuple:"+tuple.getTupleType()+" to the storage node:"+rep);
				///*Log.writeIn///*LogFile(this.getName(), "Send tuple:"+tuple.getTupleType()+" to the storage node:"+rep);
								
								
				float latency = BasisDelayMatrix.getFatestLink(tupleSourceDevId, rep);
		
				int ex = DataPlacement.Basis_Exchange_Unit;
				long tupleDataSize = tuple.getCloudletFileSize();
				int nb_Unit = (int) (tupleDataSize / ex);
				if(tupleDataSize % ex != 0) nb_Unit++;
											
				send(rep, latency*nb_Unit , FogEvents.TUPLE_STORAGE, tuple);
				if(maxLatency < (latency*nb_Unit)){
					maxLatency = latency*nb_Unit;
				}
			}
			
			/*
			 * add latency 
			 */
			
			LatencyStats.add_Overall_blocked_write_Latency(maxLatency);			
			
			/*
			 * Send Storage event to the eventual update replicas
			 */
			if(unLockedReplicas.removeAll(listOfResponseReplicas)){
				maxLatency =0;
				for(int rep: unLockedReplicas){	
					
					/*
					 * Send storage event for non responds replicas
					 * delay the event for eventually
					 */
					
					Stats.incrementDelayedWrite();
					
					float latency = BasisDelayMatrix.getFatestLink(tupleSourceDevId, rep);
					//latency += Math.random()*DataPlacement.writeDelayRequest;
			
					latency += DataPlacement.writeDelayRequest;
					
					int ex = DataPlacement.Basis_Exchange_Unit;
					long tupleDataSize = tuple.getCloudletFileSize();
					int nb_Unit = (int) (tupleDataSize / ex);
					if(tupleDataSize % ex != 0) nb_Unit++;
										
					//*//*System.out.println(this.getName()+ " postpone write tuple:"+tuple.getTupleType()+" to the non selected storage node:"+rep+" in time event:"+String.valueOf(latency+ev.eventTime()));
					///*Log.writeIn///*LogFile(this.getName(), " postpone write tuple:"+tuple.getTupleType()+" to the non selected storage node:"+rep+" in time event:"+String.valueOf(latency+ev.eventTime()));
					
					send(rep, latency*nb_Unit , FogEvents.DELAY_TUPLE_STORAGE, tuple);
					if(maxLatency < (latency*nb_Unit)){
						maxLatency = latency*nb_Unit;
					}
				}
				LatencyStats.add_Overall_delayed_write_Latency(maxLatency);
			}
			
			
			/*
			 * Add the locked replicas to the Map
			 */
			if(lockedReplicas.size()>0){
				application.getDataConsistencyProtocol().addCurrentLockedReplicas(tuple,lockedReplicas);
				for(int i=0; i<lockedReplicas.size();i++){
					Stats.incrementLockedWrite();
					Stats.incrementDelayedWrite();
				}
			}	
		}
			
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
	}
	
	protected void processTupleStorage(SimEvent ev){
		Tuple tuple = (Tuple) ev.getData();
					
		//*System.out.println();
		//*System.out.println("Clock:"+ev.eventTime()+"\t"+this.getName()+ " receive Storage event from:"+ev.getSource()+" for tuple:"+tuple.toString());
		///*Log.writeIn///*LogFile(this.getName()," receive Storage event from:"+ev.getSource()+" for tuple:"+tuple.toString());
		
		if(Stats.production.get(tuple.getTupleType()) == null){
			List<Float> list = new ArrayList<Float>();
			if(tuple.getTupleType().startsWith("TempHGW")){
				if(DataPlacement.sendPerdiodicTuples){
					list.add((float) CloudSim.clock());
				}else{
					list.add((float) CloudSim.clock() - 50);
				}
				
			}else{
				list.add((float) CloudSim.clock());
			}
			
			
			Stats.production.put(tuple.getTupleType(), list);
		}else{
			if(tuple.getTupleType().startsWith("TempHGW")){
				if(DataPlacement.sendPerdiodicTuples){
					Stats.production.get(tuple.getTupleType()).add((float) CloudSim.clock());
				}else{
					Stats.production.get(tuple.getTupleType()).add((float) CloudSim.clock() -50);
				}
	
			}else{
				Stats.production.get(tuple.getTupleType()).add((float) CloudSim.clock());
			}
		}
		
				
		List<Integer> allReplicas = new ArrayList<Integer>();
		
		//*System.out.println("DataAllocation.getEmplacementNodeId("+tuple.getTupleType()+"):"+dataAllocation.getEmplacementNodeId(tuple.getTupleType()).toString());
		
		allReplicas.addAll(dataAllocation.getEmplacementNodeId(tuple.getTupleType()));
		///*Log.writeIn///*LogFile(this.getName(),"All replicas:"+allReplicas.toString());
		//*System.out.println(this.getName()+" All replicas:"+allReplicas.toString());
		
		int tupleSourceDevId =  application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(tuple.getSrcModuleName())).getId();
		
		List<Integer> unLockedReplicas = this.application.getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(),dataAllocation);
		///*Log.writeIn///*LogFile(this.getName(),"UnlockedReplicas:"+unLockedReplicas.toString());
		//*System.out.println(this.getName()+" UnlockedReplicas:"+unLockedReplicas.toString());
		
		List<Integer> listOfResponseReplicas = this.application.getDataConsistencyProtocol().getReplicasListRequestForWrite(tupleSourceDevId, unLockedReplicas);
		///*Log.writeIn///*LogFile(this.getName(),"responseReplicas:"+listOfResponseReplicas.toString());
		//*System.out.println(this.getName()+" ResponseReplicas:"+listOfResponseReplicas.toString());
		
		int nb_total_replica = allReplicas.size();
				
		//*System.out.println(this.getName()+" total number of replicas for tuple type is:"+nb_total_replica);
		///*Log.writeIn///*LogFile(this.getName(), " total number of replicas for tuple type is:"+nb_total_replica);
		
		for(int i=0;i<nb_total_replica;i++)
			Stats.incrementTotalWrite();
		
		allReplicas.removeAll(unLockedReplicas);
		List<Integer> lockedReplicas = allReplicas;
		///*Log.writeIn///*LogFile(this.getName(),"lockedReplicas for write:"+lockedReplicas.toString());
		//*System.out.println(this.getName()+" lockedReplicas for write:"+lockedReplicas.toString());	
		
		
		
		/*
		 * If there is insufficient free replicas or there are other old blocked write ==> add this write to the blocked write
		 */
						
		if(listOfResponseReplicas.size()<application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica) || 
				application.getDataConsistencyProtocol().checkIfTupleIsInCurrentBlockedWrites(tuple.getTupleType())){
			
//			System.out.println(this.getName()+"application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica)="+application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica));
//			System.out.println(this.getName()+"listOfResponseReplicas.size()="+listOfResponseReplicas.size());
//			System.out.println(this.getName()+"checkIfTupleIsInCurrentBlockedWrites="+application.getDataConsistencyProtocol().checkIfTupleIsInCurrentBlockedWrites(tuple.getTupleType()));
			
			///*Log.writeIn///*LogFile(this.getName(),"application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica)="+application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica));
			///*Log.writeIn///*LogFile(this.getName(),"listOfResponseReplicas.size()="+listOfResponseReplicas.size());
			///*Log.writeIn///*LogFile(this.getName(),"checkIfTupleIsInCurrentBlockedWrites="+application.getDataConsistencyProtocol().checkIfTupleIsInCurrentBlockedWrites(tuple.getTupleType()));
			
			/*
			 * Add this write to blocked write map
			 * Add the time stamp of the blocked write
			 */
			application.getDataConsistencyProtocol().addCurrentBlockedWrite(tuple, ev.eventTime());	
			
			for(int i=0; i<application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica);i++)
				Stats.incrementBlockedWrite();
			
			
//			if(tuple.getTupleType().equals("TempRPOP1")){
//				System.out.println(this.getName()+"application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica)="+application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica));
//				System.out.println(this.getName()+"listOfResponseReplicas.size()="+listOfResponseReplicas.size());
//				System.out.println(this.getName()+"checkIfTupleIsInCurrentBlockedWrites="+application.getDataConsistencyProtocol().checkIfTupleIsInCurrentBlockedWrites(tuple.getTupleType()));
//				
//			
//				application.getDataConsistencyProtocol().printAllCurrentBlockedWrites();
//				application.getDataConsistencyProtocol().printAllCurrentLockedReplicas();
////				Scanner sc = new Scanner(System.in);
////				String str = sc.nextLine();
//			}

		}else{
			
			/*
			 * Lock All response replicas
			 */
			for(int rep : listOfResponseReplicas){
				Pair<String,Integer> pair = new Pair<String,Integer> (tuple.getTupleType(),rep);
				this.application.getDataConsistencyProtocol().setReplicaStateForWrite(pair, true);
			}
			
			List<Integer> unLockedReplicasNew = this.application.getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(), dataAllocation);
			//*System.out.println(this.getName()+" unLocked Replicas for write New:"+unLockedReplicasNew.toString());
			///*Log.writeIn///*LogFile(this.getName()," unLocked Replicas for write New:"+unLockedReplicasNew.toString());
			
			/*
			 * Send Storage event to All response replicas
			 */
			float maxLatency = 0;
			for(int rep: listOfResponseReplicas){
				//*System.out.println(this.getName()+ " Send tuple:"+tuple.getTupleType()+" to the storage node:"+rep);
				///*Log.writeIn///*LogFile(this.getName(), "Send tuple:"+tuple.getTupleType()+" to the storage node:"+rep);
				
				Stats.incrementResponseWrite();
					
				float latency = BasisDelayMatrix.getFatestLink(tupleSourceDevId, rep);
		
				int ex = DataPlacement.Basis_Exchange_Unit;
				long tupleDataSize = tuple.getCloudletFileSize();
				int nb_Unit = (int) (tupleDataSize / ex);
				if(tupleDataSize % ex != 0) nb_Unit++;
				
							
				send(rep, latency*nb_Unit , FogEvents.TUPLE_STORAGE, tuple);
				if(maxLatency < (latency*nb_Unit)){
					maxLatency = latency*nb_Unit;
				}
			}
						
			LatencyStats.add_Overall_write_Latency(maxLatency);
			LatencyStats.addWriteLatency(tuple.getTupleType() , maxLatency);
						
			/*
			 * Send Storage event to the eventual update replicas
			 */
			if(unLockedReplicas.removeAll(listOfResponseReplicas)){
				maxLatency =0;
				for(int rep: unLockedReplicas){	
					
					/*
					 * Send storage event for non responds replicas delay the event for eventually
					 */
					
					Stats.incrementDelayedWrite();
					
					float latency = BasisDelayMatrix.getFatestLink(tupleSourceDevId, rep);
					//latency += Math.random()*DataPlacement.writeDelayRequest;
					
					latency += DataPlacement.writeDelayRequest;
			
					int ex = DataPlacement.Basis_Exchange_Unit;
					long tupleDataSize = tuple.getCloudletFileSize();
					int nb_Unit = (int) (tupleDataSize / ex);
					if(tupleDataSize % ex != 0) nb_Unit++;
										
					//*System.out.println(this.getName()+ " postpone write tuple:"+tuple.getTupleType()+" to the non selected storage node:"+rep+" in time event:"+String.valueOf(latency+ev.eventTime()));
					///*Log.writeIn///*LogFile(this.getName(), " postpone write tuple:"+tuple.getTupleType()+" to the non selected storage node:"+rep+" in time event:"+String.valueOf(latency+ev.eventTime()));
					
					send(rep, latency*nb_Unit , FogEvents.DELAY_TUPLE_STORAGE, tuple);
					if(maxLatency < (latency*nb_Unit)){
						maxLatency = latency*nb_Unit;
					}
				}
				LatencyStats.add_Overall_delayed_write_Latency(maxLatency);
			}
			
			
			/*
			 * Add the locked replicas to the Map
			 */
			if(lockedReplicas.size()>0){
				this.application.getDataConsistencyProtocol().addCurrentLockedReplicas(tuple,lockedReplicas);
				for(int i=0; i<lockedReplicas.size();i++){
					Stats.incrementLockedWrite();
					Stats.incrementDelayedWrite();
				}
			}	
		}
			
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
	}
	
	protected void processTupleStorageACK(SimEvent ev){
		Tuple tuple = (Tuple) ev.getData();
		//*System.out.println();
		//*System.out.println(this.getName()+" from node:"+ev.getSource()+" Storage ACK: "+tuple.toString());
		///*Log.writeIn///*LogFile(this.getName(), " from node:"+ev.getSource()+" Storage ACK: "+tuple.toString());
		
		Stats.incrementDoneWrite();
		
		this.addStorageAck(tuple,ev.getSource());
		
		/*
		 * Set replicas state unlocked for write map
		 */
		this.application.getDataConsistencyProtocol().setReplicaStateForWrite(new Pair<String,Integer>(tuple.getTupleType(),ev.getSource()), false);
		
		/*
		 * Send to this replicas an old locked write event if there is one in currentLockReplica in Consistency Manager
		 * this is just for this replicas
		 */
		Tuple oldTuple = this.application.getDataConsistencyProtocol().getOldWriteFromCurrentLockedReplicas(new Pair<String,Integer>(tuple.getTupleType(),ev.getSource()));
		
		if(oldTuple!=null){
			float latency = BasisDelayMatrix.getFatestLink(application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(tuple.getSrcModuleName())).getId(), ev.getSource());
			//latency += Math.random()*DataPlacement.writeDelayRequest;
			
			latency += DataPlacement.writeDelayRequest;
	
			int ex = DataPlacement.Basis_Exchange_Unit;
			long tupleDataSize = oldTuple.getCloudletFileSize();
			int nb_Unit = (int) (tupleDataSize / ex);
			if(tupleDataSize % ex != 0) nb_Unit++;

			//*//*System.out.println(this.getName()+ " Send old locked tuple write:"+oldTuple.toString()+"\nto the non selected storage node:"+ev.getSource()+" in time event:"+String.valueOf(latency+ev.eventTime()));
			///*Log.writeIn///*LogFile(this.getName(), " Send old locked tuple write:"+oldTuple.toString()+"\nto the non selected storage node:"+ev.getSource()+" in time event:"+String.valueOf(latency+ev.eventTime()));
			
			send(ev.getSource(), latency*nb_Unit , FogEvents.DELAY_TUPLE_STORAGE, oldTuple);
			LatencyStats.add_Overall_delayed_write_Latency(latency*nb_Unit);
		
		}else{
			
			/*
			 * Check if there are not other locked writes in other replicas for this tuple ==> launch the oldest blocked write , Otherwise, do no thing
			 */			
			if(!application.getDataConsistencyProtocol().checkIfThereAreCurrentLockedReplicaForTuple(tuple.getTupleType(), dataAllocation)){
				
				/*
				 * get old blocked write
				 */
				
				if(application.getDataConsistencyProtocol().checkIfTupleIsInCurrentBlockedWrites(tuple.getTupleType())){
					
					/*
					 * Check if there is the minimum of required replicas is free
					 */
					
					List<Integer> allReplicas = new ArrayList<Integer>();
					
					//*System.out.println("DataAllocation.getEmplacementNodeId("+tuple.getTupleType()+"):"+dataAllocation.getEmplacementNodeId(tuple.getTupleType()).toString());
					
					allReplicas.addAll(dataAllocation.getEmplacementNodeId(tuple.getTupleType()));
					///*Log.writeIn///*LogFile(this.getName(),"All replicas:"+allReplicas.toString());
					//*System.out.println(this.getName()+" All replicas:"+allReplicas.toString());
										
					List<Integer> unLockedReplicas = this.application.getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(),dataAllocation);
					///*Log.writeIn///*LogFile(this.getName(),"UnlockedReplicas:"+unLockedReplicas.toString());
					//*System.out.println(this.getName()+" UnlockedReplicas:"+unLockedReplicas.toString());
					
					int nb_total_replica = allReplicas.size();
					
					if(!(unLockedReplicas.size()<application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica))){
						Pair<Tuple,Double> pair = this.application.getDataConsistencyProtocol().getOldWriteFromCurrentBlockedWrite(tuple.getTupleType());
						oldTuple = pair.getFirst();
						sendNow(2, FogEvents.BLOCKED_WRITE_STORAGE, oldTuple);
												
						/*
						 * Add the blocked write time stamp
						 */
						
						double blockedWriteLatency = ev.eventTime()-pair.getSecond();
						LatencyStats.add_Overall_blocked_write_Latency(blockedWriteLatency);
						
						//*System.out.println(this.getName()+ " Send for storage old blocked write:"+oldTuple.toString());
						///*Log.writeIn///*LogFile(this.getName(), " Send for storage old blocked write:"+oldTuple.toString());
					
					}else{
						//*System.out.println(this.getName()+ " the minimum required free replicas is not satisfied for tuple:"+tuple.toString());
						///*Log.writeIn///*LogFile(this.getName(), " the minimum required free replicas is not satisfied for tuple:"+tuple.toString());
					}				
					
				}

			}else {
				//*System.out.println(this.getName()+ " there is a locked write in somme other replica for tuple:"+tuple.toString());
				///*Log.writeIn///*LogFile(this.getName(), " there is a locked write in somme other replica for tuple:"+tuple.toString());
			}
		}
			
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
	}
	
	protected void processDelayTupleStorageACK(SimEvent ev){
		Tuple tuple = (Tuple) ev.getData();
		//*System.out.println();
		//*System.out.println(this.getName()+" from node:"+ev.getSource()+" delay Storage ACK: "+tuple.toString());
		///*Log.writeIn///*LogFile(this.getName(), " from node:"+ev.getSource()+" delay Storage ACK: "+tuple.toString());
		
		Stats.incrementDoneWrite();
		
		this.addStorageAck(tuple, ev.getSource());
		
		if(this.application.getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(), dataAllocation).contains(ev.getSource()) ){
			/*
			 * if the replica is unlocked for write
			 * Send to this replicas an update event if there is one in currentLockReplica in Consistency Manager
			 */
			///*Log.writeIn///*LogFile(this.getName(), " rep:"+ev.getSource()+"\tis unlocked fro write for tuple:"+tuple.getTupleType());
			
			Tuple oldTuple = this.application.getDataConsistencyProtocol().getOldWriteFromCurrentLockedReplicas(new Pair<String,Integer>(tuple.getTupleType(),ev.getSource()));
			
			if(oldTuple!=null){
				float latency = BasisDelayMatrix.getFatestLink(application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(tuple.getSrcModuleName())).getId(), ev.getSource());
				//latency += Math.random()*DataPlacement.writeDelayRequest;
				
				latency += DataPlacement.writeDelayRequest;
		
				int ex = DataPlacement.Basis_Exchange_Unit;
				long tupleDataSize = oldTuple.getCloudletFileSize();
				int nb_Unit = (int) (tupleDataSize / ex);
				if(tupleDataSize % ex != 0) nb_Unit++;
				
				///*Log.writeIn///*LogFile(this.getName(), " there is an old locked write:"+oldTuple.toString());
				//*//*System.out.println(this.getName()+ " Send old locked tuple write:"+oldTuple.toString()+"\nto the non selected storage node:"+ev.getSource()+" in time event:"+String.valueOf(latency+ev.eventTime()));
				///*Log.writeIn///*LogFile(this.getName(), " Send old locked tuple write:"+oldTuple.toString()+"\nto the non selected storage node:"+ev.getSource()+" in time event:"+String.valueOf(latency+ev.eventTime()));
				
				send(ev.getSource(), latency*nb_Unit , FogEvents.DELAY_TUPLE_STORAGE, oldTuple);
				LatencyStats.add_Overall_delayed_write_Latency(latency*nb_Unit);
			
			}else{
				
				///*Log.writeIn///*LogFile(this.getName(), " there is no an old locked write for tuple"+tuple.toString());
				/*
				 * Check if there are not other locked writes in other replicas for this tuple ==> launch the oldest blocked write , Otherwise, do no thing
				 */			
				if(!application.getDataConsistencyProtocol().checkIfThereAreCurrentLockedReplicaForTuple(tuple.getTupleType(), dataAllocation)){
					
					/*
					 * get old blocked write
					 */
					///*Log.writeIn///*LogFile(this.getName(), " searching for old blocked writes for tuple:"+tuple.toString());
					
					if(application.getDataConsistencyProtocol().checkIfTupleIsInCurrentBlockedWrites(tuple.getTupleType())){
						
						/*
						 * Check if there is the minimum of required replicas is free
						 */
						
						List<Integer> allReplicas = new ArrayList<Integer>();
						
						//*//*System.out.println("DataAllocation.getEmplacementNodeId("+tuple.getTupleType()+"):"+DataAllocation.getEmplacementNodeId(tuple.getTupleType()).toString());
						
						allReplicas.addAll(dataAllocation.getEmplacementNodeId(tuple.getTupleType()));
						///*Log.writeIn///*LogFile(this.getName(),"All replicas:"+allReplicas.toString());
						//*//*System.out.println(this.getName()+" All replicas:"+allReplicas.toString());
											
						List<Integer> unLockedReplicas = application.getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(), dataAllocation);
						///*Log.writeIn///*LogFile(this.getName(),"UnlockedReplicas:"+unLockedReplicas.toString());
						//*//*System.out.println(this.getName()+" UnlockedReplicas:"+unLockedReplicas.toString());
						
						int nb_total_replica = allReplicas.size();
						
						if(!(unLockedReplicas.size()<application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica))){
							
							Pair<Tuple,Double> pair = application.getDataConsistencyProtocol().getOldWriteFromCurrentBlockedWrite(tuple.getTupleType());
							oldTuple = pair.getFirst();
							sendNow(2, FogEvents.BLOCKED_WRITE_STORAGE, oldTuple);
													
							/*
							 * Add the blocked write time stamp
							 */
							
							double blockedWriteLatency = ev.eventTime()-pair.getSecond();
							LatencyStats.add_Overall_blocked_write_Latency(blockedWriteLatency);

							//*//*System.out.println(this.getName()+ " Send for storage old blocked write:"+oldTuple.toString());
							///*Log.writeIn///*LogFile(this.getName(), " Send for storage old blocked write:"+oldTuple.toString());
						
						}else{
							//*//*System.out.println(this.getName()+ " the minimum required free replicas is not satisfied for tuple:"+tuple.toString());
							///*Log.writeIn///*LogFile(this.getName(), " the minimum required free replicas is not satisfied for tuple:"+tuple.getTupleType());
						}				
						
					}else{
						//*//*System.out.println(this.getName()+ " there is no blocked write for tuple:"+tuple.getTupleType());
						///*Log.writeIn///*LogFile(this.getName(), " there is no blocked write for tuple:"+tuple.getTupleType());
					}
				
				}else {
					//*//*System.out.println(this.getName()+ " there is a locked write in somme replica for tuple:"+tuple.toString());
					///*Log.writeIn///*LogFile(this.getName(), " there is a locked write in somme replica for tuple:"+tuple.toString());
				}
			}
		}
		
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
	}
	
	protected void processTupleRetrieve(SimEvent ev){
		/*
		 * get data map and chose a replicas set 
		 */
		Object [] tab = (Object []) ev.getData();
		
		/*
		 * Send an event to a datahost of a replica  to send this replica to the consumer
		 * tab [0]: Data Consumer Id
		 * tab [1]: destinator service name
		 * tab [2]: edge
		 * tab [3]: list of response replicas
		 * tab [4]: reserved for the median
		 * tab [5]: requestId
		 */
		
		Stats.incrementTotalRead();
		
		AppEdge edge = (AppEdge) tab[2];
		
		String consumer = (String) tab[1];
		
		Pair<String, String> key = new Pair<String, String>(edge.getTupleType(), consumer);
		
		if(Stats.consumption.get(key) == null){
			List<Float> list = new ArrayList<Float>();
			list.add((float) CloudSim.clock());
			Stats.consumption.put(key, list);
		
		}else{
			Stats.consumption.get(key).add((float) CloudSim.clock());
		}
		
		//*//*System.out.println();
		//**System.out.println("Clock:"+ev.eventTime()+"\t"+this.getName()+": Retrieve: from consumer:"+((String) tab[1])+" for edge:"+edge.toString());
		///*Log.writeIn///*LogFile(this.getName(), ": Retrieve: from consumer:"+((String) tab[1])+" for edge:"+edge.toString());
						
		List<Integer> allReplicas = new ArrayList<Integer>(dataAllocation.getEmplacementNodeId(edge.getTupleType()));
		///*Log.writeIn///*LogFile(this.getName(),"All replicas:"+allReplicas.toString());
		//**System.out.println(this.getName()+" All replicas:"+allReplicas.toString());
		
		List<Integer> unLockedReplicas = this.application.getDataConsistencyProtocol().getUnlockedReplicasForRead(edge.getTupleType(), dataAllocation);
		///*Log.writeIn///*LogFile(this.getName(),"UnlockedReplicas:"+unLockedReplicas.toString());
		//**System.out.println(this.getName()+" UnlockedReplicas:"+unLockedReplicas.toString());
		
		List<Integer> listOfResponseReplicas = this.application.getDataConsistencyProtocol().getReplicasListRequestForRead(ev.getSource(), unLockedReplicas);
		///*Log.writeIn///*LogFile(this.getName(),"responseReplicas:"+listOfResponseReplicas.toString());
		//**System.out.println(this.getName()+" ResponseReplicas:"+listOfResponseReplicas.toString());
		
				
		int nb_total_replica = allReplicas.size();
		
		//**System.out.println(this.getName()+" total number of replicas for tuple type is:"+nb_total_replica);
		///*Log.writeIn///*LogFile(this.getName(), " total number of replicas for tuple type is:"+nb_total_replica);
		
		
		if(listOfResponseReplicas.size()<application.getDataConsistencyProtocol().getNumberOfResponseReadReplica(nb_total_replica)){
			//*System.out.println("Number of replicas violation!!!");
			for(int i=0; i< (application.getDataConsistencyProtocol().getNumberOfResponseReadReplica(nb_total_replica)-listOfResponseReplicas.size());i++)
				Stats.incrementReplicaViolationInRead();
			
			Stats.incrementNonServedRead();
			
			//System.exit(0);
		
		}else{
			/*
			 * Lock All response replicas
			 */
			tab[0] = ev.getSource();
			tab[3] = listOfResponseReplicas;
						
			for(int rep : listOfResponseReplicas){
				Pair<String,Integer> pair = new Pair<String,Integer> (edge.getTupleType(),rep);
				int nb_current_reader = this.application.getDataConsistencyProtocol().getReplicaStateForRead(pair);
				nb_current_reader++;
				this.application.getDataConsistencyProtocol().setReplicaStateForRead(pair, nb_current_reader);
			}
			
			unLockedReplicas = this.application.getDataConsistencyProtocol().getUnlockedReplicasForRead(edge.getTupleType(), dataAllocation);
			///*Log.writeIn///*LogFile(this.getName(),"Unlocked Replicas for read New:"+unLockedReplicas.toString());
			//*System.out.println(this.getName()+" Unlocked Replicas for read New:"+unLockedReplicas.toString());
			
						
			if(listOfResponseReplicas.size()>2){
				/*
				 * Chose a median replicas across list of response replicas
				 */
				int median = this.application.getDataConsistencyProtocol().selectMedianReplicas(listOfResponseReplicas);
				//*//*System.out.println(this.getName()+ " the choosen median is:"+median+" for all response replicas:"+listOfResponseReplicas.toString());
				///*Log.writeIn///*LogFile(this.getName(), " the choosen median is:"+median+" for all response replicas:"+listOfResponseReplicas.toString());
				
				/*
				 * Send event VERSION_EXCHANGE to all list of Response Replicas
				 */
				float latency; 
				
				if(application.getFogDeviceById(ev.getSource()).getName().startsWith("HGW")){
					latency = BasisDelayMatrix.getFatestLink(application.getFogDeviceById(ev.getSource()).getParentId(), median);
					
				}else{
					latency = BasisDelayMatrix.getFatestLink(ev.getSource(), median);
					
				}
				
				send(median , latency, FogEvents.VERSION_EXCHANGE, tab);
				LatencyStats.add_Overall_read_Latency(latency);
				
				LatencyStats.addReadLatency(edge.getTupleType() , latency);
				
			}else if(listOfResponseReplicas.size()==2){
				/*
				 * Send event VERSION_EXCHANGE to all list of Response Replicas
				 */
				//**System.out.println(this.getName()+ " There are 2 response replicas, there is no median replicas choosing for response replicas:"+listOfResponseReplicas.toString());
				///*Log.writeIn///*LogFile(this.getName(), " There are less to 2 response replicas, there is no median replicas choosing for response replicas:"+listOfResponseReplicas.toString());
				
				float latency1;
				
				if(application.getFogDeviceById(ev.getSource()).getName().startsWith("HGW")){
					latency1 = BasisDelayMatrix.getFatestLink(application.getFogDeviceById(ev.getSource()).getParentId(), listOfResponseReplicas.get(0));
					
				}else{
					latency1 = BasisDelayMatrix.getFatestLink(ev.getSource(), listOfResponseReplicas.get(0));
					
				}
								
				float latency2; 
				
				if(application.getFogDeviceById(ev.getSource()).getName().startsWith("HGW")){
					latency2 = BasisDelayMatrix.getFatestLink(application.getFogDeviceById(ev.getSource()).getParentId(), listOfResponseReplicas.get(1));
					
				}else{
					latency2 = BasisDelayMatrix.getFatestLink(ev.getSource(), listOfResponseReplicas.get(1));
					
				}
				
				if(latency1<=latency2){  
					send(listOfResponseReplicas.get(0) , latency1,FogEvents.VERSION_EXCHANGE, tab);
					LatencyStats.add_Overall_read_Latency(latency1);
					LatencyStats.addReadLatency(edge.getTupleType() , latency1);
				
				}else{
					send(listOfResponseReplicas.get(1),latency2 , FogEvents.VERSION_EXCHANGE, tab);
					LatencyStats.add_Overall_read_Latency(latency2);
					LatencyStats.addReadLatency(edge.getTupleType() , latency2);
					
				}
			
			}else{
				//**System.out.println(this.getName()+ " There is just 1 response replicas, there is no median replicas choosing for response replicas:"+listOfResponseReplicas.toString());
				///*Log.writeIn///*LogFile(this.getName(), " There are less to 1 response replicas, there is no median replicas choosing for response replicas:"+listOfResponseReplicas.toString());
				float latency; 	
				
				if(application.getFogDeviceById(ev.getSource()).getName().startsWith("HGW")){
					latency = BasisDelayMatrix.getFatestLink(application.getFogDeviceById(ev.getSource()).getParentId(), listOfResponseReplicas.get(0));
					
				}else{
					latency = BasisDelayMatrix.getFatestLink(ev.getSource(), listOfResponseReplicas.get(0));
					
				}
				
				send(listOfResponseReplicas.get(0) , latency,FogEvents.SEND_DATA_TO_CONSUMER, tab);
				LatencyStats.add_Overall_read_Latency(latency);
				LatencyStats.addReadLatency(edge.getTupleType() , latency);
			}
		
		}
		
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
		
	}
	
//	private void checkNearestReplicasForRead(int devId, List<>){
//		
//	}
	
	
	
	protected void processNONTupleRetrieveACK(SimEvent ev){
		Object [] tab = (Object[]) ev.getData();
		/*
		 * tab [0]: DataCons Id
		 * tab [1]: destinater service name
		 * tab [2]: edge
		 * tab [3]: list of response replicas
		 * tab [4]: reserved to last version
		 * tab [5]: requestId
		 */
		AppEdge edge = (AppEdge) tab[2];
		String tupleType = edge.getTupleType();
		int storageNode = ev.getSource();
		String consumerModule = (String) tab[1];
		//**System.out.println();
		//**System.out.println(this.getName()+" Non Retrieve ACK: "+tupleType+" consumerModule:"+consumerModule+" replicas:"+storageNode);
		///*Log.writeIn///*LogFile(this.getName(), "Non Retrieve ACK: "+tupleType+" consumerModule:"+consumerModule+" replicas:"+storageNode);
	
		
		/*
		 * must do this for all response replicas
		 * unlock response replicas
		 * for all unlocked replicas, search the old locked write 
		 */
		List<Integer> listResponseReplicas = (List<Integer>) tab[3];
		
		if(listResponseReplicas!= null){
			
			float maxlatency = 0;
			for(Integer rep: listResponseReplicas){
				/*
				 * Unlock all response replicas
				 */
				int nb_current_reader = application.getDataConsistencyProtocol().getReplicaStateForRead(new Pair<String,Integer>(tupleType, rep));
				nb_current_reader--;
				application.getDataConsistencyProtocol().setReplicaStateForRead(new Pair<String,Integer>(tupleType, rep),nb_current_reader);
				
				if(application.getDataConsistencyProtocol().getUnlockedReplicasForWrite(tupleType, dataAllocation).contains(rep)){
					/*
					 * check if there are old blocked writes, and send delay writes for them
					 */
					Tuple oldTuple = application.getDataConsistencyProtocol().getOldWriteFromCurrentLockedReplicas(new Pair<String,Integer>(tupleType,rep));
					
					if(oldTuple!=null){
						float latency = BasisDelayMatrix.getFatestLink(application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(edge.getSource())).getId(), rep);
						//latency += Math.random()*DataPlacement.writeDelayRequest;
						
						latency += DataPlacement.writeDelayRequest;
						int ex = DataPlacement.Basis_Exchange_Unit;
						long tupleDataSize = oldTuple.getCloudletFileSize();
						int nb_Unit = (int) (tupleDataSize / ex);
						if(tupleDataSize % ex != 0) nb_Unit++;
						
						
						//**System.out.println(this.getName()+ " Send old tuple write:"+oldTuple.toString()+" triggred by a non retrieve event  \nto the non selected storage node:"+rep+" in time event:"+String.valueOf(latency+ev.eventTime()));
						///*Log.writeIn///*LogFile(this.getName(), " Send old tuple write:"+oldTuple.toString()+" triggred by a non retrieve event  \nto the non selected storage node:"+rep+" in time event:"+String.valueOf(latency+ev.eventTime()));
						
						send(rep, latency*nb_Unit , FogEvents.DELAY_TUPLE_STORAGE, oldTuple);
						if(maxlatency< (latency*nb_Unit))
							maxlatency = latency*nb_Unit;

					}
					
				}else{
					//**System.out.println(this.getName()+ " there are other reader from this replicas! non delayed tuple storage are checked in replica:"+rep);
					///*Log.writeIn///*LogFile(this.getName(), " there are other reader from this replicas! non delayed tuple storage are checked in replica:"+rep);
					
				}
			}
			
			LatencyStats.add_Overall_delayed_write_Latency(maxlatency);
					
			/*
			 * Check if there are not other locked writes in other replicas for this tuple ==> launch the oldest blocked write , Otherwise, do no thing
			 */			
			if(!application.getDataConsistencyProtocol().checkIfThereAreCurrentLockedReplicaForTuple(tupleType, dataAllocation)){
				
				/*
				 * get old blocked write
				 */
				
				if(application.getDataConsistencyProtocol().checkIfTupleIsInCurrentBlockedWrites(tupleType)){
					
					/*
					 * Check if there is the minimum of required replicas is free
					 */
					
					List<Integer> allReplicas = new ArrayList<Integer>();
					
					//System.out.println("DataAllocation.getEmplacementNodeId("+tupleType+"):"+DataAllocation.getEmplacementNodeId(tupleType).toString());
					
					allReplicas.addAll(dataAllocation.getEmplacementNodeId(tupleType));
					///*Log.writeIn///*LogFile(this.getName(),"All replicas:"+allReplicas.toString());
					//*System.out.println(this.getName()+" All replicas:"+allReplicas.toString());
										
					List<Integer> unLockedReplicas = this.application.getDataConsistencyProtocol().getUnlockedReplicasForWrite(tupleType, dataAllocation);
					///*Log.writeIn///*LogFile(this.getName(),"UnlockedReplicas:"+unLockedReplicas.toString());
					//**System.out.println(this.getName()+" UnlockedReplicas:"+unLockedReplicas.toString());
					
					int nb_total_replica = allReplicas.size();
					
					if(!(unLockedReplicas.size()<application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica))){
						
						Pair<Tuple,Double> pair = this.application.getDataConsistencyProtocol().getOldWriteFromCurrentBlockedWrite(tupleType);
						Tuple oldTuple = pair.getFirst();
						sendNow(2, FogEvents.BLOCKED_WRITE_STORAGE, oldTuple);
												
						/*
						 * Add the blocked write time stamp
						 */
						
						double blockedWriteLatency = ev.eventTime()-pair.getSecond();
						LatencyStats.add_Overall_blocked_write_Latency(blockedWriteLatency);
												
						//**System.out.println(this.getName()+ " Send for storage old blocked write:"+oldTuple.toString());
						///*Log.writeIn///*LogFile(this.getName(), " Send for storage old blocked write:"+oldTuple.toString());
					
					}else{
						//**System.out.println(this.getName()+ " the minimum required free replicas is not satisfied for tuple:"+tupleType);
						///*Log.writeIn///*LogFile(this.getName(), " the minimum required free replicas is not satisfied for tuple:"+tupleType);
					}				
					
				}else{
					//**System.out.println(this.getName()+ " there is no blocked write for tuple:"+tupleType);
					///*Log.writeIn///*LogFile(this.getName(), " there is no blocked write for tuple:"+tupleType);
				}
				
			}else {
				//**System.out.println(this.getName()+ " there is a locked write in somme replica for tuple:"+tupleType);
				///*Log.writeIn///*LogFile(this.getName(), " there is a locked write in somme replica for tuple:"+tupleType);
			}
		}
		
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
	}
	
	protected void processTupleRetrieveACK(SimEvent ev){
		Object [] tab = (Object[]) ev.getData();
		//**System.out.println();
		/*
		 * tab [0]: DataCons Id <--- tuple
		 * tab [1]: destinater service name
		 * tab [2]: edge
		 * tab [3]: list of response replicas
		 * tab [4]: reserved to last version
		 * tab [5]: requestId
		 */
		
		
		Tuple tuple = (Tuple) tab[0];
		List<Integer> listResponseReplicas = (List<Integer>) tab[3];
		
		int sourceNodeId = ev.getSource();
		
		String tupleType = tuple.getTupleType();
				
		if(listResponseReplicas!= null){
			
			/*
			 * must do this for all response replicas
			 */
			float maxlatency = 0;
			
			for(Integer rep: listResponseReplicas){
	
				//**System.out.println(this.getName()+" Retrieve ACK: "+tuple.toString()+" consuemer:"+sourceNodeId+" replicas:"+rep);
				///*Log.writeIn///*LogFile(this.getName(), "Retrieve ACK: "+tuple.toString()+" consuemer:"+sourceNodeId+" replicas:"+rep);
				
				/*
				 * Unlock all response replicas
				 */			
				int nb_current_reader = application.getDataConsistencyProtocol().getReplicaStateForRead(new Pair<String,Integer>(tuple.getTupleType(), rep));
				nb_current_reader--;
				application.getDataConsistencyProtocol().setReplicaStateForRead(new Pair<String,Integer>(tuple.getTupleType(), rep),nb_current_reader);
				
				if(application.getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(), dataAllocation).contains(rep)){
					/*
					 * check if there are old locked writes, and send delay writes for them
					 */
					Tuple oldTuple = application.getDataConsistencyProtocol().getOldWriteFromCurrentLockedReplicas(new Pair<String,Integer>(tuple.getTupleType(),rep));
					
					if(oldTuple!=null){
						float latency = BasisDelayMatrix.getFatestLink(application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(tuple.getSrcModuleName())).getId(), rep);
						//latency += Math.random()*DataPlacement.writeDelayRequest;
						
						latency += DataPlacement.writeDelayRequest;
				
						int ex = DataPlacement.Basis_Exchange_Unit;
						long tupleDataSize = oldTuple.getCloudletFileSize();
						int nb_Unit = (int) (tupleDataSize / ex);
						if(tupleDataSize % ex != 0) nb_Unit++;
						
						
						//**System.out.println(this.getName()+ " Send old tuple write:"+oldTuple.toString()+"\nto the non selected storage node:"+rep+" in time event:"+String.valueOf(latency+ev.eventTime()));
						///*Log.writeIn///*LogFile(this.getName(), " Send old tuple write:"+oldTuple.toString()+"\nto the non selected storage node:"+rep+" in time event:"+String.valueOf(latency+ev.eventTime()));
						
						send(rep, latency*nb_Unit , FogEvents.DELAY_TUPLE_STORAGE, oldTuple);
						
						if(maxlatency< (latency*nb_Unit))
							maxlatency = latency*nb_Unit;
	
					}
					
				}else{
					//**System.out.println(this.getName()+ " there are other reader from this replicas! non delayed tuple storage are checked in replica:"+rep);
					///*Log.writeIn///*LogFile(this.getName(), " there are other reader from this replicas! non delayed tuple storage are checked in replica:"+rep);
					
				}
			}
			
			LatencyStats.add_Overall_delayed_write_Latency(maxlatency);
			
			/*
			 * Check if there are not other locked writes in other replicas for this tuple ==> launch the oldest blocked write , Otherwise, do no thing
			 */			
			if(!application.getDataConsistencyProtocol().checkIfThereAreCurrentLockedReplicaForTuple(tupleType, dataAllocation)){
				
				/*
				 * get old blocked write
				 */
				
				if(application.getDataConsistencyProtocol().checkIfTupleIsInCurrentBlockedWrites(tuple.getTupleType())){
					
					/*
					 * Check if there is the minimum of required replicas is free
					 */
					
					List<Integer> allReplicas = new ArrayList<Integer>();
					
					//*//*System.out.println("DataAllocation.getEmplacementNodeId("+tuple.getTupleType()+"):"+DataAllocation.getEmplacementNodeId(tuple.getTupleType()).toString());
					
					allReplicas.addAll(dataAllocation.getEmplacementNodeId(tuple.getTupleType()));
					///*Log.writeIn///*LogFile(this.getName(),"All replicas:"+allReplicas.toString());
					//**System.out.println(this.getName()+" All replicas:"+allReplicas.toString());
										
					List<Integer> unLockedReplicas = this.application.getDataConsistencyProtocol().getUnlockedReplicasForWrite(tuple.getTupleType(), dataAllocation);
					///*Log.writeIn///*LogFile(this.getName(),"UnlockedReplicas:"+unLockedReplicas.toString());
					//**System.out.println(this.getName()+" UnlockedReplicas:"+unLockedReplicas.toString());
					
					int nb_total_replica = allReplicas.size();
					
					if(!(unLockedReplicas.size()<application.getDataConsistencyProtocol().getNumberOfResponseWriteReplica(nb_total_replica))){
						
						Pair<Tuple,Double> pair = this.application.getDataConsistencyProtocol().getOldWriteFromCurrentBlockedWrite(tupleType);
						Tuple oldTuple = pair.getFirst();
						sendNow(2, FogEvents.BLOCKED_WRITE_STORAGE, oldTuple);
												
						/*
						 * Add the blocked write time stamp
						 */
						
						double blockedWriteLatency = ev.eventTime()-pair.getSecond();
						LatencyStats.add_Overall_blocked_write_Latency(blockedWriteLatency);
						
						//**System.out.println(this.getName()+ " Send for storage old blocked write:"+oldTuple.toString());
						///*Log.writeIn///*LogFile(this.getName(), " Send for storage old blocked write:"+oldTuple.toString());
					
					}else{
						//**System.out.println(this.getName()+ " the minimum required free replicas is not satisfied for tuple:"+tuple.toString());
						///*Log.writeIn///*LogFile(this.getName(), " the minimum required free replicas is not satisfied for tuple:"+tuple.getTupleType());
					}				
					
				}else{
					//**System.out.println(this.getName()+ " there is no blocked write for tuple:"+tuple.getTupleType());
					///*Log.writeIn///*LogFile(this.getName(), " there is no blocked write for tuple:"+tuple.getTupleType());
				}
							
			}else {
				//**System.out.println(this.getName()+ " there is a locked write in somme replica for tuple:"+tupleType);
				///*Log.writeIn///*LogFile(this.getName(), " there is a locked write in somme replica for tuple:"+tupleType);
			}
		
		}
		
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
	}
	
	public void initializeDataRetrieveRequests_estimation(Application application){
		//*//*System.out.println("Initialize consumption requests!");
		for(AppEdge edge : application.getEdges()){
			if(edge.getSource().startsWith("s-"))
				continue;
			if(edge.getDestination().get(0).startsWith("DISPLAY"))
				continue;
			
			for(String moduleName : edge.getDestination()){
				String fogDevName = ModuleMapping.getDeviceHostModule(moduleName);
				FogDevice fogdev = application.getFogDeviceByName(fogDevName);
				/*
				 * tab [0]: DataProd Id
				 * tab [1]: destinater service name
				 * tab [2]: edge
				 * tab [3]: list of response replicas
				 * tab [4]: is reserved for the median replicas
				 * tab [5]: requestId
				 */
				Object [] tab = new Object[6];
				tab[0] = application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(edge.getSource())).getId();
				tab[1] = moduleName;
				tab[2] = edge;
				tab[3] = null;
				tab[4] = -1;
				tab[5] = -1;
				
				List<Double> consumptionTimes = new ArrayList<Double>();
				consumptionTimes = Stats.loadConsumption(edge.getTupleType(), moduleName);
				
				for(Double delay : consumptionTimes){
					///*Log.writeIn///*LogFile(this.getName(),"Send Initialize consumption for:"+moduleName+"\tnode:"+fogdev.getName());
					send(fogdev.getId(), delay,FogEvents.INITIALIZE_CONSUMPTION, tab);
				}

			}
		}
	}
	
	
	public void initializeDataRetrieveRequests_global(Application application){
		//*//*System.out.println("Initialize consumption requests!");
		for(AppEdge edge : application.getEdges()){
			if(edge.getSource().startsWith("s-"))
				continue;
			if(edge.getDestination().get(0).startsWith("DISPLAY"))
				continue;
			
			for(String moduleName : edge.getDestination()){
				String fogDevName = ModuleMapping.getDeviceHostModule(moduleName);
				FogDevice fogdev = application.getFogDeviceByName(fogDevName);
				/*
				 * tab [0]: DataProd Id
				 * tab [1]: destinater service name
				 * tab [2]: edge
				 * tab [3]: list of response replicas
				 * tab [4]: is reserved for the median replicas
				 * tab [5]: requestId
				 */
				Object [] tab = new Object[6];
				tab[0] = application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(edge.getSource())).getId();
				tab[1] = moduleName;
				tab[2] = edge;
				tab[3] = null;
				tab[4] = -1;
				tab[5] = -1;
				
				sendNow(fogdev.getId(), FogEvents.INITIALIZE_CONSUMPTION, tab);
				
			}
		}
	}
	
	
	public void intializeproduction(){
		
		
		AppEdge edge = application.getEdges().get(0);
		
		String tupleType = edge.getTupleType();
		String serviceName = FogStorage.application.getEdgeMap().get(tupleType).getSource();
		FogDevice fogdev =   FogStorage.application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(serviceName));
		List<Double> timeList = Stats.loadProduction(tupleType);
		
		Object [] tab = new  Object[3];
		tab[0] = edge;
		tab[1] = timeList;
		
		
		//**System.out.println(this.getName()+"\tSend initialize periodic tuple production to "+fogdev.getName()+" for tuple "+tupleType);
		sendNow(fogdev.getId(), FogEvents.INITIALIZE_PERIODIC_TUPLE_PRODUCTION, tab);
	}
	
	
	
//	public void intializePeriodicTupleProduction() {
//		
//		AppEdge edge = application.getEdges().get(0);
//		
//		String tupleType = edge.getTupleType();
//		String serviceName = FogStorage.application.getEdgeMap().get(tupleType).getSource();
//		FogDevice fogdev =   FogStorage.application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(serviceName));
//		
//		AppModule appModule = application.getModuleByName(edge.getSource());
//
//		float latency = 0;
//		
//		if(fogdev.getName().startsWith("HGW")){
//			latency = 50;
//		}
//
//		List<Double> timeList = Stats.loadProduction(tupleType);
//
//		for(Double time: timeList){
//			int version = appModule.getVersion();
//			version++;
//			
//			Tuple tuple = FogBroker.application.createTuple(edge, this.getId());
//			
//			appModule.setVersion(version);
//			tuple.setTupleVersion(version);
//			
//			LatencyStats.add_Overall_write_Latency(latency);
//			LatencyStats.addWriteLatency(tupleType , latency);
//			
//			///*Log.writeIn///*LogFile(this.getName(), "Send tuple storage time:"+time+"\ttuple:"+tuple.toString());
//			send(this.getId(), time, FogEvents.TUPLE_STORAGE, tuple);
//		}
//						
//	}
	
//	public void intializePeriodicTupleProduction() {
//		
//		AppEdge edge = application.getEdges().get(0);
//		
//		String tupleType = edge.getTupleType();
//		String serviceName = FogStorage.application.getEdgeMap().get(tupleType).getSource();
//		FogDevice fogdev =   FogStorage.application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(serviceName));
//		
//		AppModule appModule = application.getModuleByName(edge.getSource());
//
//		float latency = 0;
//		
//		if(fogdev.getName().startsWith("HGW")){
//			latency = 50;
//		}
//
//		List<Double> timeList = Stats.loadProduction(tupleType);
//
//		for(Double time: timeList){
//			int version = appModule.getVersion();
//			version++;
//			
//			Tuple tuple = FogBroker.application.createTuple(edge, this.getId());
//			
//			appModule.setVersion(version);
//			tuple.setTupleVersion(version);
//			
//			LatencyStats.add_Overall_write_Latency(latency);
//			LatencyStats.addWriteLatency(tupleType , latency);
//			
//			///*Log.writeIn///*LogFile(this.getName(), "Send tuple storage time:"+time+"\ttuple:"+tuple.toString());
//			send(this.getId(), time, FogEvents.TUPLE_STORAGE, tuple);
//		}
//						
//	}
	
	private void addStorageAck(Tuple tuple, int rep){
		Pair<Tuple,Integer> pair = new Pair<Tuple,Integer>(tuple,rep);
		
		if(this.storageACKTestingMap.containsKey(pair)){
			System.out.println("Error, storing tuple more than ones!");
			System.exit(0);
		}
		
		this.storageACKTestingMap.put(pair, true);
		
	}
	
		
}

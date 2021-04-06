package org.fog.dataConsistency;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.StorageMode.FogStorage;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.fog.application.AppEdge;
import org.fog.cplex.DataAllocation;
import org.fog.examples.DataPlacement;
import org.fog.stats.LatencyStats;
import org.fog.utils.Config;
import org.fog2.entities.Tuple;

public abstract class DataConsistency {
	
	/*
	 * this map is used to lock and unlock replicas
	 * Locked true, unlocked false
	 */
	protected Map<Pair<String, Integer>, Boolean> lockReplicaMapForWrite;
	
	/*
	 * this map is used to lock and unlock replicas
	 * Map value specify the number of reader from this replica
	 * locked ==> value > 0
	 * unlocked ==> value = 0;
	 */
	protected Map<Pair<String, Integer>, Integer> lockReplicaMapForRead;
	
	/*
	 * This map is used to save current locked replicas when a write arrived, 
	 * these replicas will be updated once unlocked using eventually progressing
	 * Map<Pair<TupleType,Replicas>,List<Tuple>>
	 */
	public static Map<Pair<String,Integer>,List<Tuple>> currentLockedReplicas = new HashMap<Pair<String,Integer>,List<Tuple>>();

	
	
	/*
	 * This map is used to save current blocked writes <when violation of number of replicas>
	 * These writes will be done once the required number of replicas is satisfied>
	 */
	public static Map<String,List<Pair<Tuple,Double>>> currentBlockedWrites = new HashMap<String,List<Pair<Tuple,Double>>>();
	
	
//	/*
//	 * This map is used to save the current delayed writes, in same way to lock replicas for another delayed writes
//	 *
//	 */
//	private static Map<Pair<String,Integer>,Boolean> currentDelayedWriteMap = new HashMap<Pair<String,Integer>,Boolean>();
//	
//	public void  addCurrentDelayedWrite(String tupleType, int rep){
//		Log.writeInLogFile("Data Consistency Manager", " add delayed write :"+tupleType+" for rep:"+rep);
//		//*System.out.println("Data Consistency Manager"+ " add delayed write :"+tupleType+" for rep:"+rep);
//		
//		Pair<String,Integer> pair = new Pair<String,Integer>(tupleType,rep);
//		currentDelayedWriteMap.put(pair, true);
//		
//	}
//	
//	public void removeCurrentDelayedWrite(String tupleType, int rep){
//		Log.writeInLogFile("Data Consistency Manager", " remove delayed write :"+tupleType+" for rep:"+rep);
//		//*System.out.println("Data Consistency Manager"+ " remove delayed write :"+tupleType+" for rep:"+rep);
//		
//		Pair<String,Integer> pair = new Pair<String,Integer>(tupleType,rep);
//		currentDelayedWriteMap.put(pair, false);
//		
//	}
//	
//	public boolean  getDelayedWriteStatus(String tupleType, int rep){
//		Log.writeInLogFile("Data Consistency Manager", " get status for delayed write :"+tupleType+" for rep:"+rep);
//		//*System.out.println("Data Consistency Manager"+ " get status for delayed write :"+tupleType+" for rep:"+rep);
//		Pair<String,Integer> pair = new Pair<String,Integer>(tupleType,rep);
//		
//		if (currentDelayedWriteMap.containsKey(pair)) {
//			return currentDelayedWriteMap.get(pair);
//		}
//		
//		return false;
//		
//	}
	
	public void addCurrentBlockedWrite(Tuple tuple, Double writeTime){
		
		Log.writeInLogFile("Data Consistency Manager", " add write "+tuple.getTupleType()+" with version"+tuple.getTupleVersion()+" to current blocked write");
		//*System.out.println("Data Consistency Manager"+ " add write "+tuple.getTupleType()+" with version"+tuple.getTupleVersion()+" to current blocked write");
		
		List<Pair<Tuple,Double>> pairs;
		
		if(currentBlockedWrites.containsKey(tuple.getTupleType())){
			pairs = currentBlockedWrites.get(tuple.getTupleType());
			
		}else{
			pairs = new ArrayList<Pair<Tuple,Double>>();
			
		}
		
		Pair<Tuple,Double> pair = new Pair<Tuple,Double>(tuple,writeTime);
		
		pairs.add(pair);
		currentBlockedWrites.put(tuple.getTupleType(), pairs);
		saveAllCurrentBlockedWrites(tuple, writeTime);
		

	}
	
	public boolean checkIfTupleIsInCurrentBlockedWrites(String tupleType){
		//*System.out.println("Data Consistency Manager:"+" checking for current Blocked writes for tuple:"+tupleType);
		Log.writeInLogFile("Data Consistency Manager:"," checking for current Blocked writes for tuple:"+tupleType);
		
		if(!currentBlockedWrites.containsKey(tupleType)){
			//*System.out.println("Data Consistency Manager:"+" there is no current Blocked writes for tuple:"+tupleType);
			Log.writeInLogFile("Data Consistency Manager:"," there is no current Blocked writes for tuple:"+tupleType);
			return false;
			
		}else{
			if(!(currentBlockedWrites.get(tupleType).size()>0)){
				//*System.out.println("Data Consistency Manager:"+" there is no current Blocked writes for tuple:"+tupleType);
				Log.writeInLogFile("Data Consistency Manager:"," there is no current Blocked writes for tuple:"+tupleType);
				return false;
			}
		}

		return true;
	}
	
	public Pair<Tuple,Double> getOldWriteFromCurrentBlockedWrite(String tupleType){
		
		//*System.out.println("Data Consistency Manager:"+" checking for current blocked writes for :"+tupleType);
		Log.writeInLogFile("Data Consistency Manager:"," checking for current blocked writes for :"+tupleType);
		
		if(currentBlockedWrites.containsKey(tupleType)){
			
			if(currentBlockedWrites.get(tupleType)!=null && !currentBlockedWrites.get(tupleType).isEmpty()){
				
				Iterator<Pair<Tuple,Double>> itr = currentBlockedWrites.get(tupleType).iterator(); 
				Pair<Tuple,Double> old = (Pair<Tuple,Double>) itr.next();
				Pair<Tuple,Double> pair;
				
				while(itr.hasNext()){
					pair  = (Pair<Tuple,Double>) itr.next();
					if(pair.getFirst().getTupleVersion() < old.getFirst().getTupleVersion()){
						old = pair;
					}
				}
				
				//*System.out.println("Data Consistency Manager:"+old.toString()+"  is found!");
				Log.writeInLogFile("Data Consistency Manager:",old.toString()+" is found!");
				currentBlockedWrites.get(tupleType).remove(old);
				
				if (currentBlockedWrites.get(tupleType).contains(old)){
					//*System.out.println("Error, in get old write from current blocked write!");
					System.exit(0);
				}
				
				return old;
			}
		}
		//*System.out.println("Data Consistency Manager:"+" non blocked writes is found!");
		Log.writeInLogFile("Data Consistency Manager:"," non blocked writes is found!");
		return null;
	}
		
	public void printAllCurrentBlockedWrites(){
		// TODO Auto-generated method stub
		Log.writeInLogFile("Data Consistency Manager", " Print all current blocked writes:");
		System.out.println("Data Consistency Manager"+ " Print all current blocked writes:");
		
		for(String key: currentBlockedWrites.keySet()){
			Log.writeInLogFile("Data Consistency Manager", " current blocked writes for :"+key);
			System.out.println("Data Consistency Manager"+ " current blocked writes for :"+key);
			for(Pair<Tuple,Double> pair: currentBlockedWrites.get(key)){
				Log.writeInLogFile("Data Consistency Manager\t", pair.getFirst().toString());
				Log.writeInLogFile("Data Consistency Manager\t", " Write time:"+String.valueOf(pair.getSecond()));
				System.out.println("Data Consistency Manager\t"+ pair.getFirst().toString());
				System.out.println("Data Consistency Manager\t"+ " Write time:"+String.valueOf(pair.getSecond()));
			}
		}
	}

	public void addCurrentLockedReplicas(Tuple tuple, List<Integer> lockedReplicas){
		List<Tuple> tuples;
		
		for(int rep : lockedReplicas){
			Log.writeInLogFile("Data Consistency Manager", " add replica of "+tuple.getTupleType()+" located in:"+rep+" to current locked with version:"+tuple.getTupleVersion());
			//*System.out.println("Data Consistency Manager"+ " add replica of "+tuple.getTupleType()+" located in:"+rep+" to current locked with version:"+tuple.getTupleVersion());
			Pair<String,Integer> pair = new Pair<String,Integer>(tuple.getTupleType(),rep);
			if(currentLockedReplicas.containsKey(pair)){
				tuples = currentLockedReplicas.get(pair);
			}else{
				tuples = new ArrayList<Tuple>();
			}
			tuples.add(tuple);
			currentLockedReplicas.put(pair, tuples);
		}
		//printAllCurrentLockedReplicas();
	}
	
	public Tuple getOldWriteFromCurrentLockedReplicas(Pair<String,Integer> pair){
		//*System.out.println("Data Consistency Manager:"+" checking for current locked writes for tuple:"+pair.getFirst()+" in replicas:"+pair.getSecond());
		Log.writeInLogFile("Data Consistency Manager:"," checking for current locked writes for tuple:"+pair.getFirst()+" in replicas:"+pair.getSecond());
		if(currentLockedReplicas.containsKey(pair)){
			
			if(currentLockedReplicas.get(pair)!=null && !currentLockedReplicas.get(pair).isEmpty()){
				Iterator<Tuple> itr = currentLockedReplicas.get(pair).iterator(); 
				Tuple old = (Tuple) itr.next();
				Tuple tuple;
				while(itr.hasNext()){
					tuple  = (Tuple) itr.next();
					if(tuple.getTupleVersion() < old.getTupleVersion()){
						old = tuple;
					}
				}
				
				//*System.out.println("Data Consistency Manager:"+old.toString()+"  is found!");
				Log.writeInLogFile("Data Consistency Manager:",old.toString()+" is found!");
				currentLockedReplicas.get(pair).remove(old);
				
				if (currentLockedReplicas.get(pair).contains(old)) {
					//*System.out.println("Error, in get old write from current locked replicas!");
					System.exit(0);
				}
				
				return old;
			}
		}
		//*System.out.println("Data Consistency Manager:"+" non writes is found!");
		Log.writeInLogFile("Data Consistency Manager:"," non writes is found!");
		return null;
	}
	
	public boolean checkIfThereAreCurrentLockedReplicaForTuple(String tupleTupe, DataAllocation dataAllocation){
		//*System.out.println("Data Consistency Manager:"+" checking for current locked writes for tuple:"+tupleTupe);
		Log.writeInLogFile("Data Consistency Manager:"," checking for current locked writes for tuple:"+tupleTupe);
		
		for(int emp: dataAllocation.dataPlacementMap.get(tupleTupe)){
			
			Pair<String,Integer> pair = new Pair<String,Integer>(tupleTupe,emp);
			
			if(currentLockedReplicas.containsKey(pair)){
				if(currentLockedReplicas.get(pair)!=null && !currentLockedReplicas.get(pair).isEmpty()){
					//*System.out.println("Data Consistency Manager:"+" there is a current locked writes for tuple:"+tupleTupe+" in rep:"+emp);
					Log.writeInLogFile("Data Consistency Manager:"," there is a current locked writes for tuple:"+tupleTupe+" in rep:"+emp);
					return true;
				}
			}
		}
		
		//*System.out.println("Data Consistency Manager:"+" there is no current locked writes for tuple:"+tupleTupe);
		Log.writeInLogFile("Data Consistency Manager:"," there is no current locked writes for tuple:"+tupleTupe);
		return false;
	}
	
	public void printAllCurrentLockedReplicas(){
		System.out.println("Print all current locked replicas!");
		for(Pair<String,Integer> key : currentLockedReplicas.keySet()){
			for(Tuple tuple : currentLockedReplicas.get(key)){
				System.out.println("Tuple:"+key.getFirst()+"\treplica:"+key.getSecond()+"\tversion"+tuple.getTupleVersion());
			}
		}
	}
	
	public void initializeLockReplicaMap(DataAllocation dataAllocation){
		//*System.out.println("initialize Lock Replica Map");
		
		lockReplicaMapForWrite = new HashMap<Pair<String, Integer>, Boolean>();
		lockReplicaMapForRead = new HashMap<Pair<String, Integer>, Integer>();
		
		for(String tupleType : dataAllocation.dataPlacementMap.keySet()){
			//*System.out.println("initilize map for tuple type:"+tupleType);
			for(Integer emplacement : dataAllocation.dataPlacementMap.get(tupleType)){
				this.lockReplicaMapForWrite.put( new Pair<String,Integer>(tupleType,emplacement), false);
				this.lockReplicaMapForRead.put( new Pair<String,Integer>(tupleType,emplacement), 0);
			}
		}
	}
	
	
	public abstract List<Integer> getReplicasListRequestForWrite(int requestNodeId, List<Integer> unLockedReplicas);
	
	public abstract List<Integer> getReplicasListRequestForRead(int requestNodeId, List<Integer> unLockedReplicas);
	
	protected Pair<Float,Integer> getMaxLatencyAndPosition(float [] tab){
		
		int position = 0;
		float maxLatency = tab[0];
		int ind = 0;
		for(float elem: tab){
			if(elem > maxLatency){
				maxLatency = elem;
				position = ind;
			}
			ind++;
		}

		return new Pair<Float,Integer>(maxLatency, position);
	}
	
	public abstract int getNumberOfResponseWriteReplica(int nb_total_replica);
	
	public abstract int getNumberOfResponseReadReplica(int nb_total_replica);
	
	public List<Integer> getUnlockedReplicasForWrite(String tupleType, DataAllocation dataAllocation){
		// TODO Auto-generated method stub
		List<Integer> listOfUnlockedReplicas = new ArrayList<Integer>();
		Pair<String,Integer> pair =null;

		for(Integer emp : dataAllocation.getEmplacementNodeId(tupleType)){
			pair = new Pair<String,Integer>(tupleType, emp);
						
			if(lockReplicaMapForWrite.containsKey(pair) && lockReplicaMapForRead.containsKey(pair)){
				/*
				 * locked -> true, unlocked -> false
				 * 0 reader ==> unlocked, otherwise locked
				 */
				if(lockReplicaMapForWrite.get(pair) == false && lockReplicaMapForRead.get(pair) == 0){
					listOfUnlockedReplicas.add(pair.getSecond());
				}
			}else {
				System.out.println("Erreur! lockReplicaMap for write or read doesn't contain pair :"+pair.getFirst()+"\t"+pair.getSecond());
				Log.writeInLogFile("Data consistency manager", "Erreur! lockReplicaMap for write or read doesn't contain pair :"+pair.getFirst()+"\t"+pair.getSecond());
				System.exit(0);
			}
		}
		
		return listOfUnlockedReplicas;
	}
	
	public List<Integer> getUnlockedReplicasForRead(String tupleType, DataAllocation datAllocation){
		// TODO Auto-generated method stub
		List<Integer> listOfUnlockedReplicas = new ArrayList<Integer>();
		Pair<String,Integer> pair =null;

		for(Integer emp : datAllocation.getEmplacementNodeId(tupleType)){
			pair = new Pair<String,Integer>(tupleType, emp);
			if(lockReplicaMapForWrite.containsKey(pair)){
				/*
				 * locked -> true, unlocked -> false
				 * 0 reader ==> unlocked, otherwise locked
				 */
				if(lockReplicaMapForWrite.get(pair) == false){
					listOfUnlockedReplicas.add(pair.getSecond());
				}
			}else {
				//*System.out.println("Erreur! lockReplicaMap for write doesn't contain pair :"+pair.getFirst()+"\t"+pair.getSecond());
				System.exit(0);
			}
		}
		
		return listOfUnlockedReplicas;
	}
	
	
	public void printLockReplicaMap(){
		//*System.out.println("Print Lock Replica Map for writes");
		for(Pair<String,Integer> pair : lockReplicaMapForWrite.keySet()){
			//*System.out.println("Tuple:"+pair.getFirst()+"\templacement:"+pair.getSecond()+"\tlock:"+lockReplicaMapForWrite.get(pair));
		}
		
		//*System.out.println("\nPrint Lock Replica Map for Reads");
		for(Pair<String,Integer> pair : lockReplicaMapForRead.keySet()){
			//*System.out.println("Tuple:"+pair.getFirst()+"\templacement:"+pair.getSecond()+"\tlock:"+lockReplicaMapForRead.get(pair));
		}
	}

	
	public boolean getReplicaStateForWrite(Pair<String,Integer> pair){
		if(lockReplicaMapForWrite.containsKey(pair)){
			return lockReplicaMapForWrite.get(pair);
		}else{
			//*System.out.println("Erreur! lockReplicaMap for write doesn't contain pair :"+pair.getFirst()+"\t"+pair.getSecond());
			System.exit(0);
		}
		return false;
	}
	
	public int getReplicaStateForRead(Pair<String,Integer> pair){
		if(lockReplicaMapForRead.containsKey(pair)){
			return lockReplicaMapForRead.get(pair);
		}else{
			//*System.out.println("Erreur! lockReplicaMap for read doesn't contain pair :"+pair.getFirst()+"\t"+pair.getSecond());
			System.exit(0);
		}
		return -1;
	}
	
	
	public void setReplicaStateForWrite(Pair<String,Integer> pair, boolean state){
		/*
		 * pair.first is tuple Type, pair.second is node id
		 * true locked, false unlocked
		 */
		if(lockReplicaMapForWrite.containsKey(pair)){
			//*System.out.println("Data Consistency Manager:"+ " Set replicas of :"+pair.getFirst()+" for write in node:"+pair.getSecond()+" in state:"+state);
			Log.writeInLogFile("Data Consistency Manager", "Set replicas of :"+pair.getFirst()+" for write in node:"+pair.getSecond()+" in state:"+state);
			lockReplicaMapForWrite.put(pair,state);
		}else{
			//*System.out.println("Erreur! lockReplicaMap for write doesn't contain pair :"+pair.getFirst()+"\t"+pair.getSecond());
			System.exit(0);
		}
	}
	
	public void setReplicaStateForRead(Pair<String,Integer> pair, int nb_reader){
		/*
		 * pair.first is tuple Type, pair.second is node id
		 * > 0 locked, = 0 unlocked
		 */
		if(lockReplicaMapForRead.containsKey(pair)){
			//*System.out.println("Data Consistency Manager:"+ " Set replicas of :"+pair.getFirst()+" for Read in node:"+pair.getSecond()+" nb_reader :"+nb_reader);
			Log.writeInLogFile("Data Consistency Manager", "Set replicas of :"+pair.getFirst()+" for Read in node:"+pair.getSecond()+" nb_reader :"+nb_reader);
			lockReplicaMapForRead.put(pair,nb_reader);
			
		}else{
			//*System.out.println("Erreur! lockReplicaMap for Read doesn't contain pair :"+pair.getFirst()+"\t"+pair.getSecond());
			System.exit(0);
		}
	}

	
	/*
	 * chose the replica that responds to Read requests
	 * Here a median problem is formulated and solved
	 */
	public int selectMedianReplicas(List<Integer> listOfResponseReplicas){
		
		if(listOfResponseReplicas.size() < 3)
			return listOfResponseReplicas.get(0);
		
		Median median = new Median(listOfResponseReplicas);
		return median.getMedianReplica();
	}
	
	public void clearConsistencyData(){
		lockReplicaMapForWrite.clear();
		lockReplicaMapForRead.clear();
		currentLockedReplicas.clear();
		currentBlockedWrites.clear();
	}

	public void addBlockedWritesLatency() {
		// TODO Auto-generated method stub
		System.out.println("add Blocked write latency to blocked write latency counter");
		for(String tupleType: currentBlockedWrites.keySet()){
			for(Pair<Tuple,Double> pair: currentBlockedWrites.get(tupleType)){
				double blockedWriteLatency = Config.MAX_SIMULATION_TIME - pair.getSecond();
				LatencyStats.add_Overall_blocked_write_Latency(blockedWriteLatency);
			}
		}
	}
	
	public void saveAllCurrentBlockedWrites(Tuple tuple, Double writeTime){
//		FileWriter file;
//		try {
//			if(DataPlacement.cond==0){
//				file = new FileWriter("Stats/iFogStor/data/blockedWriteiFogStor_"+DataPlacement.nb_DataCons_By_DataProd, true);
//				
//			}else if(DataPlacement.cond==1){
//				file = new FileWriter("Stats/Heuristic/data/blockedWriteHeuristic_"+DataPlacement.nb_DataCons_By_DataProd, true);
//			
//			}else {
//				file = new FileWriter("Stats/Exact/data/blockedWriteExact_"+DataPlacement.nb_DataCons_By_DataProd, true);
//			}
//			
//			BufferedWriter fw = new BufferedWriter(file);
//
//		
//			fw.write(tuple.getTupleType()+"\t"+tuple.getTupleVersion()+"\t"+String.valueOf(writeTime)+"\n");
//			
//			fw.close();
//			
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
	}

}

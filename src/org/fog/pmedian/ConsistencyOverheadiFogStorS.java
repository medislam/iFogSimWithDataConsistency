package org.fog.pmedian;



import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.Results.SaveResults;
import org.StorageMode.FogStorage;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.combinaison.AllCombination;
import org.fog.cplex.DataAllocation;
import org.fog.dataConsistency.QuorumConsistency;
import org.fog.dataConsistency.ReadOneWriteAllConsistency;
import org.fog.dataConsistency.ReadOneWriteOneConsistency;
import org.fog.examples.DataPlacement;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.stats.LatencyStats;
import org.fog.stats.Stats;
import org.fog2.entities.Actuator;
import org.fog2.entities.FogBroker;
import org.fog2.entities.FogDevice;
import org.fog2.entities.Sensor;
import org.fog2.entities.Tuple;

public class ConsistencyOverheadiFogStorS {

	private Application application;
	private ModuleMapping moduleMapping;
	
	private Map<Pair<String, Pair<Integer,Integer>>, Pair<List<Integer>, Double>> estimationSolutionMap;
	
	private Map<Pair<String,Pair<Integer,Integer>>, Pair<List<Integer>, Double>> estimationMap;
	
	private Map<Pair<String,Pair<Integer,Integer>>, Double> estimationMapWriteLatency;
	private Map<Pair<String,Pair<Integer,Integer>>, Double> estimationMapReadLatency;
	private Map<Pair<String,Pair<Integer,Integer>>, Double> estimationMapVersionLatency;
	private Map<Pair<String,Pair<Integer,Integer>>, Double> estimationMapOverallLatency;
	private Map<Pair<String,Pair<Integer,Integer>>, Double> estimationMapDelayedWriteLatency;
	private Map<Pair<String,Pair<Integer,Integer>>, Double> estimationMapBlockedWriteLatency;
	
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbTotaleWrite;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbDoneWrite;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbResponseWrite;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbBlockedWrite;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbBlockedWriteForBlockedWrite;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbDelayedWrite;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbLockedWrite;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbReplicaViolationWrite;
	
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbServedRead;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbTotalRead;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbNonServedRead;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbReplicaViolationRead;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbReadServedWithRecentVersion;
	
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbReadServedWith1_version_old;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbReadServedWith2_version_old;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbReadServedWith3_version_old;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbReadServedWith4_version_old;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbReadServedWith5_version_old;
	private Map<Pair<String,Pair<Integer,Integer>>, Integer> estimationMapNbReadServedWithUp5_version_old;
	

	public ConsistencyOverheadiFogStorS(Application application, ModuleMapping moduleMapping) {
		// TODO Auto-generated constructor stub
		this.application = application;
		this.moduleMapping = moduleMapping;
		estimationMap = new HashMap<Pair<String, Pair<Integer,Integer>>, Pair<List<Integer>, Double>>();
		estimationSolutionMap = new HashMap<Pair<String,Pair<Integer,Integer>>, Pair<List<Integer>, Double>>();
		
		estimationMapWriteLatency = new HashMap<Pair<String,Pair<Integer,Integer>>, Double>(); 
		estimationMapReadLatency = new HashMap<Pair<String,Pair<Integer,Integer>>, Double>();
		estimationMapVersionLatency = new HashMap<Pair<String,Pair<Integer,Integer>>, Double>();
		estimationMapOverallLatency = new HashMap<Pair<String,Pair<Integer,Integer>>, Double>();
		estimationMapDelayedWriteLatency = new HashMap<Pair<String,Pair<Integer,Integer>>, Double>();
		estimationMapBlockedWriteLatency = new HashMap<Pair<String,Pair<Integer,Integer>>, Double>();
		
		
		estimationMapNbTotaleWrite = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbDoneWrite = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbResponseWrite = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbBlockedWrite = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbBlockedWriteForBlockedWrite = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbDelayedWrite = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbLockedWrite = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbReplicaViolationWrite = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		
		estimationMapNbServedRead = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbTotalRead = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbNonServedRead = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbReplicaViolationRead = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbReadServedWithRecentVersion = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbReadServedWith1_version_old = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbReadServedWith2_version_old = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbReadServedWith3_version_old = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbReadServedWith4_version_old = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbReadServedWith5_version_old = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		estimationMapNbReadServedWithUp5_version_old = new HashMap<Pair<String,Pair<Integer,Integer>>, Integer>();
		
	}
	
	
	public void consitencyEsitmation() throws Exception{
		
		try {
			AllCombination allcombin = new AllCombination(DataPlacement.min_data_replica, DataPlacement.max_data_replica, Pmedian.shortestPathNodesMap);
			allcombin.setCombinaisonMap();
			//allcombin.printCombinaisonMap();
			//System.exit(0);
			
			long nb_combin = allcombin.getCombinaisonMap().keySet().size();
			
			org.fog.examples.Log.writeNbCombinaison(DataPlacement.nb_HGW,"Methode: iFogStorS"+"\tNb_Combin:"+String.valueOf(nb_combin));

			long nb_co = 0;
			for(Pair<String, Pair<Integer,Integer>> key : allcombin.getCombinaisonMap().keySet()){
					//if(nb_co % 10000 == 0)
						//System.gc();
					
					String tupleType = key.getFirst();
					int nb_replicas = key.getSecond().getFirst();
					int num_combin = key.getSecond().getSecond();
					
					nb_co++;
					List<Integer> listFogNodes =  Pmedian.shortestPathNodesMap.get(tupleType);
					
					List<FogDevice> fogdevicesList = new ArrayList<FogDevice>();
					
					for(int devId: listFogNodes){
						fogdevicesList.add(application.getFogDeviceById(devId));
					}

					
					CloudSim.init(DataPlacement.num_user, DataPlacement.calendar, DataPlacement.trace_flag);
					
					System.out.println("\n**********************************************************************");
					System.out.println("\nEstimation of consistency overhead for");
					System.out.println("Tuple tupe:"+tupleType+"\tnb_replicas:"+nb_replicas+"\tcombin N:"+num_combin);
					
					//Log.writeInLogFile("consitencyEsitmation:", "Tuple tupe:"+tupleType+"\tnb_replicas:"+nb_replicas+"\tcombin N:"+num_combin);
					System.out.println("NB_combin :\t"+nb_co+"\tfrom total of:"+nb_combin);
					
					FogBroker broker1 = new FogBroker("broker1");

					CloudSim.broker = broker1;
					DataPlacement.cond=2;
					
					Controller controller1 = new Controller("master-controller1", DataPlacement.fogDevices, moduleMapping);
					controller1.setFogDevices(fogdevicesList);
					
					//controller1.submitApplication2(application, 0);
					List<Sensor> snr = new ArrayList<Sensor>();
					List<Actuator> act = new ArrayList<Actuator>();
					
					controller1.setActuators(act);
					controller1.setSensors(snr);
					
					broker1.setId(2);
												
					SimEntity cis = CloudSim.getEntity("CloudInformationService");
								
					controller1.setId(0);
					
					CloudSim.resetEntities();
					CloudSim.resetClock();
					CloudSim.entities.add(cis);
					CloudSim.entities.add(controller1);
					CloudSim.entities.add(broker1);
					
					for(FogDevice fogdev : DataPlacement.fogDevices){
						if(fogdevicesList.contains(fogdev)){
							CloudSim.entities.add(fogdev);
						}
					}
				

					String appId1 = "Data_Placement1";
					
					Application application1 = DataPlacement.createApplication(appId1, broker1.getId(), fogdevicesList);
					application1.setUserId(broker1.getId());
					application1.setFogDeviceList(fogdevicesList);
					
					DataPlacement.NB_REP = nb_replicas;
					DataPlacement.estimatedTuple= tupleType;
					
					if (FogStorage.application.DataConsistencyMap.get(DataPlacement.estimatedTuple).equals(DataPlacement.Strong)){
						setDataPlacementQW_QR_Strong(nb_replicas);
					}else{
						setDataPlacementQW_QR_Weak(nb_replicas);	
					}
					
					application1.setDataConsistencyProtocol(new QuorumConsistency( DataPlacement.QW, DataPlacement.QR));

					
					AppEdge appEdge = application.getEdgeMap().get(tupleType);
					
					//System.out.println("Consistency protocol:"+DataPlacement.dcp);
					//System.out.println("Consistency QW ="+DataPlacement.QW);
					//System.out.println("Consistency QR ="+DataPlacement.QR);
					
					List<Integer> replicas = new ArrayList<Integer>(allcombin.getCombinaisonMap().get(key));
										
					application1.addAppEdge(appEdge);
					
					resetVersionTupleInModules();
		
					/*
					 * Defining the input-output relationships (represented by
					 * selectivity) of the application modules.
					 */
					//application1.addTupleMappingFraction();
					
					broker1.setAppliaction(application1);
					
					DataPlacement.sendPerdiodicTuples = true;
		
					DataAllocation dataAllocation1 = new DataAllocation();
					
					
					dataAllocation1.setDataPlacementMap(tupleType, replicas);
					//dataAllocation1.loadDataAllocationMap();
					dataAllocation1.createStorageListIneachStroageNode(application1);
					
					broker1.setDataAllocation(dataAllocation1);
					//dataAllocation1.printDataAllocationMap(application1);
					application1.getDataConsistencyProtocol().initializeLockReplicaMap(dataAllocation1);
					
					System.out.println("Start simulation for tuple:"+tupleType+"\tnb_Replicas:"+nb_replicas+"\tNum Combin:"+num_combin);
					System.out.print("Fog nodes:");
					for(FogDevice fog : fogdevicesList){
						System.out.print(fog.getId()+",\t");
					}
					System.out.println();
					System.out.println("Storage emplacement:"+dataAllocation1.getEmplacementNodeId(tupleType).toString());
					CloudSim.startSimulation();
					
					/*
					 * Add latency of blocked writes
					 */
					application1.getDataConsistencyProtocol().addBlockedWritesLatency();
//					Stats.saveConsumption();
//					Stats.saveProduction();
					CloudSim.stopSimulation();
					System.out.println("End of simulation");
					
					//List<Integer> replicas = new ArrayList<Integer>(dataAllocation1.dataPlacementMap.get(pair.getFirst()));
						
					estimationMap.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Pair.create(replicas, LatencyStats.getOverall_Latency()));
					estimationMapWriteLatency.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), LatencyStats.getOverall_write_Latency());
					estimationMapReadLatency.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), LatencyStats.getOverall_read_Latency());
					estimationMapVersionLatency.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), (double)Stats.version_exchange_latency);
					estimationMapOverallLatency.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), LatencyStats.getOverall_Latency());
					estimationMapDelayedWriteLatency.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), LatencyStats.getOverall_delayed_write_Latency());
					estimationMapBlockedWriteLatency.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), LatencyStats.getOverall_blocked_write_Latency());
					
					estimationMapNbTotaleWrite.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_totol_write);
					estimationMapNbDoneWrite.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_done_write);
					estimationMapNbResponseWrite.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_response_write);
					estimationMapNbBlockedWrite.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_bloked_write);
					estimationMapNbBlockedWriteForBlockedWrite.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_bloked_write_for_blocked_write);
					estimationMapNbDelayedWrite.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_delayed_write);
					estimationMapNbLockedWrite.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_locked_write);
					estimationMapNbReplicaViolationWrite.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_replica_violation_in_write);
					
					estimationMapNbServedRead.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_served_read);
					estimationMapNbTotalRead.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_totol_read);
					estimationMapNbNonServedRead.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_non_served_read);
					estimationMapNbReplicaViolationRead.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_replica_violation_in_read);
					estimationMapNbReadServedWithRecentVersion.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_read_served_with_recent_version);
					estimationMapNbReadServedWith1_version_old.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_read_served_with_version_old_1);
					estimationMapNbReadServedWith2_version_old.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_read_served_with_version_old_2);
					estimationMapNbReadServedWith3_version_old.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_read_served_with_version_old_3);
					estimationMapNbReadServedWith4_version_old.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_read_served_with_version_old_4);
					estimationMapNbReadServedWith5_version_old.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_read_served_with_version_old_5);
					estimationMapNbReadServedWithUp5_version_old.put(Pair.create(tupleType, Pair.create(nb_replicas, num_combin)), Stats.nb_read_served_with_version_old_up5);
					
					LatencyStats.reset_ALLStats();
					Stats.reset_AllStats();

					dataAllocation1.dataPlacementMap.clear();
					application1.getDataConsistencyProtocol().clearConsistencyData();

					application1.resetStoredData();
					
					System.out.println("End of simulation!");
					
				}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	private void setDataPlacementQW_QR_Strong(int nb_median) {
		// TODO Auto-generated method stub
		if(nb_median == 1 ){
			DataPlacement.QW = 1;
			DataPlacement.QR = 1;
			
		}else if(nb_median == 2 ){
			DataPlacement.QW = 2;
			DataPlacement.QR = 1;
			
		}else if(nb_median == 3 ){
			DataPlacement.QW = 2;
			DataPlacement.QR = 2;
			
		}else if(nb_median == 4 ){
			DataPlacement.QW = 3;
			DataPlacement.QR = 2;
		
		}else if(nb_median == 5 ){
			DataPlacement.QW = 4;
			DataPlacement.QR = 2;
		}
	}
	
	
	private void setDataPlacementQW_QR_Weak(int nb_median) {
		// TODO Auto-generated method stub
		if(nb_median == 1 ){
			DataPlacement.QW = 1;
			DataPlacement.QR = 1;
			
		}else if(nb_median == 2 ){
			DataPlacement.QW = 1;
			DataPlacement.QR = 1;
			
		}else if(nb_median == 3 ){
			DataPlacement.QW = 1;
			DataPlacement.QR = 1;
			
		}else if(nb_median == 4 ){
			DataPlacement.QW = 2;
			DataPlacement.QR = 1;
		
		}else if(nb_median == 5 ){
			DataPlacement.QW = 2;
			DataPlacement.QR = 1;
		}
	}


	public void getEstimationSolution(){
		
		for(AppEdge edge : FogStorage.application.getEdges()){
			if(edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct")){
				continue;
			}
			double min = Double.MAX_VALUE;
			int nb_replicas = -1;
			int num_combin = -1;
			
			for(Pair<String,Pair<Integer, Integer>> pair: estimationMap.keySet()){
				if(pair.getFirst().equals(edge.getTupleType())){
					if(min>estimationMap.get(pair).getSecond()){
						min = estimationMap.get(pair).getSecond();
						nb_replicas = pair.getSecond().getFirst();
						num_combin = pair.getSecond().getSecond();
					}
				}	
			}
			
			if(nb_replicas==-1){
				 System.out.println("pmedian for tuple:"+edge.getTupleType()+"\tis not found!");
				 System.exit(0);
			}
			Pair<Integer, Integer> pair1 = new Pair<Integer, Integer>(nb_replicas,num_combin);
			Pair<String,Pair<Integer, Integer>> pair = new Pair<String, Pair<Integer, Integer>>(edge.getTupleType(), pair1);
			
			estimationSolutionMap.put(pair, estimationMap.get(pair));
			
		}
		
	}
	
	public void saveEstimationSolution() throws IOException {
		try {
			FileWriter fichier = new FileWriter("Stats/iFogStorS/solution/"+DataPlacement.nb_HGW+"_solution_DC_DP_"+DataPlacement.nb_DataCons_By_DataProd);
			BufferedWriter fw = new BufferedWriter(fichier);
			fw.write("");
			fw.close();
			
			float overall_latency = 0;
			FileWriter fichier2 = new FileWriter("Stats/iFogStorS/solution/"+DataPlacement.nb_HGW+"_solution_DC_DP_"+DataPlacement.nb_DataCons_By_DataProd, true);
			BufferedWriter fw2 = new BufferedWriter(fichier2);
			
			for(AppEdge edge : FogStorage.application.getEdges()){
				if(edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct")){
					continue;
				}
				
				for(Pair<String,Pair<Integer,Integer>> pair: estimationSolutionMap.keySet()){
					if(pair.getFirst().equals(edge.getTupleType())){
						
						overall_latency += estimationSolutionMap.get(pair).getSecond();
						
						fw2.write("tuple:"+pair.getFirst()+"\tNb_rep"+pair.getSecond().getFirst()+"\tNum_Com:"+pair.getSecond().getSecond()+"\tReplicas:"+estimationSolutionMap.get(pair).getFirst()+"\tLatency:"+estimationSolutionMap.get(pair).getSecond()+"\n");
						
					}	
				}
	
			}
			fw2.write("Overall latency = \t"+overall_latency);
			fw2.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
			
	private void resetVersionTupleInModules() {
		// TODO Auto-generated method stub
		for(AppModule module: application.getModules()){
			//System.out.println("rest version in module: "+module.getName());
			module.resetVersion();
		}
	}

	public void printEstimationMap(){
		System.out.println("Print All estimated values");
		
		
//		for(AppEdge edge : FogStorage.application.getEdges()){
//			if(edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct")){
//				continue;
//			}
//			
//			for(Pair<String,Integer> pair: estimationMap.keySet()){
//				if(pair.getFirst().equals(edge.getTupleType())){
//					System.out.println("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\tlatency:"+estimationMap.get(pair));
//				}	
//			}	
//		}
		
	}
	
	public void saveEstimationMap() throws IOException{
		
		FileWriter fichier_write_latency = new FileWriter("Stats/iFogStorS/latency/"+ DataPlacement.nb_HGW+"_write_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_Read_latency = new FileWriter("Stats/iFogStorS/latency/"+ DataPlacement.nb_HGW+"_Read_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_Version_latency = new FileWriter("Stats/iFogStorS/latency/"+ DataPlacement.nb_HGW+"_Version_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_Overall_latency = new FileWriter("Stats/iFogStorS/latency/"+ DataPlacement.nb_HGW+"_Overall_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_delayed_latency = new FileWriter("Stats/iFogStorS/latency/"+DataPlacement.nb_HGW+"_delayed_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_blocked_latency = new FileWriter("Stats/iFogStorS/latency/"+DataPlacement.nb_HGW+"_blocked_latency"+DataPlacement.nb_DataCons_By_DataProd, true);

		FileWriter fichier_nb_totol_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_totol_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_done_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_done_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_response_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_response_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_bloked_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_bloked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_bloked_write_for_blocked_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_bloked_write_for_blocked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_delayed_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_delayed_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_locked_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_locked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_replica_violation_in_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_replica_violation_in_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		
		FileWriter fichier_nb_totol_read = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"_nb_totol_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_served_read = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"_nb_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_non_served_read = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"_nb_non_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_replica_violation_in_read = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"_nb_replica_violation_in_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_recent_version = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"_nb_read_served_with_recent_version"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_1_version_Old = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_1_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_2_version_Old = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_2_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_3_version_Old = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_3_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_4_version_Old = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_4_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_5_version_Old = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_5_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_Up5_version_Old = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_Up5_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);

		
		FileWriter fichier_allStats = new FileWriter("Stats/iFogStorS/data/"+DataPlacement.nb_HGW+"fichier_allStats_Exact"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);

		
		
		
		try {
			BufferedWriter fw_write_latency = new BufferedWriter(fichier_write_latency);
			BufferedWriter fw_Read_latency = new BufferedWriter(fichier_Read_latency);
			BufferedWriter fw_Version_latency = new BufferedWriter(fichier_Version_latency);
			BufferedWriter fw_Overall_latency = new BufferedWriter(fichier_Overall_latency);
			BufferedWriter fw_delayed_latency = new BufferedWriter(fichier_delayed_latency);
			BufferedWriter fw_blocked_latency = new BufferedWriter(fichier_blocked_latency);

			BufferedWriter fw_nb_totol_write = new BufferedWriter(fichier_nb_totol_write);
			BufferedWriter fw_nb_done_write = new BufferedWriter(fichier_nb_done_write);
			BufferedWriter fw_nb_response_write = new BufferedWriter(fichier_nb_response_write);
			BufferedWriter fw_nb_bloked_write = new BufferedWriter(fichier_nb_bloked_write);
			BufferedWriter fw_nb_bloked_write_for_blocked_write = new BufferedWriter(fichier_nb_bloked_write_for_blocked_write);
			BufferedWriter fw_nb_delayed_write = new BufferedWriter(fichier_nb_delayed_write);
			BufferedWriter fw_nb_locked_write = new BufferedWriter(fichier_nb_locked_write);
			BufferedWriter fw_nb_replica_violation_in_write = new BufferedWriter(fichier_nb_replica_violation_in_write);
			
			BufferedWriter fw_nb_totol_read = new BufferedWriter(fichier_nb_totol_read);
			BufferedWriter fw_nb_served_read = new BufferedWriter(fichier_nb_served_read);
			BufferedWriter fw_nb_non_served_read = new BufferedWriter(fichier_nb_non_served_read);
			BufferedWriter fw_nb_replica_violation_in_read = new BufferedWriter(fichier_nb_replica_violation_in_read);
			BufferedWriter fw_nb_read_served_with_recent_version = new BufferedWriter(fichier_nb_read_served_with_recent_version);
			
			BufferedWriter fw_nb_read_served_with_1_version_Old = new BufferedWriter(fichier_nb_read_served_with_1_version_Old);
			BufferedWriter fw_nb_read_served_with_2_version_Old = new BufferedWriter(fichier_nb_read_served_with_2_version_Old);
			BufferedWriter fw_nb_read_served_with_3_version_Old = new BufferedWriter(fichier_nb_read_served_with_3_version_Old);
			BufferedWriter fw_nb_read_served_with_4_version_Old = new BufferedWriter(fichier_nb_read_served_with_4_version_Old);
			BufferedWriter fw_nb_read_served_with_5_version_Old = new BufferedWriter(fichier_nb_read_served_with_5_version_Old);
			BufferedWriter fw_nb_read_served_with_Up5_version_Old = new BufferedWriter(fichier_nb_read_served_with_Up5_version_Old);
			
			BufferedWriter fw_fichier_allStats = new BufferedWriter(fichier_allStats);

			float write_latency = 0;
			float read_latency =0;
			float version_latency =0;
			float overall_latency =0;
			float delayed_latency =0;
			float blocked_latency =0;
			
			int total_write =0;
			int done_write = 0;
			int repone_write = 0;
			int delayed_write = 0;
			int blocked_write = 0;
			int blocked_for_blocked_write = 0;
			int locked_write = 0;
			int replica_violation__write = 0;
			
			int total_read = 0;
			int served_read = 0;
			int non_served_read = 0;
			int replica_vilation_read = 0;
			int read_served_with_recent_version_read = 0;
			
			int read_served_with_1_version_Old = 0;
			int read_served_with_2_version_Old = 0;
			int read_served_with_3_version_Old = 0;
			int read_served_with_4_version_Old = 0;
			int read_served_with_5_version_Old = 0;
			int read_served_with_Up5_version_Old = 0;
			
			
			//fw.write(write + "\n");
			
			for(AppEdge edge : FogStorage.application.getEdges()){
				if(edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct")){
					continue;
				}
					
				
				for(Pair<String,Pair<Integer,Integer>> pair: estimationSolutionMap.keySet()){
					
					if(pair.getFirst().equals(edge.getTupleType())){
						//fw_write_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\tmedians:"+estimationMap.get(pair).getFirst()+"\tLatency: "+estimationMap.get(pair).getSecond()+"\n");
												
						//fw_write_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapWriteLatency.get(pair)+"\n");
						write_latency += estimationMapWriteLatency.get(pair);
						
						//fw_Read_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapReadLatency.get(pair)+"\n");
						read_latency += estimationMapReadLatency.get(pair);
						
						//fw_Version_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapVersionLatency.get(pair)+"\n");
						version_latency += estimationMapVersionLatency.get(pair);
						
						//fw_Overall_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapOverallLatency.get(pair)+"\n");
						overall_latency+=estimationMapOverallLatency.get(pair);
						
						//fw_delayed_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapDelayedWriteLatency.get(pair)+"\n");
						delayed_latency += estimationMapDelayedWriteLatency.get(pair);
						
						//fw_blocked_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapBlockedWriteLatency.get(pair)+"\n");
						blocked_latency += estimationMapBlockedWriteLatency.get(pair);
						
						
						//fw_nb_totol_write.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbTotaleWrite.get(pair)+"\n");
						total_write += estimationMapNbTotaleWrite.get(pair);
						
						//fw_nb_done_write.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbDoneWrite.get(pair)+"\n");
						done_write += estimationMapNbDoneWrite.get(pair);
						
						//fw_nb_response_write.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbResponseWrite.get(pair)+"\n");
						repone_write += estimationMapNbResponseWrite.get(pair);
						
						//fw_nb_bloked_write.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbBlockedWrite.get(pair)+"\n");
						blocked_write += estimationMapNbBlockedWrite.get(pair);
						
						//fw_nb_bloked_write_for_blocked_write.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbBlockedWriteForBlockedWrite.get(pair)+"\n");
						blocked_for_blocked_write += estimationMapNbBlockedWriteForBlockedWrite.get(pair);
						
						//fw_nb_delayed_write.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbDelayedWrite.get(pair)+"\n");
						delayed_write += estimationMapNbDelayedWrite.get(pair);
						
						//fw_nb_locked_write.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbLockedWrite.get(pair)+"\n");
						locked_write += estimationMapNbLockedWrite.get(pair);
											
						//fw_nb_replica_violation_in_write.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbReplicaViolationWrite.get(pair)+"\n");
						replica_violation__write += estimationMapNbReplicaViolationWrite.get(pair);
						
						
						//fw_nb_totol_read.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbTotalRead.get(pair)+"\n");
						total_read += estimationMapNbTotalRead.get(pair);
						
						//fw_nb_served_read.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbServedRead.get(pair)+"\n");
						served_read += estimationMapNbServedRead.get(pair);
						
						//fw_nb_non_served_read.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbNonServedRead.get(pair)+"\n");
						non_served_read += estimationMapNbNonServedRead.get(pair);
						
						//fw_nb_replica_violation_in_read.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbReplicaViolationRead.get(pair)+"\n");
						replica_vilation_read += estimationMapNbReplicaViolationRead.get(pair);
						
						//fw_nb_read_served_with_recent_version.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbReadServedWithRecentVersion.get(pair)+"\n");
						read_served_with_recent_version_read += estimationMapNbReadServedWithRecentVersion.get(pair);
						
						//fw_nb_read_served_with_1_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbReadServedWith1_version_old.get(pair)+"\n");
						read_served_with_1_version_Old += estimationMapNbReadServedWith1_version_old.get(pair);
						
						//fw_nb_read_served_with_2_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbReadServedWith2_version_old.get(pair)+"\n");
						read_served_with_2_version_Old += estimationMapNbReadServedWith2_version_old.get(pair);
						
						//fw_nb_read_served_with_3_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbReadServedWith3_version_old.get(pair)+"\n");
						read_served_with_3_version_Old += estimationMapNbReadServedWith3_version_old.get(pair);
						
						//fw_nb_read_served_with_4_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbReadServedWith4_version_old.get(pair)+"\n");
						read_served_with_4_version_Old += estimationMapNbReadServedWith4_version_old.get(pair);
						
						//fw_nb_read_served_with_5_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbReadServedWith5_version_old.get(pair)+"\n");
						read_served_with_5_version_Old += estimationMapNbReadServedWith5_version_old.get(pair);
						
						//fw_nb_read_served_with_Up5_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_rep:"+pair.getSecond().getFirst()+"\tnum_Com:"+pair.getSecond().getSecond()+"\t:"+estimationMapNbReadServedWithUp5_version_old.get(pair)+"\n");
						read_served_with_Up5_version_Old += estimationMapNbReadServedWithUp5_version_old.get(pair);
					}	
				}	
			}	
			
			fw_fichier_allStats.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
							
			fw_fichier_allStats.write("write_latency = \t"+write_latency+"\n");
			fw_fichier_allStats.write("read_latency = \t"+read_latency+"\n");
			fw_fichier_allStats.write("version_latency = \t"+version_latency+"\n");
			fw_fichier_allStats.write("overall_latency = \t"+overall_latency+"\n");;
			fw_fichier_allStats.write("delayed_latency = \t"+delayed_latency+"\n");
			fw_fichier_allStats.write("blocked_latency = \t"+blocked_latency+"\n");
			
			fw_fichier_allStats.write("\n");
			fw_fichier_allStats.write("\n");
			
			fw_fichier_allStats.write("total_write = \t"+total_write+"\n");
			fw_fichier_allStats.write("done_write = \t"+done_write+"\n");
			fw_fichier_allStats.write("repone_write = \t"+repone_write+"\n");
			fw_fichier_allStats.write("blocked_write = \t"+blocked_write+"\n");
			fw_fichier_allStats.write("blocked_for_blocked_write = \t"+blocked_for_blocked_write+"\n");
			fw_fichier_allStats.write("delayed_write = \t"+delayed_write+"\n");
			fw_fichier_allStats.write("locked_write = \t"+locked_write+"\n");
			fw_fichier_allStats.write("replica_violation__write = \t"+replica_violation__write+"\n");
			
			fw_fichier_allStats.write("\n");
			fw_fichier_allStats.write("\n");
			 
			fw_fichier_allStats.write("total_read = \t"+total_read+"\n");
			fw_fichier_allStats.write("served_read = \t"+served_read+"\n");
			fw_fichier_allStats.write("non_served_read = \t"+non_served_read+"\n");
			fw_fichier_allStats.write("replica_vilation_read = \t"+replica_vilation_read+"\n");
			fw_fichier_allStats.write("read_served_with_recent_version   = \t"+read_served_with_recent_version_read+"\n");
			fw_fichier_allStats.write("read_served_with_1_version_old    = \t"+read_served_with_1_version_Old+"\n");
			fw_fichier_allStats.write("read_served_with_2_version_old    = \t"+read_served_with_2_version_Old+"\n");
			fw_fichier_allStats.write("read_served_with_3_version_old    = \t"+read_served_with_3_version_Old+"\n");
			fw_fichier_allStats.write("read_served_with_4_version_old    = \t"+read_served_with_4_version_Old+"\n");
			fw_fichier_allStats.write("read_served_with_5_version_old    = \t"+read_served_with_5_version_Old+"\n");
			fw_fichier_allStats.write("read_served_with_up5_version_old  = \t"+read_served_with_Up5_version_Old+"\n");
			
			fw_fichier_allStats.write("==================================================================================\n");
			
			fw_write_latency.close();
			fw_Read_latency.close();
			fw_Version_latency.close();
			fw_Overall_latency.close();
			fw_delayed_latency.close();
			fw_blocked_latency.close();
			
			fw_nb_totol_write.close();
			fw_nb_done_write.close();
			fw_nb_response_write.close();
			fw_nb_bloked_write.close();
			fw_nb_bloked_write_for_blocked_write.close(); 
			fw_nb_delayed_write.close();
			fw_nb_locked_write.close();
			fw_nb_replica_violation_in_write.close(); 
			fw_nb_totol_read.close();
			fw_nb_served_read.close();
			fw_nb_non_served_read.close();
			fw_nb_replica_violation_in_read.close(); 
			fw_nb_read_served_with_recent_version.close();
			
			fw_nb_read_served_with_1_version_Old.close();
			fw_nb_read_served_with_2_version_Old.close();
			fw_nb_read_served_with_3_version_Old.close();
			fw_nb_read_served_with_4_version_Old.close();
			fw_nb_read_served_with_5_version_Old.close();
			fw_nb_read_served_with_Up5_version_Old.close();
			
			fw_fichier_allStats.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void initializeEstimationMap() throws IOException{
		
		FileWriter fichier_write_latency = new FileWriter("Stats/iFogStorS/latency/"+ DataPlacement.nb_HGW+"_write_latency"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_Read_latency = new FileWriter("Stats/iFogStorS/latency/"+ DataPlacement.nb_HGW+"_Read_latency"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_Version_latency = new FileWriter("Stats/iFogStorS/latency/"+ DataPlacement.nb_HGW+"_Version_latency"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_Overall_latency = new FileWriter("Stats/iFogStorS/latency/"+ DataPlacement.nb_HGW+"_Overall_latency"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_delayed_latency = new FileWriter("Stats/iFogStorS/latency/"+DataPlacement.nb_HGW+"_delayed_latency"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_blocked_latency = new FileWriter("Stats/iFogStorS/latency/"+DataPlacement.nb_HGW+"_blocked_latency"+DataPlacement.nb_DataCons_By_DataProd);

		FileWriter fichier_nb_totol_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_totol_write"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_done_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_done_write"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_response_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_response_write"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_bloked_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_bloked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_bloked_write_for_blocked_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_bloked_write_for_blocked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_delayed_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_delayed_write"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_locked_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_locked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_replica_violation_in_write = new FileWriter("Stats/iFogStorS/write/"+DataPlacement.nb_HGW+"_nb_replica_violation_in_write"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		
		FileWriter fichier_nb_totol_read = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"_nb_totol_read"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_served_read = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"_nb_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_non_served_read = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"_nb_non_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_replica_violation_in_read = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"_nb_replica_violation_in_read"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_read_served_with_recent_version = new FileWriter("Stats/iFogStorS/read/"+DataPlacement.nb_HGW+"_nb_read_served_with_recent_version"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_solution = new FileWriter("Stats/iFogStorS/solution/"+DataPlacement.nb_HGW+"_solution_DC_DP_"+DataPlacement.nb_DataCons_By_DataProd);
		
		
		
		try {
			BufferedWriter fw_write_latency = new BufferedWriter(fichier_write_latency);
			BufferedWriter fw_Read_latency = new BufferedWriter(fichier_Read_latency);
			BufferedWriter fw_Version_latency = new BufferedWriter(fichier_Version_latency);
			BufferedWriter fw_Overall_latency = new BufferedWriter(fichier_Overall_latency);
			BufferedWriter fw_delayed_latency = new BufferedWriter(fichier_delayed_latency);
			BufferedWriter fw_blocked_latency = new BufferedWriter(fichier_blocked_latency);

			BufferedWriter fw_nb_totol_write = new BufferedWriter(fichier_nb_totol_write);
			BufferedWriter fw_nb_done_write = new BufferedWriter(fichier_nb_done_write);
			BufferedWriter fw_nb_response_write = new BufferedWriter(fichier_nb_response_write);
			BufferedWriter fw_nb_bloked_write = new BufferedWriter(fichier_nb_bloked_write);
			BufferedWriter fw_nb_bloked_write_for_blocked_write = new BufferedWriter(fichier_nb_bloked_write_for_blocked_write);
			BufferedWriter fw_nb_delayed_write = new BufferedWriter(fichier_nb_delayed_write);
			BufferedWriter fw_nb_locked_write = new BufferedWriter(fichier_nb_locked_write);
			BufferedWriter fw_nb_replica_violation_in_write = new BufferedWriter(fichier_nb_replica_violation_in_write);
			
			BufferedWriter fw_nb_totol_read = new BufferedWriter(fichier_nb_totol_read);
			BufferedWriter fw_nb_served_read = new BufferedWriter(fichier_nb_served_read);
			BufferedWriter fw_nb_non_served_read = new BufferedWriter(fichier_nb_non_served_read);
			BufferedWriter fw_nb_replica_violation_in_read = new BufferedWriter(fichier_nb_replica_violation_in_read);
			BufferedWriter fw_nb_read_served_with_recent_version = new BufferedWriter(fichier_nb_read_served_with_recent_version);
			
			BufferedWriter fw_solution = new BufferedWriter(fichier_solution);

			
			//fw.write(write + "\n");
			
			for(AppEdge edge : FogStorage.application.getEdges()){
				if(edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct")){
					continue;
				}
				
				for(Pair<String,Pair<Integer,Integer>> pair: estimationMap.keySet()){
					if(pair.getFirst().equals(edge.getTupleType())){
						fw_write_latency.write("");
						fw_Read_latency.write("");
						fw_Version_latency.write("");
						fw_Overall_latency.write("");
						fw_delayed_latency.write("");
						fw_blocked_latency.write("");
						
						fw_nb_totol_write.write("");
						fw_nb_done_write.write("");
						fw_nb_response_write.write("");
						fw_nb_bloked_write.write("");
						fw_nb_bloked_write_for_blocked_write.write("");
						fw_nb_delayed_write.write("");
						fw_nb_locked_write.write("");
						fw_nb_replica_violation_in_write.write("");
						 
						fw_nb_totol_read.write("");
						fw_nb_served_read.write("");
						fw_nb_non_served_read.write("");
						fw_nb_replica_violation_in_read.write("");
						fw_nb_read_served_with_recent_version.write("");
						fw_solution.write("");
					}	
				}	
			}	

			//fw.close();
			fw_write_latency.close();
			fw_Read_latency.close();
			fw_Version_latency.close();
			fw_Overall_latency.close();
			fw_delayed_latency.close();
			fw_blocked_latency.close();
			
			 fw_nb_totol_write.close();
			 fw_nb_done_write.close();
			 fw_nb_response_write.close();
			 fw_nb_bloked_write.close();
			 fw_nb_bloked_write_for_blocked_write.close(); 
			 fw_nb_delayed_write.close();
			 fw_nb_locked_write.close();
			 fw_nb_replica_violation_in_write.close(); 
			 fw_nb_totol_read.close();
			 fw_nb_served_read.close();
			 fw_nb_non_served_read.close();
			 fw_nb_replica_violation_in_read.close(); 
			 fw_nb_read_served_with_recent_version.close();
			 fw_solution.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public double computeOverallLatency() {
		// TODO Auto-generated method stub
		
		double overall_laterncy = 0.0;
		
		for(AppEdge edge : FogStorage.application.getEdges()){
			if(edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct")){
				continue;
			}
			
			for(Pair<String,Pair<Integer,Integer>> pair: estimationSolutionMap.keySet()){	
				if(pair.getFirst().equals(edge.getTupleType())){

					//System.out.println("add latency of tuple:"+pair.getFirst()+"\tlatency = "+estimationSolutionMap.get(pair).getSecond());
					overall_laterncy += estimationSolutionMap.get(pair).getSecond();
				}
			}	
		}
		return overall_laterncy;
	}

}

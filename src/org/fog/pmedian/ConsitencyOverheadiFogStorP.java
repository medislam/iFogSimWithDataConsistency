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
import org.fog.cplex.DataAllocation;
import org.fog.dataConsistency.QuorumConsistency;
import org.fog.dataConsistency.ReadOneWriteAllConsistency;
import org.fog.dataConsistency.ReadOneWriteOneConsistency;
import org.fog.examples.DataPlacement;
import org.fog.lpFileConstuction.BasisDelayMatrix;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacement;
import org.fog.stats.LatencyStats;
import org.fog.stats.Stats;
import org.fog2.entities.Actuator;
import org.fog2.entities.FogBroker;
import org.fog2.entities.FogDevice;
import org.fog2.entities.Sensor;

public class ConsitencyOverheadiFogStorP {

	private Application application;
	private ModuleMapping moduleMapping;
	
	private Map<Pair<String,Integer>, Pair<List<Integer>, Double>> estimationSolutionMap;
	
	private Map<Pair<String,Integer>, Pair<List<Integer>, Double>> estimationMap;
	
	private Map<Pair<String,Integer>, Double> estimationMapWriteLatency;
	private Map<Pair<String,Integer>, Double> estimationMapReadLatency;
	private Map<Pair<String,Integer>, Double> estimationMapVersionLatency;
	private Map<Pair<String,Integer>, Double> estimationMapOverallLatency;
	private Map<Pair<String,Integer>, Double> estimationMapDelayedWriteLatency;
	private Map<Pair<String,Integer>, Double> estimationMapBlockedWriteLatency;
	
	private Map<Pair<String,Integer>, Integer> estimationMapNbTotaleWrite;
	private Map<Pair<String,Integer>, Integer> estimationMapNbDoneWrite;
	private Map<Pair<String,Integer>, Integer> estimationMapNbResponseWrite;
	private Map<Pair<String,Integer>, Integer> estimationMapNbBlockedWrite;
	private Map<Pair<String,Integer>, Integer> estimationMapNbBlockedWriteForBlockedWrite;
	private Map<Pair<String,Integer>, Integer> estimationMapNbDelayedWrite;
	private Map<Pair<String,Integer>, Integer> estimationMapNbLockedWrite;
	private Map<Pair<String,Integer>, Integer> estimationMapNbReplicaViolationWrite;
	
	private Map<Pair<String,Integer>, Integer> estimationMapNbServedRead;
	private Map<Pair<String,Integer>, Integer> estimationMapNbTotalRead;
	private Map<Pair<String,Integer>, Integer> estimationMapNbNonServedRead;
	private Map<Pair<String,Integer>, Integer> estimationMapNbReplicaViolationRead;
	private Map<Pair<String,Integer>, Integer> estimationMapNbReadServedWithRecentVersion;
	
	private Map<Pair<String,Integer>, Integer> estimationMapNbReadServedWith1_version_old;
	private Map<Pair<String,Integer>, Integer> estimationMapNbReadServedWith2_version_old;
	private Map<Pair<String,Integer>, Integer> estimationMapNbReadServedWith3_version_old;
	private Map<Pair<String,Integer>, Integer> estimationMapNbReadServedWith4_version_old;
	private Map<Pair<String,Integer>, Integer> estimationMapNbReadServedWith5_version_old;
	private Map<Pair<String,Integer>, Integer> estimationMapNbReadServedWithUp5_version_old;
	

	public ConsitencyOverheadiFogStorP(Application application, ModuleMapping moduleMapping) {
		// TODO Auto-generated constructor stub
		this.application = application;
		this.moduleMapping = moduleMapping;
		estimationMap = new HashMap<Pair<String, Integer>, Pair<List<Integer>, Double>>();
		estimationSolutionMap = new HashMap<Pair<String,Integer>, Pair<List<Integer>, Double>>();
		
		estimationMapWriteLatency = new HashMap<Pair<String,Integer>, Double>(); 
		estimationMapReadLatency = new HashMap<Pair<String,Integer>, Double>();
		estimationMapVersionLatency = new HashMap<Pair<String,Integer>, Double>();
		estimationMapOverallLatency = new HashMap<Pair<String,Integer>, Double>();
		estimationMapDelayedWriteLatency = new HashMap<Pair<String,Integer>, Double>();
		estimationMapBlockedWriteLatency = new HashMap<Pair<String,Integer>, Double>();
		
		
		estimationMapNbTotaleWrite = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbDoneWrite = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbResponseWrite = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbBlockedWrite = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbBlockedWriteForBlockedWrite = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbDelayedWrite = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbLockedWrite = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbReplicaViolationWrite = new HashMap<Pair<String,Integer>, Integer>();
		
		estimationMapNbServedRead = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbTotalRead = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbNonServedRead = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbReplicaViolationRead = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbReadServedWithRecentVersion = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbReadServedWith1_version_old = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbReadServedWith2_version_old = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbReadServedWith3_version_old = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbReadServedWith4_version_old = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbReadServedWith5_version_old = new HashMap<Pair<String,Integer>, Integer>();
		estimationMapNbReadServedWithUp5_version_old = new HashMap<Pair<String,Integer>, Integer>();
		
	}
	
	
	public void consitencyEsitmation() throws Exception{
		
		try {
			
			long nb_combin = 0;
			for(String tupleType: Pmedian.shortestPathNodesMap.keySet()){
				List<FogDevice> fogdevicesList = new ArrayList<FogDevice>();
				System.gc();
				
				for(int devId: Pmedian.shortestPathNodesMap.get(tupleType)){
					fogdevicesList.add(application.getFogDeviceById(devId));
				}
				
				for(Pair<String, Integer> pair: Pmedian.pmedianMap.keySet()){
					if(!pair.getFirst().equals(tupleType)){
						continue;
						
					}
					
					int nb_median = pair.getSecond();
					nb_combin++;
					CloudSim.init(DataPlacement.num_user, DataPlacement.calendar, DataPlacement.trace_flag);
					
					System.out.println("\n**********************************************************************");
					System.out.println("\nEstimation of consistency overhead for");
					System.out.println("Tuple tupe:"+pair.getFirst()+"\tnb_median:"+pair.getSecond());
//					
//					Log.writeInLogFile("consitencyEsitmation:", "Estimation of consistency overhead for:"+"\ttupe:"+pair.getFirst()+"\tnb_median:"+pair.getSecond());
										
					FogBroker broker1 = new FogBroker("broker1");

					CloudSim.broker = broker1;
					DataPlacement.cond=1;
					
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
					
					for(FogDevice fogdev :DataPlacement.fogDevices){
						if(fogdevicesList.contains(fogdev)){
							CloudSim.entities.add(fogdev);
						}
						
					}
				

					String appId1 = "Data_Placement1";
					
					Application application1 = DataPlacement.createApplication(appId1, broker1.getId(), fogdevicesList);
					application1.setUserId(broker1.getId());
					application1.setFogDeviceList(fogdevicesList);
					
					DataPlacement.NB_REP = nb_median;
					DataPlacement.estimatedTuple= pair.getFirst();
										
					if (FogStorage.application.DataConsistencyMap.get(DataPlacement.estimatedTuple).equals(DataPlacement.Strong)){
						setDataPlacementQW_QR_Strong(nb_median);
					}else{
						setDataPlacementQW_QR_Weak(nb_median);	
					}
					
					
					
					application1.setDataConsistencyProtocol(new QuorumConsistency( DataPlacement.QW, DataPlacement.QR));
//					System.out.println("Data consistency protocol:"+FogStorage.application.DataConsistencyMap.get(DataPlacement.estimatedTuple));
//					System.out.println("DataPlacement.QW="+DataPlacement.QW+"\tDataPlacement.QR="+DataPlacement.QR);
					
										
					AppEdge appEdge = application.getEdgeMap().get(tupleType);
					
//					System.out.println("Consistency protocol:"+DataPlacement.dcp);
//					System.out.println("Consistency QW ="+DataPlacement.QW);
//					System.out.println("Consistency QR ="+DataPlacement.QR);
					
					List<Integer> replicas = new ArrayList<Integer>(Pmedian.pmedianMap.get(pair));
										
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
					
//					System.out.println("Start simulation for tuple:"+pair.getFirst()+"\tnb_median:"+pair.getSecond());
//					System.out.print("Fog nodes:");
//					for(FogDevice fog : fogdevicesList){
//						System.out.print(fog.getId()+",\t");
//					}
//					System.out.println();
//					System.out.println("Storage emplacement:"+dataAllocation1.getEmplacementNodeId(pair.getFirst()).toString());
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
						
					estimationMap.put(pair, Pair.create(replicas, LatencyStats.getOverall_Latency()));
					estimationMapWriteLatency.put(pair, LatencyStats.getOverall_write_Latency());
					estimationMapReadLatency.put(pair, LatencyStats.getOverall_read_Latency());
					estimationMapVersionLatency.put(pair, (double)Stats.version_exchange_latency);
					estimationMapOverallLatency.put(pair, LatencyStats.getOverall_Latency());
					estimationMapDelayedWriteLatency.put(pair, LatencyStats.getOverall_delayed_write_Latency());
					estimationMapBlockedWriteLatency.put(pair, LatencyStats.getOverall_blocked_write_Latency());
					
					estimationMapNbTotaleWrite.put(pair, Stats.nb_totol_write);
					estimationMapNbDoneWrite.put(pair, Stats.nb_done_write);
					estimationMapNbResponseWrite.put(pair, Stats.nb_response_write);
					estimationMapNbBlockedWrite.put(pair, Stats.nb_bloked_write);
					estimationMapNbBlockedWriteForBlockedWrite.put(pair, Stats.nb_bloked_write_for_blocked_write);
					estimationMapNbDelayedWrite.put(pair, Stats.nb_delayed_write);
					estimationMapNbLockedWrite.put(pair, Stats.nb_locked_write);
					estimationMapNbReplicaViolationWrite.put(pair, Stats.nb_replica_violation_in_write);
					
					estimationMapNbServedRead.put(pair, Stats.nb_served_read);
					estimationMapNbTotalRead.put(pair, Stats.nb_totol_read);
					estimationMapNbNonServedRead.put(pair, Stats.nb_non_served_read);
					estimationMapNbReplicaViolationRead.put(pair, Stats.nb_replica_violation_in_read);
					estimationMapNbReadServedWithRecentVersion.put(pair, Stats.nb_read_served_with_recent_version);
					estimationMapNbReadServedWith1_version_old.put(pair, Stats.nb_read_served_with_version_old_1);
					estimationMapNbReadServedWith2_version_old.put(pair, Stats.nb_read_served_with_version_old_2);
					estimationMapNbReadServedWith3_version_old.put(pair, Stats.nb_read_served_with_version_old_3);
					estimationMapNbReadServedWith4_version_old.put(pair, Stats.nb_read_served_with_version_old_4);
					estimationMapNbReadServedWith5_version_old.put(pair, Stats.nb_read_served_with_version_old_5);
					estimationMapNbReadServedWithUp5_version_old.put(pair, Stats.nb_read_served_with_version_old_up5);
					
					LatencyStats.reset_ALLStats();
					Stats.reset_AllStats();

					dataAllocation1.dataPlacementMap.clear();
					application1.getDataConsistencyProtocol().clearConsistencyData();

					application1.resetStoredData();
					
					System.out.println("End of simulation!");
					
				}
			}
			
			org.fog.examples.Log.writeNbCombinaison(DataPlacement.nb_HGW,"Methode: iFogStorP"+"\tNb_combin:"+String.valueOf(nb_combin));
			
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
			DataPlacement.QW = 3;
			DataPlacement.QR = 1;
			
		}else if(nb_median == 4 ){
			DataPlacement.QW = 4;
			DataPlacement.QR = 1;
		
		}else if(nb_median == 5 ){
			DataPlacement.QW = 5;//4
			DataPlacement.QR = 1;//2
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
			DataPlacement.QW = 2;//1
			DataPlacement.QR = 1;
			
		}else if(nb_median == 4 ){
			DataPlacement.QW = 3;
			DataPlacement.QR = 1;//1
		
		}else if(nb_median == 5 ){
			DataPlacement.QW = 3;//2
			DataPlacement.QR = 1;//1
		}
	}


	public void getEstimationSolution(){
		
		for(AppEdge edge : FogStorage.application.getEdges()){
			if(edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct")){
				continue;
			}
			double min = Double.MAX_VALUE;
			int pmedian = -1;
			
			for(Pair<String,Integer> pair: estimationMap.keySet()){
				if(pair.getFirst().equals(edge.getTupleType())){
					if(min>estimationMap.get(pair).getSecond()){
						min = estimationMap.get(pair).getSecond();
						pmedian = pair.getSecond();
						
					}
				}	
			}
						
			if(pmedian==-1){
				 System.out.println("pmedian for tuple:"+edge.getTupleType()+"\tis not found!");
				 System.exit(0);
			}
			
			Pair<String,Integer> pair = new Pair<String, Integer>(edge.getTupleType(), pmedian);
			
			estimationSolutionMap.put(pair, estimationMap.get(pair));
			
		}
		
	}
	
	public void saveEstimationSolution() throws IOException {
		try {
			FileWriter fichier = new FileWriter("Stats/iFogStorP/solution/"+DataPlacement.nb_HGW+"_solution_DC_DP_"+DataPlacement.nb_DataCons_By_DataProd);
			BufferedWriter fw = new BufferedWriter(fichier);
			fw.write("");
			fw.close();
			
			float overall_latency = 0;
			FileWriter fichier2 = new FileWriter("Stats/iFogStorP/solution/"+DataPlacement.nb_HGW+"_solution_DC_DP_"+DataPlacement.nb_DataCons_By_DataProd, true);
			BufferedWriter fw2 = new BufferedWriter(fichier2);
			
			for(AppEdge edge : FogStorage.application.getEdges()){
				if(edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct")){
					continue;
				}
				
				for(Pair<String,Integer> pair: estimationSolutionMap.keySet()){
					if(pair.getFirst().equals(edge.getTupleType())){
						
						
						overall_latency += estimationSolutionMap.get(pair).getSecond();
						fw2.write(pair.getFirst()+"\t"+pair.getSecond()+"\t"+estimationSolutionMap.get(pair).getFirst()+"\t"+estimationSolutionMap.get(pair).getSecond()+"\n");
						
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
		
		
		for(AppEdge edge : FogStorage.application.getEdges()){
			if(edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct")){
				continue;
			}
			
			for(Pair<String,Integer> pair: estimationMap.keySet()){
				if(pair.getFirst().equals(edge.getTupleType())){
					System.out.println("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\tlatency:"+estimationMap.get(pair));
				}	
			}	
		}
		
	}
	
	
	public void saveAllEstimationValues() throws IOException {
		try {
			FileWriter fichier = new FileWriter("Stats/iFogStorP/solution/"+DataPlacement.nb_HGW+"_All_estimations_DC_DP_"+DataPlacement.nb_DataCons_By_DataProd);
			BufferedWriter fw = new BufferedWriter(fichier);
			fw.write("");
			fw.close();
			
			
			FileWriter fichier2 = new FileWriter("Stats/iFogStorP/solution/"+DataPlacement.nb_HGW+"_All_estimations_DC_DP_"+DataPlacement.nb_DataCons_By_DataProd, true);
			BufferedWriter fw2 = new BufferedWriter(fichier2);
			
			for(AppEdge edge : FogStorage.application.getEdges()){
				if(edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct")){
					continue;
				}
				
				for(Pair<String,Integer> pair: estimationMap.keySet()){
					if(pair.getFirst().equals(edge.getTupleType())){
						
						fw2.write(pair.getFirst()+"\t"+pair.getSecond()+"\t"+estimationMap.get(pair).getFirst()+"\t"+estimationMap.get(pair).getSecond()+"\n");
						
					}	
				}
	
			}
			
			fw2.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveEstimationMap() throws IOException{
		
		FileWriter fichier_write_latency = new FileWriter("Stats/iFogStorP/latency/"+ DataPlacement.nb_HGW+"_write_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_Read_latency = new FileWriter("Stats/iFogStorP/latency/"+ DataPlacement.nb_HGW+"_Read_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_Version_latency = new FileWriter("Stats/iFogStorP/latency/"+ DataPlacement.nb_HGW+"_Version_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_Overall_latency = new FileWriter("Stats/iFogStorP/latency/"+ DataPlacement.nb_HGW+"_Overall_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_delayed_latency = new FileWriter("Stats/iFogStorP/latency/"+DataPlacement.nb_HGW+"_delayed_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_blocked_latency = new FileWriter("Stats/iFogStorP/latency/"+DataPlacement.nb_HGW+"_blocked_latency"+DataPlacement.nb_DataCons_By_DataProd, true);

		FileWriter fichier_nb_totol_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_totol_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_done_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_done_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_response_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_response_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_bloked_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_bloked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_bloked_write_for_blocked_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_bloked_write_for_blocked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_delayed_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_delayed_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_locked_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_locked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_replica_violation_in_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_replica_violation_in_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		
		FileWriter fichier_nb_totol_read = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"_nb_totol_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_served_read = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"_nb_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_non_served_read = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"_nb_non_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_replica_violation_in_read = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"_nb_replica_violation_in_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_recent_version = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"_nb_read_served_with_recent_version"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_1_version_Old = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_1_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_2_version_Old = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_2_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_3_version_Old = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_3_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_4_version_Old = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_4_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_5_version_Old = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_5_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_Up5_version_Old = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"fichier_nb_read_served_with_Up5_version_Old"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);

		
		FileWriter fichier_allStats = new FileWriter("Stats/iFogStorP/data/"+DataPlacement.nb_HGW+"fichier_allStats_Median"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);

		
		
		
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
					
				
				for(Pair<String,Integer> pair: estimationSolutionMap.keySet()){
					if(pair.getFirst().equals(edge.getTupleType())){
						fw_write_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\tmedians:"+estimationMap.get(pair).getFirst()+"\tLatency: "+estimationMap.get(pair).getSecond()+"\n");
												
						fw_write_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapWriteLatency.get(pair)+"\n");
						write_latency += estimationMapWriteLatency.get(pair);
						
						fw_Read_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapReadLatency.get(pair)+"\n");
						read_latency += estimationMapReadLatency.get(pair);
						
						fw_Version_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapVersionLatency.get(pair)+"\n");
						version_latency += estimationMapVersionLatency.get(pair);
						
						fw_Overall_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapOverallLatency.get(pair)+"\n");
						overall_latency+=estimationMapOverallLatency.get(pair);
						
						fw_delayed_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapDelayedWriteLatency.get(pair)+"\n");
						delayed_latency += estimationMapDelayedWriteLatency.get(pair);
						
						fw_blocked_latency.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapBlockedWriteLatency.get(pair)+"\n");
						blocked_latency += estimationMapBlockedWriteLatency.get(pair);
						
						
						fw_nb_totol_write.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbTotaleWrite.get(pair)+"\n");
						total_write += estimationMapNbTotaleWrite.get(pair);
						
						fw_nb_done_write.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbDoneWrite.get(pair)+"\n");
						done_write += estimationMapNbDoneWrite.get(pair);
						
						fw_nb_response_write.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbResponseWrite.get(pair)+"\n");
						repone_write += estimationMapNbResponseWrite.get(pair);
						
						fw_nb_bloked_write.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbBlockedWrite.get(pair)+"\n");
						blocked_write += estimationMapNbBlockedWrite.get(pair);
						
						fw_nb_bloked_write_for_blocked_write.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbBlockedWriteForBlockedWrite.get(pair)+"\n");
						blocked_for_blocked_write += estimationMapNbBlockedWriteForBlockedWrite.get(pair);
						
						fw_nb_delayed_write.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbDelayedWrite.get(pair)+"\n");
						delayed_write += estimationMapNbDelayedWrite.get(pair);
						
						fw_nb_locked_write.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbLockedWrite.get(pair)+"\n");
						locked_write += estimationMapNbLockedWrite.get(pair);
											
						fw_nb_replica_violation_in_write.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbReplicaViolationWrite.get(pair)+"\n");
						replica_violation__write += estimationMapNbReplicaViolationWrite.get(pair);
						
						
						fw_nb_totol_read.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbTotalRead.get(pair)+"\n");
						total_read += estimationMapNbTotalRead.get(pair);
						
						fw_nb_served_read.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbServedRead.get(pair)+"\n");
						served_read += estimationMapNbServedRead.get(pair);
						
						fw_nb_non_served_read.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbNonServedRead.get(pair)+"\n");
						non_served_read += estimationMapNbNonServedRead.get(pair);
						
						fw_nb_replica_violation_in_read.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbReplicaViolationRead.get(pair)+"\n");
						replica_vilation_read += estimationMapNbReplicaViolationRead.get(pair);
						
						fw_nb_read_served_with_recent_version.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbReadServedWithRecentVersion.get(pair)+"\n");
						read_served_with_recent_version_read += estimationMapNbReadServedWithRecentVersion.get(pair);
						
						fw_nb_read_served_with_1_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbReadServedWith1_version_old.get(pair)+"\n");
						read_served_with_1_version_Old += estimationMapNbReadServedWith1_version_old.get(pair);
						
						fw_nb_read_served_with_2_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbReadServedWith2_version_old.get(pair)+"\n");
						read_served_with_2_version_Old += estimationMapNbReadServedWith2_version_old.get(pair);
						
						fw_nb_read_served_with_3_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbReadServedWith3_version_old.get(pair)+"\n");
						read_served_with_3_version_Old += estimationMapNbReadServedWith3_version_old.get(pair);
						
						fw_nb_read_served_with_4_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbReadServedWith4_version_old.get(pair)+"\n");
						read_served_with_4_version_Old += estimationMapNbReadServedWith4_version_old.get(pair);
						
						fw_nb_read_served_with_5_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbReadServedWith5_version_old.get(pair)+"\n");
						read_served_with_5_version_Old += estimationMapNbReadServedWith5_version_old.get(pair);
						
						fw_nb_read_served_with_Up5_version_Old.write("Tuple Type:"+pair.getFirst()+"\tnb_median:"+pair.getSecond()+"\t:"+estimationMapNbReadServedWithUp5_version_old.get(pair)+"\n");
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
		
		FileWriter fichier_write_latency = new FileWriter("Stats/iFogStorP/latency/"+ DataPlacement.nb_HGW+"_write_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_Read_latency = new FileWriter("Stats/iFogStorP/latency/"+ DataPlacement.nb_HGW+"_Read_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_Version_latency = new FileWriter("Stats/iFogStorP/latency/"+ DataPlacement.nb_HGW+"_Version_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_Overall_latency = new FileWriter("Stats/iFogStorP/latency/"+ DataPlacement.nb_HGW+"_Overall_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_delayed_latency = new FileWriter("Stats/iFogStorP/latency/"+DataPlacement.nb_HGW+"_delayed_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_blocked_latency = new FileWriter("Stats/iFogStorP/latency/"+DataPlacement.nb_HGW+"_blocked_latency"+DataPlacement.nb_DataCons_By_DataProd, true);

		FileWriter fichier_nb_totol_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_totol_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_done_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_done_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_response_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_response_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_bloked_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_bloked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_bloked_write_for_blocked_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_bloked_write_for_blocked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_delayed_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_delayed_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_locked_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_locked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_replica_violation_in_write = new FileWriter("Stats/iFogStorP/write/"+DataPlacement.nb_HGW+"_nb_replica_violation_in_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		
		FileWriter fichier_nb_totol_read = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"_nb_totol_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_served_read = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"_nb_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd);
		FileWriter fichier_nb_non_served_read = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"_nb_non_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_replica_violation_in_read = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"_nb_replica_violation_in_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_nb_read_served_with_recent_version = new FileWriter("Stats/iFogStorP/read/"+DataPlacement.nb_HGW+"_nb_read_served_with_recent_version"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier_solution = new FileWriter("Stats/iFogStorP/solution/"+DataPlacement.nb_HGW+"_solution_DC_DP_"+DataPlacement.nb_DataCons_By_DataProd, true);
		
		
		
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
				
				for(Pair<String,Integer> pair: estimationMap.keySet()){
					if(pair.getFirst().equals(edge.getTupleType())){
						fw_write_latency.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_Read_latency.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_Version_latency.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_Overall_latency.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_delayed_latency.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_blocked_latency.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						
						fw_nb_totol_write.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_nb_done_write.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_nb_response_write.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_nb_bloked_write.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_nb_bloked_write_for_blocked_write.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_nb_delayed_write.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_nb_locked_write.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_nb_replica_violation_in_write.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						 
						fw_nb_totol_read.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_nb_served_read.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_nb_non_served_read.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_nb_replica_violation_in_read.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_nb_read_served_with_recent_version.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
						fw_solution.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
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
			
			for(Pair<String,Integer> pair: estimationSolutionMap.keySet()){	
				if(pair.getFirst().equals(edge.getTupleType())){

					//System.out.println("add latency of tuple:"+pair.getFirst()+"\tlatency = "+estimationSolutionMap.get(pair).getSecond());
					overall_laterncy += estimationSolutionMap.get(pair).getSecond();
				}
			}	
		}
		return overall_laterncy;
	}

}

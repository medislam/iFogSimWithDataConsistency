package org.StorageMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import org.Results.SaveResults;
import org.apache.commons.math3.geometry.spherical.twod.Circle;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.cplex.CallCplex;
import org.fog.cplex.DataAllocation;
import org.fog.criticalDataPourcentage.CriticalData;
import org.fog.dataConsistency.QuorumConsistency;
import org.fog.dataConsistency.ReadOneWriteAllConsistency;
import org.fog.dataConsistency.ReadOneWriteOneConsistency;
import org.fog.examples.DataPlacement;
import org.fog.lpFileConstuction.BasisDelayMatrix;
import org.fog.lpFileConstuction.ConsProdMatrix;
import org.fog.lpFileConstuction.DataSizeVector;
import org.fog.lpFileConstuction.FreeCapacityVector;
import org.fog.lpFileConstuction.MakeLPFile;
import org.fog.stats.LatencyStats;
import org.fog.stats.Stats;
import org.fog.lpFileConstuction.MakeLPFile2;
import org.fog.pMedianOfAllConsumersShortestPaths.AllShortestPathsNodes;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.pmedian.ConsistencyOverheadExact;
import org.fog.pmedian.ConsistencyOverheadiFogStorS;
import org.fog.pmedian.ConsitencyOverheadiFogStorP;
import org.fog.pmedian.Pmedian;
import org.fog.pmedian.PmedianFormulation;
import org.fog.pmedian.PmedianSolving;
import org.fog.utils.TimeKeeper;
import org.fog2.entities.Actuator;
import org.fog2.entities.FogBroker;
import org.fog2.entities.FogDevice;
import org.fog2.entities.Sensor;

public class FogStorage {

	public static Application application;

	public FogStorage() {

	}

	public void sim() throws Exception {
		System.out.println("\n\n\n----------------------------------------------------------");
		System.out.println(DataPlacement.FogStorage);
		Log.writeInLogFile("DataPlacement","----------------------------------------------------------");
		Log.writeInLogFile("DataPlacement", DataPlacement.FogStorage);

		org.fog.examples.Log.writeSolvingTime(DataPlacement.nb_HGW,	"----------------------------------------------------------------------");
		org.fog.examples.Log.writeSolvingTime(DataPlacement.nb_HGW, "consProd:"	+ DataPlacement.nb_DataCons_By_DataProd + "		storage mode:"+ DataPlacement.FogStorage);
		CloudSim.init(DataPlacement.num_user, DataPlacement.calendar,DataPlacement.trace_flag);
		String appId = "Data_Placement"; // identifier of the application
		FogBroker broker = new FogBroker("broker");
		System.out.println("Creating of the Fog devices!");
		Log.writeInLogFile("DataPlacement", "Creating of the Fog devices!");

		DataPlacement.createFogDevices();

		System.out.println("Creating of Sensors and Actuators!");
		Log.writeInLogFile("DataPlacement",	"Creating of Sensors and Actuators!");
		DataPlacement.createSensorsAndActuators(broker.getId(), appId);

		/* Module deployment */
		System.out.println("Creating and Adding modules to devices");
		Log.writeInLogFile("DataPlacement",	"Creating and Adding modules to devices");
		ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping
		moduleMapping.addModulesToFogDevices();
		moduleMapping.setModuleToHostMap();

		application = DataPlacement.createApplication(appId, broker.getId());
		application.setUserId(broker.getId());
		application.setFogDeviceList(DataPlacement.fogDevices);

		DataPlacement.min_data_replica = 3;
		DataPlacement.max_data_replica = 3;
		DataPlacement.QW = 1;
		DataPlacement.QR = 2;

		System.out.println("Controller!");
		Log.writeInLogFile("DataPlacement", "Controller!");

		Controller controller = new Controller("master-controller",	DataPlacement.fogDevices, DataPlacement.sensors,DataPlacement.actuators, moduleMapping);
		controller.submitApplication(application, 0);

		TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

		System.out.println("Basis Delay Matrix computing!");
		Log.writeInLogFile("DataPlacement", "Basis Delay Matrix computing!");
		BasisDelayMatrix delayMatrix = new BasisDelayMatrix(DataPlacement.fogDevices, DataPlacement.nb_Service_HGW,
				DataPlacement.nb_Service_LPOP, DataPlacement.nb_Service_RPOP,DataPlacement.nb_Service_DC, DataPlacement.nb_HGW,
				DataPlacement.nb_LPOP, DataPlacement.nb_RPOP,DataPlacement.nb_DC, application);

		//delayMatrix.getDelayMatrix(DataPlacement.fogDevices);

		DataAllocation dataAllocation = new DataAllocation();

		/* set application in the broker */
		broker.setAppliaction(application);
		broker.setDataAllocation(dataAllocation);

		// if(DataPlacement.sim==0 &&
		// DataPlacement.dcp.equals(DataPlacement.Quorum)){
		if (DataPlacement.generateInfrastrucutre) {
			System.out.println("generate infrastructure");
			DataPlacement.generateInfrastrucutre = false;

			delayMatrix.getDelayMatrix(DataPlacement.fogDevices);
//			BasisDelayMatrix.loadBasisDelayMatrix();
//			BasisDelayMatrix.loadmAdjacenceMatrix();
//			BasisDelayMatrix.loadmFlowMatrix();
			
			
			/*
			 * Connecting the application modules (vertices) in the application
			 * model (directed graph) with edges
			 */
			application.addAppEdgesToApplication();
			org.fog.examples.Log.writeTotalNbCons(DataPlacement.nb_HGW,"Total Nb Cons:" + String.valueOf(application.total_nb_cons));

			/*
			 * Defining the input-output relationships (represented by
			 * selectivity) of the application modules.
			 */
			application.addTupleMappingFraction();
			// application.loadTupleMappingFraction();

			/* saving the configurations */
			System.out.println("Saving infrastructure ...");
			Log.writeInLogFile("DataPlacement", "Saving infrastructure ...");
			BasisDelayMatrix.saveBasisDelayMatrix();
			BasisDelayMatrix.savemAdjacenceMatrix();
			BasisDelayMatrix.savemFlowMatrix();
			application.saveApplicationEdges();
			application.saveTupleMappingFraction();
			System.out.println("End of saving");
			Log.writeInLogFile("DataPlacement", "End of saving");

		} else {
			System.out.println("Load infrastructure");
			Log.writeInLogFile("DataPlacement", "Loading ....");
			BasisDelayMatrix.loadBasisDelayMatrix();
			BasisDelayMatrix.loadmAdjacenceMatrix();
			BasisDelayMatrix.loadmFlowMatrix();
			application.loadApplicationEdges();
			application.loadTupleMappingFraction();
			// dataAllocation.loadDataAllocationMap();
			System.out.println("Loaded");
			Log.writeInLogFile("DataPlacement", "Loaded");
		}

		application.setTupleList();
		CriticalData critical = new CriticalData();
		application.setDataConsistencyMap(critical.getCriticalData(application.getTupleList(),DataPlacement.critical_data_pourcentage));
		application.setDataConsistencyProtocol(new QuorumConsistency(DataPlacement.QW, DataPlacement.QR));
		
//		iFogStor method

//		/* generate write and read basis delay files */
//		delayMatrix.generateBasisWriteDelayFile(DataPlacement.nb_HGW);
//		delayMatrix.generateBasisReadDelayFile(DataPlacement.nb_HGW);
//
//		/* generate Data Size vector */
//		System.out.println("Generating of Data Size!");
//		Log.writeInLogFile("DataPlacement", "Generating of Data Size!");
//		DataSizeVector dataSizeVec = new DataSizeVector(application.getEdgeMap(), DataPlacement.nb_Service_HGW,DataPlacement.nb_Service_LPOP, DataPlacement.nb_Service_RPOP,application);
//		dataSizeVec.generateDataSizeFile();
//
//		/* generate ConsProd matrix */
//		System.out.println("Generating of ConsProdMap!");
//		Log.writeInLogFile("DataPlacement", "Generating of ConsProdMap!");
//		ConsProdMatrix consProdMap = new ConsProdMatrix(application.getEdgeMap(), DataPlacement.nb_Service_HGW,DataPlacement.nb_Service_LPOP, DataPlacement.nb_Service_RPOP,DataPlacement.nb_Service_DC);
//		consProdMap.generateConsProdFile();
//
//		/* generate Free Capacity vector */
//		System.out.println("Generating of Free Capacity!");
//		Log.writeInLogFile("DataPlacement", "Generating of Free Capacity!");
//		FreeCapacityVector freeCapacity = new FreeCapacityVector(DataPlacement.fogDevices, DataPlacement.nb_HGW,DataPlacement.nb_LPOP, DataPlacement.nb_RPOP,DataPlacement.nb_DC);
//		freeCapacity.generateFreeCapacityFile();
//		// System.out.println("\n"+freeCapacity.toString());
//
//		System.out.println("Generating of Data Actors!");
//		Log.writeInLogFile("DataPlacement", "Generating of Data Actors!");
//		generateDataActorsFile();
//
		long begin_t ;
//		= Calendar.getInstance().getTimeInMillis();
//
//		System.out.println("Making LP file...");
//		Log.writeInLogFile("DataPlacement", "Making LP file...");
//		// MakeLPFile2 mlpf = new MakeLPFile2(DataPlacement.nb_HGW);
//		MakeLPFile mlpf = new MakeLPFile(DataPlacement.nb_HGW);
//		
//		System.gc();
//
//		int dataHost, dataCons, dataProd;
//		dataHost = DataPlacement.nb_HGW + DataPlacement.nb_LPOP	+ DataPlacement.nb_RPOP + DataPlacement.nb_DC;
//		dataProd = DataPlacement.nb_Service_HGW + DataPlacement.nb_Service_LPOP	+ DataPlacement.nb_Service_RPOP;
//		dataCons = DataPlacement.nb_Service_LPOP+ DataPlacement.nb_Service_RPOP + DataPlacement.nb_Service_DC;
		long end_t;
//		= Calendar.getInstance().getTimeInMillis();
		long period_t ;
//		= end_t - begin_t;
//
//		org.fog.examples.Log.writeProblemFormulationTime(DataPlacement.nb_HGW,"Methode: iFogStor" + "\ttime:" + String.valueOf(period_t));
//		
//		begin_t = Calendar.getInstance().getTimeInMillis();
//		CallCplex cc = new CallCplex(DataPlacement.nb_HGW + "cplex_"+ DataPlacement.nb_DataCons_By_DataProd + ".lp", dataProd,dataHost);
//		cc.problemSolving(DataPlacement.nb_HGW);
//	    end_t = Calendar.getInstance().getTimeInMillis();
//		period_t = end_t - begin_t;
//		
//		org.fog.examples.Log.writeSolvingTime(DataPlacement.nb_HGW,"Methode: iFogStor" + "\ttime:" + String.valueOf(period_t));
//		
//		System.gc();
		
		
		// use replication without consistency

		
		/* data allocation */
		
		
		
		
		
		System.out.println("Compute iFogStor replicas");
		Log.writeInLogFile("P median:", "\tCompute iFogStor replicas");
		int i=0;
		
		List<Integer> potentialNodes = new ArrayList<Integer>();
		for(FogDevice fogdev : DataPlacement.fogDevices) {
			potentialNodes.add(fogdev.getId());
		}
		
		
		begin_t = Calendar.getInstance().getTimeInMillis();
		
		for(AppEdge edge: application.getEdges()){
			
			if((edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct"))){
				continue;
			}
			
			System.out.println("\n-----------------------------------");
			System.out.println("Edge:"+edge.getTupleType());
			Log.writeInLogFile("P median:", "Tuple tupe:"+edge.getTupleType());
			
			int producerNode = 	application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(application.getEdgeMap().get(edge.getTupleType()).getSource()) ).getId();

			List<Integer> nodes = new ArrayList<Integer>();
			
			for(String consModule: application.getEdgeMap().get(edge.getTupleType()).getDestination()){
				nodes.add(application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(consModule)).getId());
			}
			
			
			
			System.out.println("P median:"+ "Tuple tupe:"+edge.getTupleType()+"\nodes:"+nodes);
			System.out.println("P median:"+ "Tuple tupe:"+edge.getTupleType()+"\tshorest nodes:"+potentialNodes);
			
			Log.writeInLogFile("P median:", "Tuple tupe:"+edge.getTupleType()+"\nodes:"+nodes);
			Log.writeInLogFile("P median:", "Tuple tupe:"+edge.getTupleType()+"\tshorest nodes:"+potentialNodes);
			
			int nb_median= 3; // 3 replicas
			
			System.out.println("P-median formualtion");
			PmedianFormulation pMedianFormulation = new PmedianFormulation(nb_median);
			pMedianFormulation.contructionLpFile(producerNode, nodes, potentialNodes, i);
			
			System.out.println("P-median solving");
			PmedianSolving pMedianSolving = new PmedianSolving();
			pMedianSolving.problemSolving(nodes, potentialNodes, i);
			
			List<Integer> replicas = pMedianSolving.getSolution(nodes, potentialNodes,i);
			
			System.out.println("Replicas: "+replicas.toString());
			
			dataAllocation.dataPlacementMap.put(edge.getTupleType(), replicas);

			i++;
			
		}	
		
		end_t = Calendar.getInstance().getTimeInMillis();
		period_t = end_t - begin_t;
				
		org.fog.examples.Log.writePmedianTime(DataPlacement.nb_HGW,"iFogStor P Median" + "\ttime:" + String.valueOf(period_t));


		//dataAllocation.setDataPlacementMap(DataPlacement.nb_HGW, application);
		org.fog.examples.Log.writeDataAllocationStats(DataPlacement.nb_HGW,"------------------------------------------\n"+ DataPlacement.nb_DataCons_By_DataProd + "\n"+ DataPlacement.storageMode + "\n"+ dataAllocation.dataAllocationStats(application));
		dataAllocation.saveDataAllocationMap();

		dataAllocation.createStorageListIneachStroageNode(application);
		dataAllocation.printDataAllocationMap(application);

		application.getDataConsistencyProtocol().initializeLockReplicaMap(dataAllocation);
		application.getDataConsistencyProtocol().printLockReplicaMap();
		application.initializeTupeToFogDeviceIdMap();

		CloudSim.broker = broker;
		DataPlacement.sendPerdiodicTuples = false;
		DataPlacement.cond = 0;
		DataPlacement.estimatedTuple = "";
		
		System.gc();
		
		begin_t = Calendar.getInstance().getTimeInMillis();

		CloudSim.startSimulation();
		end_t = Calendar.getInstance().getTimeInMillis();
		period_t = end_t - begin_t;
		org.fog.examples.Log.writeSimulationTime(DataPlacement.nb_HGW,"Methode: iFogStor" + "\ttime:" + String.valueOf(period_t));
		
		if(DataPlacement.critical_data_pourcentage == 0){
			Stats.saveConsumption();
			Stats.saveProduction();
		}
			
		

		application.getDataConsistencyProtocol().addBlockedWritesLatency();

		CloudSim.stopSimulation();

		System.out.println("End of simulation!");

		System.out.println(DataPlacement.storageMode);

		System.out.println("End of simulation!");

		// SaveResults.saveLatencyTimes(DataPlacement.nb_DataCons_By_DataProd,
		// DataPlacement.storageMode, "", 1 ,
		// LatencyStats.getOverall_write_Latency(),
		// LatencyStats.getOverall_read_Latency(),
		// LatencyStats.getOverall_delayed_write_Latency(),
		// Stats.version_exchange_latency,
		// LatencyStats.getOverall_blocked_write_Latency(),
		// LatencyStats.getOverall_Latency());

		SaveResults.saveAllStats();
		LatencyStats.saveLatencyMap(0);

		LatencyStats.reset_LatencyMapStats();
		LatencyStats.reset_ALLStats();
		Stats.reset_AllStats();

		application.getDataConsistencyProtocol().clearConsistencyData();
		application.resetStoredData();
		System.gc();

		org.fog.examples.Log.writeNbCombinaison(DataPlacement.nb_HGW, DataPlacement.Consistencyprotocol);
		org.fog.examples.Log.writeSimulationTime(DataPlacement.nb_HGW,DataPlacement.Consistencyprotocol);

		// Scanner sc = new Scanner(System.in);
		// String str = sc.nextLine();

		/*
		 * tests
		 */
		System.out.println("\n------------------------------------");
		System.out.println("------- Shortest Paths nodes --------");
		System.out.println("------------------------------------ ");
		
		System.gc();

		// sc = new Scanner(System.in);
		// str = sc.nextLine();

		DataPlacement.load_consumption_times = true;

		DataPlacement.min_data_replica = 1;
		DataPlacement.max_data_replica = 5;

		Pmedian allPmedian = new Pmedian();
		
		begin_t = Calendar.getInstance().getTimeInMillis();
		allPmedian.computeAllShortestPathsNodes(application);
		end_t = Calendar.getInstance().getTimeInMillis();
		period_t = end_t - begin_t;
		org.fog.examples.Log.writeShortestPathTime(DataPlacement.nb_HGW,"All Shortest Paths" + "\ttime:" + String.valueOf(period_t));
		
		System.gc();
		
		begin_t = Calendar.getInstance().getTimeInMillis();
		allPmedian.computeAllPMedian(application);
		end_t = Calendar.getInstance().getTimeInMillis();
		period_t = end_t - begin_t;
		org.fog.examples.Log.writePmedianTime(DataPlacement.nb_HGW,"All P Median" + "\ttime:" + String.valueOf(period_t));
		
		System.gc();
		// allPmedian.printAllShortestPathNodes();
		// System.out.println();System.out.println();
		// allPmedian.printAllPMedian();

		// sc = new Scanner(System.in);
		// str = sc.nextLine();

		System.out.println("\n------------------------------------");
		System.out.println("------- Estimation  iFogStorP --------");
		System.out.println("------------------------------------ ");

		begin_t = Calendar.getInstance().getTimeInMillis();
		ConsitencyOverheadiFogStorP iFogStorP = new ConsitencyOverheadiFogStorP(application, moduleMapping);
		iFogStorP.initializeEstimationMap();
		iFogStorP.consitencyEsitmation();
		end_t = Calendar.getInstance().getTimeInMillis();
		period_t = end_t - begin_t;
		org.fog.examples.Log.writeSimulationTime(DataPlacement.nb_HGW,"Methode: iFogStorP" + "\ttime:" + String.valueOf(period_t));

		iFogStorP.getEstimationSolution();
		iFogStorP.saveEstimationMap();
		iFogStorP.saveEstimationSolution();
		iFogStorP.saveAllEstimationValues();
		LatencyStats.saveLatencyMap(1);
		System.out.println("overall latency = "+ iFogStorP.computeOverallLatency());
		// estimation.printEstimationMap();
		
		

//		 System.out.println("\n------------------------------------");
//		 System.out.println("-------   Estimation iFogStorS   --------");
//		 System.out.println("------------------------------------ ");
//		
//		 begin_t = Calendar.getInstance().getTimeInMillis();
//		 ConsistencyOverheadiFogStorS iFogStorS = new ConsistencyOverheadiFogStorS(application, moduleMapping);
//		 iFogStorS.initializeEstimationMap();
//		 iFogStorS.consitencyEsitmation();
//		 end_t = Calendar.getInstance().getTimeInMillis();
//		 period_t = end_t - begin_t;
//		 org.fog.examples.Log.writeSimulationTime(DataPlacement.nb_HGW,"Methode: iFogStorS"+"		time:"+String.valueOf(period_t));
//		 iFogStorS.getEstimationSolution();
//		 iFogStorS.saveEstimationMap();
//		 iFogStorS.saveEstimationSolution();
//		 LatencyStats.saveLatencyMap(2);
//		 System.out.println("overall latency = "+iFogStorS.computeOverallLatency());
//		 
		 
		
		 
//		 System.out.println("\n------------------------------------");
//		 System.out.println("------- Estimation   Exact --------");
//		 System.out.println("------------------------------------ ");
//		 
//		 begin_t = Calendar.getInstance().getTimeInMillis();
//		 ConsistencyOverheadExact exact = new ConsistencyOverheadExact(application, moduleMapping);
//		 exact.initializeEstimationMap();
//		 exact.consitencyEsitmation();
//		 end_t = Calendar.getInstance().getTimeInMillis();
//		 period_t = end_t - begin_t;
//		 org.fog.examples.Log.writeSimulationTime(DataPlacement.nb_HGW,"Methode: Exact"+"		time:"+String.valueOf(period_t));
//		 exact.getEstimationSolution();
//		 exact.saveEstimationMap();
//		 exact.saveEstimationSolution();
//		 LatencyStats.saveLatencyMap(3);
//		 System.out.println("overall latency = "+exact.computeOverallLatency());
//		
//		 org.fog.examples.Log.writeNbCombinaison(DataPlacement.nb_HGW,"Methode: Exact "+"\tNb_Combin:"+String.valueOf(allCombinExact()));
//		 org.fog.examples.Log.writeNbCombinaison(DataPlacement.nb_HGW,"======================================================");
//		 org.fog.examples.Log.writeSimulationTime(DataPlacement.nb_HGW,"======================================================");
		 
		 
		
		System.out.println("End of all simulations");

	}

	private long allCombinExact() {
		long nb_combin = 0;
		
		int nb_nodes = DataPlacement.nb_DC + DataPlacement.nb_RPOP+ DataPlacement.nb_LPOP + DataPlacement.nb_HGW;
		int nb_tuple = application.getEdges().size()- (DataPlacement.nb_HGW * DataPlacement.nb_SnrPerHGW + DataPlacement.nb_HGW * DataPlacement.nb_ActPerHGW);
		
		System.out.println("nb_nodes=" + nb_nodes + "\tnb_tuples=" + nb_tuple);
		System.out.println("DataPlacement.min_data_replica="+ DataPlacement.min_data_replica+ "\tDataPlacement.max_data_replica"+ DataPlacement.max_data_replica);
		
		for (int i = DataPlacement.min_data_replica; i < DataPlacement.max_data_replica + 1; i++) {
			nb_combin = nb_combin+ getCombin(i, nb_nodes);
		}

		return nb_combin * nb_tuple;
	}

	private long getCombin(int p, int n) {
		// TODO Auto-generated method stub
		long f = fact(n) / (fact(p) * fact(n - p));
		System.out.println("P=" + p + "\tn=" + n+"\tC_n^p = "+f);
		return f;
	}

	private long fact(int f) {
		long fa = 1;
		for (int i = 1; i < f + 1; i++) {
			fa = fa *i;
		}
		return fa;
	}

	private void generateDataActorsFile() {
		int dataHost, dataCons, dataProd;
		dataHost = DataPlacement.nb_HGW + DataPlacement.nb_LPOP
				+ DataPlacement.nb_RPOP + DataPlacement.nb_DC;
		dataProd = DataPlacement.nb_Service_HGW + DataPlacement.nb_Service_LPOP
				+ DataPlacement.nb_Service_RPOP;
		dataCons = DataPlacement.nb_Service_HGW + DataPlacement.nb_Service_LPOP
				+ DataPlacement.nb_Service_RPOP + DataPlacement.nb_Service_DC;

		File fichier = new File(DataPlacement.nb_HGW + "dataActors_"
				+ DataPlacement.nb_DataCons_By_DataProd + ".txt");
		FileWriter fw;
		try {
			fw = new FileWriter(fichier);
			fw.write(dataHost + "\t");
			fw.write(dataProd + "\t");
			fw.write(dataCons + "\t");
			fw.write(DataPlacement.Basis_Exchange_Unit + "\t");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

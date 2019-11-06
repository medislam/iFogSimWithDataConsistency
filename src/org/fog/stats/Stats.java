package org.fog.stats;

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
import org.fog.examples.DataPlacement;
import org.fog.utils.Config;


public class Stats {
	
	public static Map<Pair<String,Integer>,Integer> nb_version_roll_back = new HashMap<Pair<String,Integer>,Integer>();
	
	public static int nb_totol_write = 0;
	
	public static int nb_done_write = 0;
	
	public static int nb_bloked_write = 0;
	
	public static int nb_bloked_write_for_blocked_write = 0;
	
	public static int nb_response_write = 0;
	
	public static int nb_delayed_write = 0;
	
	public static int nb_locked_write = 0;
	
	public static int nb_replica_violation_in_write = 0;
	
	
	public static int nb_totol_read = 0;
				
	public static int nb_served_read = 0;
	
	public static int nb_non_served_read = 0;
	
	public static int nb_replica_violation_in_read = 0;
	
	public static int nb_read_served_with_recent_version = 0;
	public static int nb_read_served_with_version_old_1 = 0;
	public static int nb_read_served_with_version_old_2 = 0;
	public static int nb_read_served_with_version_old_3 = 0;
	public static int nb_read_served_with_version_old_4 = 0;
	public static int nb_read_served_with_version_old_5 = 0;
	public static int nb_read_served_with_version_old_up5 = 0;
	
		
	public static int version_exchange_latency = 0;
	
	/*
	 * Map<TupleType, List<Float>>
	 */
	public static Map<String, List<Float>> production = new HashMap<String, List<Float>>();
	
	/*
	 * Map<Pair<TupleType, ServiceName>, List<Float>>
	 */
	public static Map<Pair<String, String>, List<Float>> consumption = new HashMap<Pair<String, String>, List<Float>>();
	
	public Stats() {
		// TODO Auto-generated constructor stub
	}
	
	public static void incrementVersionRollBack(String tupleType, int rep){
		Pair<String,Integer> pair = new Pair<String,Integer>(tupleType,rep);
		if(!nb_version_roll_back.containsKey(pair)){
			nb_version_roll_back.put(pair, 1);
			
		}else{
			nb_version_roll_back.put(pair, nb_version_roll_back.get(pair)+1);
		}		
	}
	
	public static void incrementTotalWrite(){
		//**System.out.println("increment Total write!");
		nb_totol_write++;
		//*//**System.out.println("nb_totol_write="+nb_totol_write);

	}
	
	public static void incrementDoneWrite(){
		//**System.out.println("increment Done write!");
		nb_done_write++;
		//*//**System.out.println("nb_done_write="+nb_done_write);
		
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();

	}

	public static void incrementDelayedWrite(){
		//**System.out.println("increment Delyaed write!");
		nb_delayed_write++;
	}

	public static void incrementLockedWrite(){
		//**System.out.println("increment locked write!");
		nb_locked_write++;
	}

	public static void incrementReplicaViolationInWrite(){
		//**System.out.println("increment replicas violation in write!");
		nb_replica_violation_in_write++;
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
	}

	public static void incrementTotalRead(){
		nb_totol_read++;
	}
	
	public static void incrementServedRead(){
		//**System.out.println("increment served read");
		nb_served_read++;
	}

	public static void incrementNonServedRead(){
		//**System.out.println("nb_non_served_read");
		nb_non_served_read++;
	}
	
	public static void incrementReplicaViolationInRead(){
		//**System.out.println("nb_replica_violation_in_read");
		nb_replica_violation_in_read++;
	}
	
	
	public static void incrementReadServedWithRecentVersion(int version){
		
		switch (version) {
			case 0:
				nb_read_served_with_recent_version++;
				//**System.out.println("nb_read_served_with_recent_version");
				break;
			case 1:
				nb_read_served_with_version_old_1++;
				//**System.out.println("nb_read_served_with_version_old_1");
				break;
			case 2:
				nb_read_served_with_version_old_2++;
				//**System.out.println("nb_read_served_with_version_old_2");
				break;
			case 3:
				nb_read_served_with_version_old_3++;
				//**System.out.println("nb_read_served_with_version_old_3");
				break;
			case 4:
				nb_read_served_with_version_old_4++;
				//**System.out.println("nb_read_served_with_version_old_4");
				break;
			case 5:
				nb_read_served_with_version_old_5++;
				//**System.out.println("nb_read_served_with_version_old_5");
				break;
			default:
				nb_read_served_with_version_old_up5++;
				//**System.out.println("nb_read_served_with_version_old_up5");
				break;
		}
	}
	
	
	public static void incrementVersionExchangeLatency(float latency){
		//**System.out.println("version_exchange_latency : + "+latency);
		version_exchange_latency+=latency;
	}
	
	public static void incrementResponseWrite(){
		//**System.out.println("increment response write!");
		nb_response_write++;
	}  
	
	public static void incrementBlockedWrite(){
		//**System.out.println("increment blocked write!");
		nb_bloked_write++;
	} 
	
	public static void incrementBlockedWriteForBlockedWrite(){
		//**System.out.println("increment blocked write for blocked write!");
		nb_bloked_write_for_blocked_write++;
	} 
	
	public static void saveProduction() throws IOException{
		FileWriter file;
		
		if(DataPlacement.cond==0){
			file = new FileWriter("Stats/"+DataPlacement.nb_HGW+"productionGeneral"+DataPlacement.nb_DataCons_By_DataProd);	
		
		}else if(DataPlacement.cond==1){
			file = new FileWriter("Stats/iFogStorP/"+DataPlacement.nb_HGW+"productionTemp"+DataPlacement.nb_DataCons_By_DataProd, true);
		
		}else if(DataPlacement.cond==2){
			file = new FileWriter("Stats/iFogStorS/"+DataPlacement.nb_HGW+"productionTemp"+DataPlacement.nb_DataCons_By_DataProd, true);
		
		}else if(DataPlacement.cond==3){
			file = new FileWriter("Stats/Exact/"+DataPlacement.nb_HGW+"productionTemp"+DataPlacement.nb_DataCons_By_DataProd, true);
		
		}else {
			file = null;
			System.out.println("Erreur, saveProduction cond = "+DataPlacement.cond);
			System.exit(0);;
		}
		
		BufferedWriter fw = new BufferedWriter(file);
		
		for(String tupleType: production.keySet()){
			fw.write(tupleType+"\t");
			for(Float time : production.get(tupleType)){
				fw.write(time+"\t");
			}
			fw.write("\n");
		}
		
		fw.close();
		
	}
	
	public static List<Double> loadProduction(String tupleType){
		//**System.out.println("Load production time for tuple:"+tupleType);
		List<Double> productionTimes = new ArrayList<Double>();

		boolean cond = false;
		try {
			FileReader fichier = new FileReader("Stats/"+DataPlacement.nb_HGW+"productionGeneral"+DataPlacement.nb_DataCons_By_DataProd);
			BufferedReader in = new BufferedReader(fichier);
			String line = null;
			

			while ((line = in.readLine()) != null) {
				String[] splited = line.split("\t");
				String tuple = splited[0];
				
				if(tuple.equals(tupleType)){
					cond = true;
					for(int i=1; i<splited.length;i++){
						productionTimes.add(Double.valueOf(splited[i]));
					}
					break;
				}	
			}
			
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(!cond){
			//**System.out.println("Error, production list for tuple:"+tupleType+" is empty!");
			//System.exit(0);
		}
		return productionTimes;
	}
	
	public static void saveConsumption() throws IOException{
		FileWriter file;
		
		if(DataPlacement.cond==0){
			file = new FileWriter("Stats/"+DataPlacement.nb_HGW+"consumptionGeneral"+DataPlacement.nb_DataCons_By_DataProd);	
		
		}else if(DataPlacement.cond==1){
			file = new FileWriter("Stats/iFogStorP/"+DataPlacement.nb_HGW+"consumptionTemp"+DataPlacement.nb_DataCons_By_DataProd, true);
		
		}else if(DataPlacement.cond==2){
			file = new FileWriter("Stats/iFogStorS/"+DataPlacement.nb_HGW+"consumptionTemp"+DataPlacement.nb_DataCons_By_DataProd, true);
		
		}else if(DataPlacement.cond==3){
			file = new FileWriter("Stats/Exact/"+DataPlacement.nb_HGW+"consumptionTemp"+DataPlacement.nb_DataCons_By_DataProd, true);
		
		}else {
			file = null;
			System.out.println("Erreur, saveConsumption cond = "+DataPlacement.cond);
			System.exit(0);;
		}
		
		
		BufferedWriter fw = new BufferedWriter(file);
		
		for(Pair<String, String> pair: consumption.keySet()){
			fw.write(pair.getFirst()+"\t"+pair.getSecond());
			for(Float time :consumption.get(pair)){
				fw.write("\t"+time.toString());
			}
			//fw.write(pair.getFirst()+"\t"+pair.getSecond()+"\t"+consumption.get(pair).toString());
			fw.write("\n");
		}

		fw.close();
		
	}
	
	public static List<Double> loadConsumption(String tupleType, String serviceName){
		////**System.out.println("Load consumption time for tuple:"+tupleType);
		List<Double> consumptionTimes = new ArrayList<Double>();

		boolean cond = false;
		try {
			FileReader fichier = new FileReader("Stats/"+DataPlacement.nb_HGW+"consumptionGeneral"+DataPlacement.nb_DataCons_By_DataProd);
			BufferedReader in = new BufferedReader(fichier);
			String line = null;
			

			while ((line = in.readLine()) != null) {
				String[] splited = line.split("\t");
				String tuple = splited[0];
				String serviceN = splited[1];
				
				if(tuple.equals(tupleType) && serviceN.equals(serviceName)){
					cond = true;
					for(int i=2; i<splited.length;i++){
						consumptionTimes.add(Double.valueOf(splited[i]));
					}
					break;
				}	
			}
			
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(!cond){
			System.out.println("Error, consumption list for tuple:"+tupleType+" is empty!");
			//System.exit(0);
		}
		return consumptionTimes;
	}
	
	public static int getProductionTimes(int sourceNode){
		
		for(String tupleType: production.keySet()){
			return production.get(FogStorage.application.fogDevicetotTupleMap.get(sourceNode)).size();
		}

		return -1;

	}
	
	public static int getConsumptionTimes(){
		
		return (int) ((Config.MAX_SIMULATION_TIME - 1) / DataPlacement.DataConsRequestInterval);
				
	}

	public static void reset_AllStats() {
		// TODO Auto-generated method stub
		
		nb_totol_write = 0;
		
		nb_done_write = 0;
		
		nb_bloked_write = 0;
		
		nb_bloked_write_for_blocked_write = 0;
		
		nb_response_write = 0;
		
		nb_delayed_write = 0;
		
		nb_locked_write = 0;
		
		nb_replica_violation_in_write = 0;
		
		
		nb_totol_read = 0;
					
		nb_served_read = 0;
		
		nb_non_served_read = 0;
		
		nb_replica_violation_in_read = 0;
		
		nb_read_served_with_recent_version = 0;
		
		version_exchange_latency = 0;
		
		nb_read_served_with_version_old_1 = 0;
		nb_read_served_with_version_old_2 = 0;
		nb_read_served_with_version_old_3 = 0;
		nb_read_served_with_version_old_4 = 0;
		nb_read_served_with_version_old_5 = 0;
		nb_read_served_with_version_old_up5 = 0;
		
		consumption.clear();
		production.clear();
	
	}
	
}
